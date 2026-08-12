package com.kami.cloud.lease.web.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kami.cloud.lease.model.entity.ApartmentInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kami.cloud.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.kami.cloud.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.kami.cloud.lease.web.admin.vo.apartment.ApartmentSubmitVo;

/**
* @author kami
* @description 针对表【apartment_info(公寓信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.kami.cloud.lease.model.ApartmentInfo
*/
public interface ApartmentInfoMapper extends BaseMapper<ApartmentInfo> {


    IPage<ApartmentItemVo> pageItem(IPage<ApartmentItemVo> page, ApartmentQueryVo queryVo);
}




