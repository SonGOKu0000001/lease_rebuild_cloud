package com.kami.cloud.lease.web.admin.mapper;

import com.kami.cloud.lease.model.entity.GraphInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kami.cloud.lease.model.enums.ItemType;
import com.kami.cloud.lease.web.admin.vo.graph.GraphVo;

import java.util.List;

/**
* @author kami
* @description 针对表【graph_info(图片信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.kami.cloud.lease.model.GraphInfo
*/
public interface GraphInfoMapper extends BaseMapper<GraphInfo> {

    List<GraphVo> selectByItemTypeAndId(ItemType itemType, Long id);
}




