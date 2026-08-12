package com.kami.cloud.lease.common.feign;

import com.kami.cloud.lease.common.feign.dto.ApartmentInfoDTO;
import com.kami.cloud.lease.common.feign.dto.ApartmentItemDTO;
import com.kami.cloud.lease.common.feign.dto.GraphDTO;
import com.kami.cloud.lease.common.feign.dto.LeaseTermDTO;
import com.kami.cloud.lease.common.feign.dto.PaymentTypeDTO;
import com.kami.cloud.lease.common.feign.dto.RoomInfoDTO;
import com.kami.cloud.lease.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 公寓服务内部 Feign API（不走网关）
 */
@FeignClient(name = "lease-apartment-service", contextId = "apartmentInnerApi")
public interface ApartmentFeignApi {

    @GetMapping("/inner/apartment/by-ids")
    Result<List<ApartmentInfoDTO>> apartmentByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/inner/apartment/item")
    Result<ApartmentItemDTO> apartmentItemById(@RequestParam("id") Long id);

    @GetMapping("/inner/apartment/list-ids-by-region")
    Result<List<Long>> apartmentIdsByRegion(@RequestParam(value = "provinceId", required = false) Long provinceId,
                                            @RequestParam(value = "cityId", required = false) Long cityId,
                                            @RequestParam(value = "districtId", required = false) Long districtId);

    @GetMapping("/inner/room/by-ids")
    Result<List<RoomInfoDTO>> roomByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/inner/room/list-ids-by-number")
    Result<List<Long>> roomIdsByNumber(@RequestParam("roomNumber") String roomNumber);

    @GetMapping("/inner/payment-type/by-ids")
    Result<List<PaymentTypeDTO>> paymentTypeByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/inner/lease-term/by-ids")
    Result<List<LeaseTermDTO>> leaseTermByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/inner/graph/list")
    Result<List<GraphDTO>> graphList(@RequestParam("itemType") Integer itemType,
                                     @RequestParam("ids") List<Long> ids);
}