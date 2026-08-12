package com.kami.cloud.lease.web.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kami.cloud.lease.model.entity.RoomInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kami.cloud.lease.web.admin.vo.room.RoomItemVo;
import com.kami.cloud.lease.web.admin.vo.room.RoomQueryVo;

/**
* @author kami
* @description 针对表【room_info(房间信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.kami.cloud.lease.model.RoomInfo
*/
public interface RoomInfoMapper extends BaseMapper<RoomInfo> {

    IPage<RoomItemVo> pageItem(IPage<RoomItemVo> page, RoomQueryVo queryVo);
}




