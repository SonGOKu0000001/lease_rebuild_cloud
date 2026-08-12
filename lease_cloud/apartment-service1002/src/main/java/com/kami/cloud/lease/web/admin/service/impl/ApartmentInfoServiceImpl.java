package com.kami.cloud.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kami.cloud.lease.common.exception.LeaseException;
import com.kami.cloud.lease.common.feign.AgreementFeignApi;
import com.kami.cloud.lease.common.feign.dto.ApartmentRentCountDTO;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.common.result.ResultCodeEnum;
import com.kami.cloud.lease.model.entity.*;
import com.kami.cloud.lease.model.enums.ItemType;
import com.kami.cloud.lease.model.enums.ReleaseStatus;
import com.kami.cloud.lease.web.admin.mapper.*;
import com.kami.cloud.lease.web.admin.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kami.cloud.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.kami.cloud.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.kami.cloud.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.kami.cloud.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.kami.cloud.lease.web.admin.vo.fee.FeeValueVo;
import com.kami.cloud.lease.web.admin.vo.graph.GraphVo;
import org.jetbrains.annotations.TestOnly;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author kami
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Slf4j
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private ApartmentFacilityService apartmentFacilityService;
    @Autowired
    private ApartmentLabelService apartmentLabelService;
    @Autowired
    private ApartmentFeeValueService apartmentFeeValueService;
    @Autowired
    private AgreementFeignApi agreementFeignApi;
    @Autowired
    private ProvinceInfoService provinceInfoService;
    @Autowired
    private CityInfoService cityInfoService;
    @Autowired
    private DistrictInfoService districtInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        fillRegionName(apartmentSubmitVo);
        boolean isUpdate = apartmentSubmitVo.getId()!=null;
        super.saveOrUpdate(apartmentSubmitVo);

        if (isUpdate){
            //1.删除图片列表
            LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
            graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphQueryWrapper.eq(GraphInfo::getItemId, apartmentSubmitVo.getId());
            graphInfoService.remove(graphQueryWrapper);

            //2.删除配套列表
            LambdaQueryWrapper<ApartmentFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
            facilityQueryWrapper.eq(ApartmentFacility::getApartmentId,apartmentSubmitVo.getId());
            apartmentFacilityService.remove(facilityQueryWrapper);

            //3.删除标签列表
            LambdaQueryWrapper<ApartmentLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
            labelQueryWrapper.eq(ApartmentLabel::getApartmentId,apartmentSubmitVo.getId());
            apartmentLabelService.remove(labelQueryWrapper);

            //4.删除杂费列表
            LambdaQueryWrapper<ApartmentFeeValue> feeQueryWrapper = new LambdaQueryWrapper<>();
            feeQueryWrapper.eq(ApartmentFeeValue::getApartmentId,apartmentSubmitVo.getId());
            apartmentFeeValueService.remove(feeQueryWrapper);

        }

        //1.插入图片列表
        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfo.setItemId(apartmentSubmitVo.getId());
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }

        //2.插入配套列表
        List<Long> facilityInfoIdList = apartmentSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIdList)){
            ArrayList<ApartmentFacility> facilityList = new ArrayList<>();
            for (Long facilityId : facilityInfoIdList) {
                ApartmentFacility apartmentFacility = new ApartmentFacility();
                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                apartmentFacility.setFacilityId(facilityId);
                facilityList.add(apartmentFacility);
            }
            apartmentFacilityService.saveBatch(facilityList);
        }

        //3.插入标签列表
        List<Long> labelIds = apartmentSubmitVo.getLabelIds();
        if (!CollectionUtils.isEmpty(labelIds)) {
            List<ApartmentLabel> apartmentLabelList = new ArrayList<>();
            for (Long labelId : labelIds) {
                ApartmentLabel apartmentLabel = new ApartmentLabel();
                apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                apartmentLabel.setLabelId(labelId);
                apartmentLabelList.add(apartmentLabel);
            }
            apartmentLabelService.saveBatch(apartmentLabelList);
        }

        //4.插入杂费列表
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        if (!CollectionUtils.isEmpty(feeValueIds)) {
            ArrayList<ApartmentFeeValue> apartmentFeeValueList = new ArrayList<>();
            for (Long feeValueId : feeValueIds) {
                ApartmentFeeValue apartmentFeeValue = new ApartmentFeeValue();
                apartmentFeeValue.setApartmentId(apartmentSubmitVo.getId());
                apartmentFeeValue.setFeeValueId(feeValueId);
                apartmentFeeValueList.add(apartmentFeeValue);
            }
            apartmentFeeValueService.saveBatch(apartmentFeeValueList);
        }
    }

    /**
     * 根据省市区id反查名称并填充，地区表查询不到时保留前端传值
     */
    private void fillRegionName(ApartmentSubmitVo vo) {
        if (vo.getProvinceId() != null) {
            ProvinceInfo province = provinceInfoService.getById(vo.getProvinceId());
            if (province != null && province.getName() != null && !province.getName().isEmpty()) {
                vo.setProvinceName(province.getName());
            }
        }
        if (vo.getCityId() != null) {
            CityInfo city = cityInfoService.getById(vo.getCityId());
            if (city != null && city.getName() != null && !city.getName().isEmpty()) {
                vo.setCityName(city.getName());
            }
        }
        if (vo.getDistrictId() != null) {
            DistrictInfo district = districtInfoService.getById(vo.getDistrictId());
            if (district != null && district.getName() != null && !district.getName().isEmpty()) {
                vo.setDistrictName(district.getName());
            }
        }
    }

    @Override
    public IPage<ApartmentItemVo> pageItem(IPage<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        IPage<ApartmentItemVo> result = apartmentInfoMapper.pageItem(page, queryVo);
        List<ApartmentItemVo> records = result.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        List<Long> apartmentIds = records.stream()
                .map(ApartmentItemVo::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        try {
            Result<List<ApartmentRentCountDTO>> feignResult =
                    agreementFeignApi.rentingCountByApartment(apartmentIds);
            if (feignResult != null && feignResult.getData() != null) {
                Map<Long, Long> rentCountMap = feignResult.getData().stream()
                        .collect(Collectors.toMap(ApartmentRentCountDTO::getApartmentId,
                                ApartmentRentCountDTO::getRentCount, (a, b) -> a));
                for (ApartmentItemVo item : records) {
                    Long rentCount = rentCountMap.getOrDefault(item.getId(), 0L);
                    Long total = item.getTotalRoomCount() == null ? 0L : item.getTotalRoomCount();
                    item.setFreeRoomCount(Math.max(0L, total - rentCount));
                }
            }
        } catch (Exception e) {
            log.warn("获取公寓在租数量失败，空闲房间数按总数处理，apartmentIds={}", apartmentIds, e);
            for (ApartmentItemVo item : records) {
                if (item.getFreeRoomCount() == null) {
                    item.setFreeRoomCount(item.getTotalRoomCount() == null ? 0L : item.getTotalRoomCount());
                }
            }
        }
        return result;
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //1.查询公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        if (apartmentInfo == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_APARTMENT_NOT_EXIST);
        }
        //2.查询图片列表
        List<GraphVo> graphVoList = graphInfoMapper.selectByItemTypeAndId(ItemType.APARTMENT , id);
        //3.查询标签列表
        List<LabelInfo> labelInfoList = labelInfoMapper.selectByApartmentId(id);
        //4.查询配套列表
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectByApartmentId(id);
        //5.查询杂费列表
        List<FeeValueVo> feeValueVoList = feeValueMapper.selectByApartmentId(id);
        //6.组装数据返回

        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo,apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);
        return apartmentDetailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeApartmentById(Long id) {
        LambdaQueryWrapper<RoomInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomInfo::getApartmentId, id);
        Long roomCount = roomInfoMapper.selectCount(queryWrapper);
        if(roomCount>0){
            throw new LeaseException(ResultCodeEnum.ADMIN_APARTMENT_DELETE_ERROR);
        }
        super.removeById(id);
        //1.删除图片列表
        LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
        graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        graphQueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoService.remove(graphQueryWrapper);

        //2.删除配套列表
        LambdaQueryWrapper<ApartmentFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
        facilityQueryWrapper.eq(ApartmentFacility::getApartmentId,id);
        apartmentFacilityService.remove(facilityQueryWrapper);

        //3.删除标签列表
        LambdaQueryWrapper<ApartmentLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
        labelQueryWrapper.eq(ApartmentLabel::getApartmentId,id);
        apartmentLabelService.remove(labelQueryWrapper);

        //4.删除杂费列表
        LambdaQueryWrapper<ApartmentFeeValue> feeQueryWrapper = new LambdaQueryWrapper<>();
        feeQueryWrapper.eq(ApartmentFeeValue::getApartmentId,id);
        apartmentFeeValueService.remove(feeQueryWrapper);
    }

}





