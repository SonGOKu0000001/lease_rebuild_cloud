package com.kami.cloud.lease.web.inner.mapper;

import com.kami.cloud.lease.common.feign.dto.GraphDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公寓服务内部查询 Mapper（仅服务内 Feign 调用，不对外暴露）
 */
public interface InnerQueryMapper {

    List<GraphDTO> selectGraphsByItemTypeAndIds(@Param("itemType") Integer itemType,
                                                 @Param("ids") List<Long> ids);

    List<Long> selectApartmentIdsByRegion(@Param("provinceId") Long provinceId,
                                          @Param("cityId") Long cityId,
                                          @Param("districtId") Long districtId);

    List<Long> selectRoomIdsByNumber(@Param("roomNumber") String roomNumber);
}