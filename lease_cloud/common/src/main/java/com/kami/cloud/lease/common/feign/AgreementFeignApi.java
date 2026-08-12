package com.kami.cloud.lease.common.feign;

import com.kami.cloud.lease.common.feign.dto.ApartmentRentCountDTO;
import com.kami.cloud.lease.common.feign.dto.RoomRentInfoDTO;
import com.kami.cloud.lease.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 租约服务内部 Feign API（不走网关，供公寓服务调用，统计在租数据）
 */
@FeignClient(name = "lease-agreement-service", contextId = "agreementInnerApi")
public interface AgreementFeignApi {

    @GetMapping("/inner/agreement/renting-count-by-apartment")
    Result<List<ApartmentRentCountDTO>> rentingCountByApartment(@RequestParam("ids") List<Long> ids);

    @GetMapping("/inner/agreement/renting-info-by-room")
    Result<List<RoomRentInfoDTO>> rentingInfoByRoom(@RequestParam("ids") List<Long> ids);
}
