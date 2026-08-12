package com.kami.cloud.lease.web.inner.controller;

import com.kami.cloud.lease.common.feign.dto.ApartmentInfoDTO;
import com.kami.cloud.lease.common.feign.dto.ApartmentItemDTO;
import com.kami.cloud.lease.common.feign.dto.GraphDTO;
import com.kami.cloud.lease.common.feign.dto.LabelInfoDTO;
import com.kami.cloud.lease.common.feign.dto.LeaseTermDTO;
import com.kami.cloud.lease.common.feign.dto.PaymentTypeDTO;
import com.kami.cloud.lease.common.feign.dto.RoomInfoDTO;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.model.entity.ApartmentInfo;
import com.kami.cloud.lease.model.entity.LabelInfo;
import com.kami.cloud.lease.model.entity.LeaseTerm;
import com.kami.cloud.lease.model.entity.PaymentType;
import com.kami.cloud.lease.model.entity.RoomInfo;
import com.kami.cloud.lease.model.enums.ItemType;
import com.kami.cloud.lease.web.admin.mapper.ApartmentInfoMapper;
import com.kami.cloud.lease.web.admin.mapper.LeaseTermMapper;
import com.kami.cloud.lease.web.admin.mapper.PaymentTypeMapper;
import com.kami.cloud.lease.web.inner.mapper.InnerQueryMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公寓服务内部接口（仅供其他服务 Feign 调用）
 */
@Tag(name = "内部接口")
@RestController
@RequestMapping("/inner")
public class InnerController {

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private com.kami.cloud.lease.web.app.mapper.RoomInfoMapper roomInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private com.kami.cloud.lease.web.app.mapper.LabelInfoMapper labelInfoMapper;
    @Autowired
    private InnerQueryMapper innerQueryMapper;

    @GetMapping("/apartment/by-ids")
    public Result<List<ApartmentInfoDTO>> apartmentByIds(@RequestParam("ids") List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.ok(Collections.emptyList());
        }
        List<ApartmentInfo> list = apartmentInfoMapper.selectBatchIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            return Result.ok(Collections.emptyList());
        }
        List<ApartmentInfoDTO> result = list.stream().map(entity -> {
            ApartmentInfoDTO dto = new ApartmentInfoDTO();
            BeanUtils.copyProperties(entity, dto);
            dto.setIsRelease(entity.getIsRelease() == null ? null : entity.getIsRelease().getCode());
            return dto;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    @GetMapping("/apartment/item")
    public Result<ApartmentItemDTO> apartmentItemById(@RequestParam("id") Long id) {
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        if (apartmentInfo == null) {
            return Result.ok();
        }
        ApartmentItemDTO dto = new ApartmentItemDTO();
        BeanUtils.copyProperties(apartmentInfo, dto);
        dto.setIsRelease(apartmentInfo.getIsRelease() == null ? null : apartmentInfo.getIsRelease().getCode());

        List<LabelInfo> labelInfos = labelInfoMapper.selectListByApartmentId(id);
        if (!CollectionUtils.isEmpty(labelInfos)) {
            List<LabelInfoDTO> labels = labelInfos.stream()
                    .filter(label -> label.getType() != null)
                    .map(label -> new LabelInfoDTO(label.getId(), label.getName(),
                            String.valueOf(label.getType().getCode())))
                    .collect(Collectors.toList());
            dto.setLabelInfoList(labels);
        }

        List<GraphDTO> graphs = innerQueryMapper.selectGraphsByItemTypeAndIds(
                ItemType.APARTMENT.getCode(), Collections.singletonList(id));
        dto.setGraphVoList(graphs);

        BigDecimal minRent = roomInfoMapper.selectMinRentByApartmentId(id);
        dto.setMinRent(minRent);
        return Result.ok(dto);
    }

    @GetMapping("/apartment/list-ids-by-region")
    public Result<List<Long>> apartmentIdsByRegion(@RequestParam(value = "provinceId", required = false) Long provinceId,
                                                   @RequestParam(value = "cityId", required = false) Long cityId,
                                                   @RequestParam(value = "districtId", required = false) Long districtId) {
        if (provinceId == null && cityId == null && districtId == null) {
            return Result.ok(null);
        }
        List<Long> ids = innerQueryMapper.selectApartmentIdsByRegion(provinceId, cityId, districtId);
        return Result.ok(ids);
    }

    @GetMapping("/room/by-ids")
    public Result<List<RoomInfoDTO>> roomByIds(@RequestParam("ids") List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.ok(Collections.emptyList());
        }
        List<RoomInfo> list = roomInfoMapper.selectBatchIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            return Result.ok(Collections.emptyList());
        }
        List<RoomInfoDTO> result = list.stream().map(entity -> {
            RoomInfoDTO dto = new RoomInfoDTO();
            BeanUtils.copyProperties(entity, dto);
            dto.setIsRelease(entity.getIsRelease() == null ? null : entity.getIsRelease().getCode());
            return dto;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    @GetMapping("/room/list-ids-by-number")
    public Result<List<Long>> roomIdsByNumber(@RequestParam("roomNumber") String roomNumber) {
        if (!StringUtils.hasText(roomNumber)) {
            return Result.ok(null);
        }
        List<Long> ids = innerQueryMapper.selectRoomIdsByNumber(roomNumber);
        return Result.ok(ids);
    }

    @GetMapping("/payment-type/by-ids")
    public Result<List<PaymentTypeDTO>> paymentTypeByIds(@RequestParam("ids") List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.ok(Collections.emptyList());
        }
        List<PaymentType> list = paymentTypeMapper.selectBatchIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            return Result.ok(Collections.emptyList());
        }
        List<PaymentTypeDTO> result = list.stream().map(entity -> {
            PaymentTypeDTO dto = new PaymentTypeDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    @GetMapping("/lease-term/by-ids")
    public Result<List<LeaseTermDTO>> leaseTermByIds(@RequestParam("ids") List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.ok(Collections.emptyList());
        }
        List<LeaseTerm> list = leaseTermMapper.selectBatchIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            return Result.ok(Collections.emptyList());
        }
        List<LeaseTermDTO> result = list.stream().map(entity -> {
            LeaseTermDTO dto = new LeaseTermDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    @GetMapping("/graph/list")
    public Result<List<GraphDTO>> graphList(@RequestParam("itemType") Integer itemType,
                                            @RequestParam("ids") List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Result.ok(Collections.emptyList());
        }
        List<GraphDTO> graphs = innerQueryMapper.selectGraphsByItemTypeAndIds(itemType, ids);
        return Result.ok(graphs);
    }
}