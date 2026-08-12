package com.kami.cloud.lease.web.admin.vo.agreement;

import com.kami.cloud.lease.common.feign.dto.ApartmentInfoDTO;
import com.kami.cloud.lease.common.feign.dto.LeaseTermDTO;
import com.kami.cloud.lease.common.feign.dto.PaymentTypeDTO;
import com.kami.cloud.lease.common.feign.dto.RoomInfoDTO;
import com.kami.cloud.lease.model.entity.LeaseAgreement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data

@Schema(description = "租约信息")
public class AgreementVo extends LeaseAgreement {

    @Schema(description = "签约公寓信息")
    private ApartmentInfoDTO apartmentInfo;

    @Schema(description = "签约房间信息")
    private RoomInfoDTO roomInfo;

    @Schema(description = "支付方式")
    private PaymentTypeDTO paymentType;

    @Schema(description = "租期")
    private LeaseTermDTO leaseTerm;
}