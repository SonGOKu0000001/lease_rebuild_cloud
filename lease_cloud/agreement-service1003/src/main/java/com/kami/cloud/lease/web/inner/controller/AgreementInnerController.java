package com.kami.cloud.lease.web.inner.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kami.cloud.lease.common.feign.dto.ApartmentRentCountDTO;
import com.kami.cloud.lease.common.feign.dto.RoomRentInfoDTO;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.model.entity.LeaseAgreement;
import com.kami.cloud.lease.model.enums.LeaseStatus;
import com.kami.cloud.lease.web.admin.mapper.LeaseAgreementMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租约服务内部接口（仅供其他服务 Feign 调用）
 */
@Tag(name = "内部接口")
@RestController
@RequestMapping("/inner")
public class AgreementInnerController {

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;

    @GetMapping("/agreement/renting-count-by-apartment")
    public Result<List<ApartmentRentCountDTO>> rentingCountByApartment(@RequestParam("ids") List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.ok(Collections.emptyList());
        }
        List<LeaseAgreement> list = leaseAgreementMapper.selectList(
                new LambdaQueryWrapper<LeaseAgreement>()
                        .in(LeaseAgreement::getApartmentId, ids)
                        .in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING));
        if (CollectionUtils.isEmpty(list)) {
            return Result.ok(Collections.emptyList());
        }
        Map<Long, Long> countMap = list.stream()
                .collect(Collectors.groupingBy(LeaseAgreement::getApartmentId, Collectors.counting()));
        List<ApartmentRentCountDTO> result = countMap.entrySet().stream()
                .map(entry -> new ApartmentRentCountDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return Result.ok(result);
    }

    @GetMapping("/agreement/renting-info-by-room")
    public Result<List<RoomRentInfoDTO>> rentingInfoByRoom(@RequestParam("ids") List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.ok(Collections.emptyList());
        }
        List<LeaseAgreement> list = leaseAgreementMapper.selectList(
                new LambdaQueryWrapper<LeaseAgreement>()
                        .in(LeaseAgreement::getRoomId, ids)
                        .in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING));
        if (CollectionUtils.isEmpty(list)) {
            return Result.ok(Collections.emptyList());
        }
        List<RoomRentInfoDTO> result = list.stream()
                .collect(Collectors.toMap(LeaseAgreement::getRoomId, leaseAgreement -> new RoomRentInfoDTO(
                        leaseAgreement.getRoomId(), true, leaseAgreement.getLeaseEndDate()),
                        (dto1, dto2) -> dto2))
                .values().stream().collect(Collectors.toList());
        return Result.ok(result);
    }
}
