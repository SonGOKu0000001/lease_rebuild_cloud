package com.kami.cloud.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kami.cloud.lease.model.entity.RoomPaymentType;
import com.kami.cloud.lease.web.app.service.RoomPaymentTypeService;
import com.kami.cloud.lease.web.app.mapper.RoomPaymentTypeMapper;
import org.springframework.stereotype.Service;

/**
* @author kami
* @description 针对表【room_payment_type(房间&支付方式关联表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class RoomPaymentTypeServiceImpl extends ServiceImpl<RoomPaymentTypeMapper, RoomPaymentType>
    implements RoomPaymentTypeService{

}




