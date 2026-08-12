package com.kami.cloud.lease.web.app.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kami.cloud.lease.model.entity.RoomInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kami.cloud.lease.web.app.vo.room.RoomItemVo;
import com.kami.cloud.lease.web.app.vo.room.RoomQueryVo;

import java.math.BigDecimal;

/**
* @author kami
* @description 针对表【room_info(房间信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.kami.cloud.lease.model.entity.RoomInfo
*/
public interface RoomInfoMapper extends BaseMapper<RoomInfo> {

    IPage<RoomInfo> pageWithRoomQueryVo(Page<RoomInfo> roomPage, RoomQueryVo queryVo);

    BigDecimal selectMinRentByApartmentId(Long id);

    IPage<RoomInfo> pageWithApartmentId(Page<RoomInfo> roomPage, Long id);
}