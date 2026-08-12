package com.kami.cloud.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kami.cloud.lease.common.feign.ApartmentFeignApi;
import com.kami.cloud.lease.common.feign.dto.ApartmentInfoDTO;
import com.kami.cloud.lease.common.feign.dto.GraphDTO;
import com.kami.cloud.lease.common.feign.dto.LeaseTermDTO;
import com.kami.cloud.lease.common.feign.dto.PaymentTypeDTO;
import com.kami.cloud.lease.common.feign.dto.RoomInfoDTO;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.model.entity.LeaseAgreement;
import com.kami.cloud.lease.model.enums.ItemType;
import com.kami.cloud.lease.web.app.mapper.LeaseAgreementMapper;
import com.kami.cloud.lease.web.app.service.LeaseAgreementService;
import com.kami.cloud.lease.web.app.vo.agreement.AgreementDetailVo;
import com.kami.cloud.lease.web.app.vo.agreement.AgreementItemVo;
import com.kami.cloud.lease.web.app.vo.graph.GraphVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kami
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {
    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;
    @Autowired
    private ApartmentFeignApi apartmentFeignApi;

    @Override
    public List<AgreementItemVo> listItemByUsername(String username) {
        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseAgreement::getPhone, username);
        List<LeaseAgreement> agreements = leaseAgreementMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(agreements)) {
            return Collections.emptyList();
        }

        Set<Long> roomIds = agreements.stream().map(LeaseAgreement::getRoomId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> apartmentIds = agreements.stream().map(LeaseAgreement::getApartmentId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, RoomInfoDTO> roomMap = roomByIds(roomIds);
        Map<Long, ApartmentInfoDTO> apartmentMap = apartmentByIds(apartmentIds);
        Map<Long, List<GraphDTO>> roomGraphMap = graphMap(ItemType.ROOM, roomIds);

        return agreements.stream().map(agreement -> {
            AgreementItemVo itemVo = new AgreementItemVo();
            itemVo.setId(agreement.getId());
            itemVo.setRent(agreement.getRent());
            itemVo.setLeaseStartDate(agreement.getLeaseStartDate());
            itemVo.setLeaseEndDate(agreement.getLeaseEndDate());
            itemVo.setLeaseStatus(agreement.getStatus());
            itemVo.setSourceType(agreement.getSourceType());
            RoomInfoDTO roomInfo = roomMap.get(agreement.getRoomId());
            if (roomInfo != null) {
                itemVo.setRoomNumber(roomInfo.getRoomNumber());
                ApartmentInfoDTO apartmentInfo = apartmentMap.get(roomInfo.getApartmentId());
                if (apartmentInfo != null) {
                    itemVo.setApartmentName(apartmentInfo.getName());
                }
            }
            itemVo.setRoomGraphVoList(convertGraphs(roomGraphMap.get(agreement.getRoomId())));
            return itemVo;
        }).collect(Collectors.toList());
    }

    @Override
    public AgreementDetailVo getDetailById(Long id) {
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
        if (leaseAgreement == null) {
            return null;
        }

        Set<Long> roomIds = Collections.singleton(leaseAgreement.getRoomId());
        Set<Long> apartmentIds = Collections.singleton(leaseAgreement.getApartmentId());
        Map<Long, RoomInfoDTO> roomMap = roomByIds(roomIds);
        Map<Long, ApartmentInfoDTO> apartmentMap = apartmentByIds(apartmentIds);
        Map<Long, List<GraphDTO>> roomGraphMap = graphMap(ItemType.ROOM, roomIds);
        Map<Long, List<GraphDTO>> apartmentGraphMap = graphMap(ItemType.APARTMENT, apartmentIds);

        PaymentTypeDTO paymentType = paymentType(leaseAgreement.getPaymentTypeId());
        LeaseTermDTO leaseTerm = leaseTerm(leaseAgreement.getLeaseTermId());

        AgreementDetailVo detailVo = new AgreementDetailVo();
        BeanUtils.copyProperties(leaseAgreement, detailVo);
        ApartmentInfoDTO apartmentInfo = apartmentMap.get(leaseAgreement.getApartmentId());
        detailVo.setApartmentName(apartmentInfo == null ? null : apartmentInfo.getName());
        RoomInfoDTO roomInfo = roomMap.get(leaseAgreement.getRoomId());
        detailVo.setRoomNumber(roomInfo == null ? null : roomInfo.getRoomNumber());
        detailVo.setApartmentGraphVoList(convertGraphs(apartmentGraphMap.get(leaseAgreement.getApartmentId())));
        detailVo.setRoomGraphVoList(convertGraphs(roomGraphMap.get(leaseAgreement.getRoomId())));
        detailVo.setPaymentTypeName(paymentType == null ? null : paymentType.getName());
        detailVo.setLeaseTermMonthCount(leaseTerm == null ? null : leaseTerm.getMonthCount());
        detailVo.setLeaseTermUnit(leaseTerm == null ? null : leaseTerm.getUnit());
        return detailVo;
    }

    private Map<Long, RoomInfoDTO> roomByIds(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Result<List<RoomInfoDTO>> result = apartmentFeignApi.roomByIds(new ArrayList<>(ids));
        List<RoomInfoDTO> list = result == null ? null : result.getData();
        return toMap(list, RoomInfoDTO::getId);
    }

    private Map<Long, ApartmentInfoDTO> apartmentByIds(Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Result<List<ApartmentInfoDTO>> result = apartmentFeignApi.apartmentByIds(new ArrayList<>(ids));
        List<ApartmentInfoDTO> list = result == null ? null : result.getData();
        return toMap(list, ApartmentInfoDTO::getId);
    }

    private Map<Long, List<GraphDTO>> graphMap(ItemType itemType, Set<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Result<List<GraphDTO>> result = apartmentFeignApi.graphList(itemType.getCode(), new ArrayList<>(ids));
        List<GraphDTO> list = result == null ? null : result.getData();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().filter(g -> g.getItemId() != null)
                .collect(Collectors.groupingBy(GraphDTO::getItemId));
    }

    private PaymentTypeDTO paymentType(Long id) {
        if (id == null) {
            return null;
        }
        Result<List<PaymentTypeDTO>> result = apartmentFeignApi.paymentTypeByIds(Collections.singletonList(id));
        List<PaymentTypeDTO> list = result == null ? null : result.getData();
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    private LeaseTermDTO leaseTerm(Long id) {
        if (id == null) {
            return null;
        }
        Result<List<LeaseTermDTO>> result = apartmentFeignApi.leaseTermByIds(Collections.singletonList(id));
        List<LeaseTermDTO> list = result == null ? null : result.getData();
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    private <T> Map<Long, T> toMap(List<T> list, Function<T, Long> keyExtractor) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.toMap(keyExtractor, Function.identity()));
    }

    private List<GraphVo> convertGraphs(List<GraphDTO> graphs) {
        if (CollectionUtils.isEmpty(graphs)) {
            return Collections.emptyList();
        }
        return graphs.stream().map(graph -> GraphVo.builder()
                .name(graph.getName()).url(graph.getUrl()).build()).collect(Collectors.toList());
    }
}