package com.kami.cloud.lease.web.admin.service;

import com.kami.cloud.lease.model.entity.FeeKey;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kami.cloud.lease.web.admin.vo.fee.FeeKeyVo;

import java.util.List;

/**
* @author kami
* @description 针对表【fee_key(杂项费用名称表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface FeeKeyService extends IService<FeeKey> {

    List<FeeKeyVo> listFeeInfo();
}
