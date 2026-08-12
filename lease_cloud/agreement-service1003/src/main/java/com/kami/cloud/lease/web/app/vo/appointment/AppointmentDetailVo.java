package com.kami.cloud.lease.web.app.vo.appointment;

import com.kami.cloud.lease.common.feign.dto.ApartmentItemDTO;
import com.kami.cloud.lease.model.entity.ViewAppointment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "APP端预约看房信息")
public class AppointmentDetailVo extends ViewAppointment {

    @Schema(description = "预约公寓详细信息")
    private ApartmentItemDTO apartmentItemVo;
}