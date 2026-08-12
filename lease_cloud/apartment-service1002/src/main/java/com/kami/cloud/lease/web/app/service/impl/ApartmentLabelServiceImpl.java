package com.kami.cloud.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kami.cloud.lease.model.entity.ApartmentLabel;
import com.kami.cloud.lease.web.app.service.ApartmentLabelService;
import com.kami.cloud.lease.web.app.mapper.ApartmentLabelMapper;
import org.springframework.stereotype.Service;

/**
* @author kami
* @description 针对表【apartment_label(公寓标签关联表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class ApartmentLabelServiceImpl extends ServiceImpl<ApartmentLabelMapper, ApartmentLabel>
    implements ApartmentLabelService{
}
