package com.kami.cloud.lease.web.app.mapper;

import com.kami.cloud.lease.model.entity.GraphInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kami.cloud.lease.model.enums.ItemType;
import com.kami.cloud.lease.web.app.vo.graph.GraphVo;

import java.util.List;

/**
* @author kami
* @description 针对表【graph_info(图片信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.kami.cloud.lease.model.entity.GraphInfo
*/
public interface GraphInfoMapper extends BaseMapper<GraphInfo> {
    List<GraphVo> selectByItemTypeAndId(ItemType itemType, Long id);
}




