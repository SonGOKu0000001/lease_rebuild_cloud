package com.kami.cloud.lease.web.admin.controller.lease;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kami.cloud.lease.common.exception.LeaseException;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.common.result.ResultCodeEnum;
import com.kami.cloud.lease.model.entity.ViewAppointment;
import com.kami.cloud.lease.model.enums.AppointmentStatus;
import com.kami.cloud.lease.web.admin.service.ViewAppointmentService;
import com.kami.cloud.lease.web.admin.vo.appointment.AppointmentQueryVo;
import com.kami.cloud.lease.web.admin.vo.appointment.AppointmentVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Tag(name = "预约看房管理")
@RequestMapping("/admin/appointment")
@RestController
public class ViewAppointmentController {
    @Autowired
    private ViewAppointmentService service;

    @Operation(summary = "分页查询预约信息")
    @GetMapping("page")
    public Result<IPage<AppointmentVo>> page(@RequestParam long current, @RequestParam long size, AppointmentQueryVo queryVo) {
        IPage<AppointmentVo> page = new Page<>(current, size);
        IPage<AppointmentVo> pageInfo = service.pageItem(page, queryVo);
        return Result.ok(pageInfo);
    }

    @Operation(summary = "根据id更新预约状态")
    @PostMapping("updateStatusById")
    public Result updateStatusById(@RequestParam Long id, @RequestParam AppointmentStatus status) {
        LambdaQueryWrapper<ViewAppointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ViewAppointment::getId, id);
        queryWrapper.eq(ViewAppointment::getIsDeleted, 0);
        // count > 0 说明这条记录存在
        long count = service.count(queryWrapper);
        if (count > 0) {
            // 有这条id，可以执行更新
            LambdaUpdateWrapper<ViewAppointment> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(ViewAppointment::getId, id);
            lambdaUpdateWrapper.eq(ViewAppointment::getIsDeleted, 0);
            lambdaUpdateWrapper.set(ViewAppointment::getAppointmentStatus, status);
            service.update(lambdaUpdateWrapper);
            return Result.ok();
        } else {
            // id不存在，抛出异常/返回提示
            throw new LeaseException(ResultCodeEnum.ADMIN_VIEW_NOT_EXIST);
        }
    }

}
