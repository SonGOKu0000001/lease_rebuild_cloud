package com.kami.cloud.lease.web.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kami.cloud.lease.model.entity.ViewAppointment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kami.cloud.lease.web.admin.vo.appointment.AppointmentQueryVo;
import com.kami.cloud.lease.web.admin.vo.appointment.AppointmentVo;

/**
* @author kami
* @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface ViewAppointmentService extends IService<ViewAppointment> {

    IPage<AppointmentVo> pageItem(IPage<AppointmentVo> page, AppointmentQueryVo queryVo);
}
