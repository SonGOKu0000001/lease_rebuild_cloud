package com.kami.cloud.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kami.cloud.lease.common.constant.RedisConstant;
import com.kami.cloud.lease.common.login.LoginUserHolder;
import com.kami.cloud.lease.model.entity.*;
import com.kami.cloud.lease.model.enums.ItemType;
import com.kami.cloud.lease.web.app.mapper.*;
import com.kami.cloud.lease.web.app.service.ApartmentInfoService;
import com.kami.cloud.lease.web.app.service.BrowsingHistoryService;
import com.kami.cloud.lease.web.app.service.LabelInfoService;
import com.kami.cloud.lease.web.app.service.RoomInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kami.cloud.lease.web.app.vo.apartment.ApartmentItemVo;
import com.kami.cloud.lease.web.app.vo.attr.AttrValueVo;
import com.kami.cloud.lease.web.app.vo.fee.FeeValueVo;
import com.kami.cloud.lease.web.app.vo.graph.GraphVo;
import com.kami.cloud.lease.web.app.vo.room.RoomDetailVo;
import com.kami.cloud.lease.web.app.vo.room.RoomItemVo;
import com.kami.cloud.lease.web.app.vo.room.RoomQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kami
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private PaymentTypeMapper  paymentTypeMapper;
    @Autowired
    private ApartmentInfoService  apartmentInfoService;
    @Autowired
    private BrowsingHistoryService browsingHistoryService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public IPage<RoomItemVo> pageRoomItemVo(Page<RoomItemVo> page, RoomQueryVo queryVo) {
        // 1. 构建分页对象（只查主表 room_info）
        Page<RoomInfo> roomPage = new Page<>(page.getCurrent(), page.getSize());
        // 处理排序（利用 MP 的 OrderItem，安全无注入）
        if (StringUtils.hasText(queryVo.getOrderType())) {
            // 校验排序方向
            boolean isAsc = "asc".equalsIgnoreCase(queryVo.getOrderType());
            // 这里假设按租金排序，字段名对应数据库列名 rent
            roomPage.addOrder(isAsc ? OrderItem.asc("rent") : OrderItem.desc("rent"));
        }

        // 执行主表分页查询（只查 room_info，不 JOIN 任何关联表）
        // 注意：查询条件只有 ri.is_deleted = 0，如果还有其他筛选条件也加在这里
        IPage<RoomInfo> roomInfoPage = roomInfoMapper.pageWithRoomQueryVo(roomPage , queryVo);

        // 2. 提取当前页的房间 ID 列表
        List<RoomInfo> roomRecords = roomInfoPage.getRecords();
        List<RoomItemVo> voList = new ArrayList<>();

        if (!roomRecords.isEmpty()) {
            List<Long> roomIds = roomRecords.stream()
                    .map(RoomInfo::getId)
                    .collect(Collectors.toList());

            // 3. 批量查询关联数据（并组装成 Map<房间ID, 关联数据>）
            // 3.1 查询图片列表 (graph_info)
            List<GraphInfo> graphList = graphInfoMapper.selectList(
                    new LambdaQueryWrapper<GraphInfo>()
                            .eq(GraphInfo::getItemType, ItemType.ROOM)
                            .in(GraphInfo::getItemId, roomIds)
                            .eq(GraphInfo::getIsDeleted, 0)
            );

            // 转成GraphVo并按房间 ID 分组
            Map<Long, List<GraphVo>> graphMap = new HashMap<>();
            for (GraphInfo g : graphList) {
                GraphVo vo = new GraphVo();
                vo.setName(g.getName());
                vo.setUrl(g.getUrl());

                Long roomId = g.getItemId();  // 从原实体获取
                graphMap.computeIfAbsent(roomId, k -> new ArrayList<>()).add(vo);
            }

            // 3.2 查询标签列表 (label_info)
            // 需要关联 room_label 中间表，这里可以写一个 Mapper 方法用 IN 查，或者手写 SQL
            // 假设有方法 selectLabelListByRoomIds(roomIds)
            List<LabelInfo> labelList = labelInfoMapper.selectListByRoomIds(roomIds);
            Map<Long, List<LabelInfo>> labelMap = labelList.stream()
                    .collect(Collectors.groupingBy(LabelInfo::getRoomId)); // 需保证 LabelInfo 里有 roomId 字段

            // 3.3 查询公寓信息 (apartment_info)
            // 由于多个房间可能属于同一个公寓，为了避免重复查询，先提取公寓ID去重
            List<Long> apartmentIds = roomRecords.stream()
                    .map(RoomInfo::getApartmentId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, ApartmentInfo> apartmentMap = apartmentInfoMapper.selectBatchIds(apartmentIds)
                    .stream()
                    .collect(Collectors.toMap(ApartmentInfo::getId, Function.identity()));

            // 4. 循环组装最终的 RoomItemVo
            for (RoomInfo room : roomRecords) {
                RoomItemVo vo = new RoomItemVo();
                // 拷贝基础字段（id, roomNumber, rent 等）
                BeanUtils.copyProperties(room, vo);
                // 设置图片列表（没图片就给空集合，避免前端报错）
                vo.setGraphVoList(graphMap.getOrDefault(room.getId(), Collections.emptyList()));
                // 设置标签列表
                vo.setLabelInfoList(labelMap.getOrDefault(room.getId(), Collections.emptyList()));
                // 设置公寓信息
                vo.setApartmentInfo(apartmentMap.get(room.getApartmentId()));
                voList.add(vo);
            }
        }

        page.setRecords(voList);
        page.setTotal(roomInfoPage.getTotal());

        return page;
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        String key = RedisConstant.APP_ROOM_PREFIX + id;
        RoomDetailVo roomDetailVo = (RoomDetailVo)redisTemplate.opsForValue().get(key);
        if (roomDetailVo == null) {
            //1.查询房间信息
            RoomInfo roomInfo = roomInfoMapper.selectById(id);
            if (roomInfo == null) {
                return null;
            }
            //2.查询图片
            List<GraphVo> graphVoList = graphInfoMapper.selectByItemTypeAndId(ItemType.ROOM, id);
            //3.查询租期
            List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);
            //4.查询配套
            List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(id);
            //5.查询标签
            List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);
            //6.查询支付方式
            List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);
            //7.查询基本属性
            List<AttrValueVo> attrValueVoList = attrValueMapper.selectListByRoomId(id);
            //8.查询杂费信息
            List<FeeValueVo> feeValueVoList = feeValueMapper.selectListByApartmentId(roomInfo.getApartmentId());
            //9.查询公寓信息
            ApartmentItemVo apartmentItemVo = apartmentInfoService.selectApartmentItemVoById(roomInfo.getApartmentId());

            roomDetailVo = new RoomDetailVo();
            BeanUtils.copyProperties(roomInfo, roomDetailVo);

            roomDetailVo.setApartmentItemVo(apartmentItemVo);
            roomDetailVo.setGraphVoList(graphVoList);
            roomDetailVo.setAttrValueVoList(attrValueVoList);
            roomDetailVo.setFacilityInfoList(facilityInfoList);
            roomDetailVo.setLabelInfoList(labelInfoList);
            roomDetailVo.setPaymentTypeList(paymentTypeList);
            roomDetailVo.setFeeValueVoList(feeValueVoList);
            roomDetailVo.setLeaseTermList(leaseTermList);

            redisTemplate.opsForValue().set(key,roomDetailVo);
        }

        browsingHistoryService.saveHistory(LoginUserHolder.getLoginUser().getUserId() , id);
        return roomDetailVo;
    }

    @Override
    public IPage<RoomItemVo> pageByApartmentId(Page<RoomItemVo> page, Long id) {
        // 1. 构建分页对象（只查主表 room_info）
        Page<RoomInfo> roomPage = new Page<>(page.getCurrent(), page.getSize());

        // 执行主表分页查询（只查 room_info，不 JOIN 任何关联表）
        // 注意：查询条件只有 ri.is_deleted = 0，如果还有其他筛选条件也加在这里
        IPage<RoomInfo> roomInfoPage = roomInfoMapper.pageWithApartmentId(roomPage , id);

        // 2. 提取当前页的房间 ID 列表
        List<RoomInfo> roomRecords = roomInfoPage.getRecords();
        List<RoomItemVo> voList = new ArrayList<>();

        if (!roomRecords.isEmpty()) {
            List<Long> roomIds = roomRecords.stream()
                    .map(RoomInfo::getId)
                    .collect(Collectors.toList());

            // 3. 批量查询关联数据（并组装成 Map<房间ID, 关联数据>）
            // 3.1 查询图片列表 (graph_info)
            List<GraphInfo> graphList = graphInfoMapper.selectList(
                    new LambdaQueryWrapper<GraphInfo>()
                            .eq(GraphInfo::getItemType, ItemType.ROOM)
                            .in(GraphInfo::getItemId, roomIds)
                            .eq(GraphInfo::getIsDeleted, 0)
            );

            // 转成GraphVo并按房间 ID 分组
            Map<Long, List<GraphVo>> graphMap = new HashMap<>();
            for (GraphInfo g : graphList) {
                GraphVo vo = new GraphVo();
                vo.setName(g.getName());
                vo.setUrl(g.getUrl());

                Long roomId = g.getItemId();  // 从原实体获取
                graphMap.computeIfAbsent(roomId, k -> new ArrayList<>()).add(vo);
            }

            // 3.2 查询标签列表 (label_info)
            // 需要关联 room_label 中间表，这里可以写一个 Mapper 方法用 IN 查，或者手写 SQL
            // 假设有方法 selectLabelListByRoomIds(roomIds)
            List<LabelInfo> labelList = labelInfoMapper.selectListByRoomIds(roomIds);
            Map<Long, List<LabelInfo>> labelMap = labelList.stream()
                    .collect(Collectors.groupingBy(LabelInfo::getRoomId)); // 需保证 LabelInfo 里有 roomId 字段

            // 3.3 查询公寓信息 (apartment_info)
            // 由于多个房间可能属于同一个公寓，为了避免重复查询，先提取公寓ID去重
            List<Long> apartmentIds = roomRecords.stream()
                    .map(RoomInfo::getApartmentId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, ApartmentInfo> apartmentMap = apartmentInfoMapper.selectBatchIds(apartmentIds)
                    .stream()
                    .collect(Collectors.toMap(ApartmentInfo::getId, Function.identity()));

            // 4. 循环组装最终的 RoomItemVo
            for (RoomInfo room : roomRecords) {
                RoomItemVo vo = new RoomItemVo();
                // 拷贝基础字段（id, roomNumber, rent 等）
                BeanUtils.copyProperties(room, vo);
                // 设置图片列表（没图片就给空集合，避免前端报错）
                vo.setGraphVoList(graphMap.getOrDefault(room.getId(), Collections.emptyList()));
                // 设置标签列表
                vo.setLabelInfoList(labelMap.getOrDefault(room.getId(), Collections.emptyList()));
                // 设置公寓信息
                vo.setApartmentInfo(apartmentMap.get(room.getApartmentId()));
                voList.add(vo);
            }
        }

        page.setRecords(voList);
        page.setTotal(roomInfoPage.getTotal());

        return page;
    }
}




