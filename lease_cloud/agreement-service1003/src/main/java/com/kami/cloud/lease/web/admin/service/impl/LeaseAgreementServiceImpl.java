package com.kami.cloud.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kami.cloud.lease.common.feign.ApartmentFeignApi;
import com.kami.cloud.lease.common.feign.dto.ApartmentInfoDTO;
import com.kami.cloud.lease.common.feign.dto.LeaseTermDTO;
import com.kami.cloud.lease.common.feign.dto.PaymentTypeDTO;
import com.kami.cloud.lease.common.feign.dto.RoomInfoDTO;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.model.entity.LeaseAgreement;
import com.kami.cloud.lease.web.admin.mapper.LeaseAgreementMapper;
import com.kami.cloud.lease.web.admin.service.LeaseAgreementService;
import com.kami.cloud.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.kami.cloud.lease.web.admin.vo.agreement.AgreementVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kami
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {
    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;
    @Autowired
    private ApartmentFeignApi apartmentFeignApi;

    @Override
    public IPage<AgreementVo> pageItem(IPage<AgreementVo> page, AgreementQueryVo queryVo) {
        Set<Long> apartmentIds = resolveApartmentIds(queryVo);
        Set<Long> roomIds = resolveRoomIds(queryVo);
        if (apartmentIds != null && apartmentIds.isEmpty()) {
            return page;
        }
        if (roomIds != null && roomIds.isEmpty()) {
            return page;
        }

        LambdaQueryWrapper<LeaseAgreement> wrapper = new LambdaQueryWrapper<>();
        if (queryVo.getApartmentId() != null) {
            wrapper.eq(LeaseAgreement::getApartmentId, queryVo.getApartmentId());
        } else if (!CollectionUtils.isEmpty(apartmentIds)) {
            wrapper.in(LeaseAgreement::getApartmentId, apartmentIds);
        }
        if (!CollectionUtils.isEmpty(roomIds)) {
            wrapper.in(LeaseAgreement::getRoomId, roomIds);
        }
        if (StringUtils.hasText(queryVo.getName())) {
            wrapper.like(LeaseAgreement::getName, queryVo.getName());
        }
        if (StringUtils.hasText(queryVo.getPhone())) {
            wrapper.like(LeaseAgreement::getPhone, queryVo.getPhone());
        }

        Page<LeaseAgreement> entityPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        leaseAgreementMapper.selectPage(entityPage, wrapper);

        List<AgreementVo> records = convert(entityPage.getRecords());
        page.setRecords(records);
        page.setTotal(entityPage.getTotal());
        page.setPages(entityPage.getPages());
        return page;
    }

    @Override
    public AgreementVo getAgreementVoById(Long id) {
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
        if (leaseAgreement == null) {
            return null;
        }
        List<AgreementVo> vos = convert(Collections.singletonList(leaseAgreement));
        return vos.isEmpty() ? null : vos.get(0);
    }

    private Set<Long> resolveApartmentIds(AgreementQueryVo queryVo) {
        if (queryVo.getProvinceId() == null && queryVo.getCityId() == null && queryVo.getDistrictId() == null) {
            return null;
        }
        Result<List<Long>> result = apartmentFeignApi.apartmentIdsByRegion(
                queryVo.getProvinceId(), queryVo.getCityId(), queryVo.getDistrictId());
        List<Long> ids = result == null ? null : result.getData();
        return CollectionUtils.isEmpty(ids) ? Collections.emptySet() : new HashSet<>(ids);
    }

    private Set<Long> resolveRoomIds(AgreementQueryVo queryVo) {
        if (!StringUtils.hasText(queryVo.getRoomNumber())) {
            return null;
        }
        Result<List<Long>> result = apartmentFeignApi.roomIdsByNumber(queryVo.getRoomNumber());
        List<Long> ids = result == null ? null : result.getData();
        return CollectionUtils.isEmpty(ids) ? Collections.emptySet() : new HashSet<>(ids);
    }

    private List<AgreementVo> convert(List<LeaseAgreement> agreements) {
        if (CollectionUtils.isEmpty(agreements)) {
            return Collections.emptyList();
        }
        Set<Long> apartmentIds = agreements.stream().map(LeaseAgreement::getApartmentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> roomIds = agreements.stream().map(LeaseAgreement::getRoomId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> paymentTypeIds = agreements.stream().map(LeaseAgreement::getPaymentTypeId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> leaseTermIds = agreements.stream().map(LeaseAgreement::getLeaseTermId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, ApartmentInfoDTO> apartmentMap = fetchByIds(apartmentIds, ids -> apartmentFeignApi.apartmentByIds(new ArrayList<>(ids))).stream()
                .collect(Collectors.toMap(ApartmentInfoDTO::getId, Function.identity()));
        Map<Long, RoomInfoDTO> roomMap = fetchByIds(roomIds, ids -> apartmentFeignApi.roomByIds(new ArrayList<>(ids))).stream()
                .collect(Collectors.toMap(RoomInfoDTO::getId, Function.identity()));
        Map<Long, PaymentTypeDTO> paymentTypeMap = fetchByIds(paymentTypeIds, ids -> apartmentFeignApi.paymentTypeByIds(new ArrayList<>(ids))).stream()
                .collect(Collectors.toMap(PaymentTypeDTO::getId, Function.identity()));
        Map<Long, LeaseTermDTO> leaseTermMap = fetchByIds(leaseTermIds, ids -> apartmentFeignApi.leaseTermByIds(new ArrayList<>(ids))).stream()
                .collect(Collectors.toMap(LeaseTermDTO::getId, Function.identity()));

        return agreements.stream().map(agreement -> {
            AgreementVo vo = new AgreementVo();
            BeanUtils.copyProperties(agreement, vo);
            vo.setApartmentInfo(apartmentMap.get(agreement.getApartmentId()));
            vo.setRoomInfo(roomMap.get(agreement.getRoomId()));
            vo.setPaymentType(paymentTypeMap.get(agreement.getPaymentTypeId()));
            vo.setLeaseTerm(leaseTermMap.get(agreement.getLeaseTermId()));
            return vo;
        }).collect(Collectors.toList());
    }

    private <T> List<T> fetchByIds(Set<Long> ids, Function<List<Long>, Result<List<T>>> fetcher) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        Result<List<T>> result = fetcher.apply(new ArrayList<>(ids));
        return result == null || result.getData() == null ? Collections.emptyList() : result.getData();
    }
}