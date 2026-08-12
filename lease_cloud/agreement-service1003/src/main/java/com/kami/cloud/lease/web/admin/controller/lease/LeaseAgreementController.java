package com.kami.cloud.lease.web.admin.controller.lease;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kami.cloud.lease.common.exception.LeaseException;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.common.result.ResultCodeEnum;
import com.kami.cloud.lease.model.entity.LeaseAgreement;
import com.kami.cloud.lease.model.enums.LeaseStatus;
import com.kami.cloud.lease.web.admin.service.LeaseAgreementService;
import com.kami.cloud.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.kami.cloud.lease.web.admin.vo.agreement.AgreementVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Tag(name = "租约管理")
@RestController
@RequestMapping("/admin/agreement")
public class LeaseAgreementController {
    @Autowired
    private LeaseAgreementService service;

    @Operation(summary = "保存或修改租约信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody LeaseAgreement leaseAgreement) {
        service.saveOrUpdate(leaseAgreement);
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询租约列表")
    @GetMapping("page")
    public Result<IPage<AgreementVo>> page(@RequestParam long current, @RequestParam long size, AgreementQueryVo queryVo) {
        IPage<AgreementVo> page = new Page<>(current, size);
        IPage<AgreementVo> pageItem = service.pageItem(page, queryVo);
        return Result.ok(pageItem);
    }

    @Operation(summary = "根据id查询租约信息")
    @GetMapping(name = "getById")
    public Result<AgreementVo> getById(@RequestParam Long id) {
        AgreementVo result = service.getAgreementVoById(id);
        if (result == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_LEASEAGREEMENT_NOT_EXIST);
        }
        return Result.ok(result);
    }

    @Operation(summary = "根据id删除租约信息")
    @DeleteMapping("removeById")
    public Result removeById(@RequestParam Long id) {
        if(service.getById(id) == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_LEASEAGREEMENT_NOT_EXIST);
        }
        service.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "根据id更新租约状态")
    @PostMapping("updateStatusById")
    public Result updateStatusById(@RequestParam Long id, @RequestParam LeaseStatus status) {
        if(service.getById(id) == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_LEASEAGREEMENT_NOT_EXIST);
        }
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LeaseAgreement::getId, id);
        updateWrapper.set(LeaseAgreement::getStatus, status);
        service.update(updateWrapper);
        return Result.ok();
    }

}

