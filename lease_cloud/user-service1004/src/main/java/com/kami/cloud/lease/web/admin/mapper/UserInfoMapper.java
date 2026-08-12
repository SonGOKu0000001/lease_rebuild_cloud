package com.kami.cloud.lease.web.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kami.cloud.lease.model.entity.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kami.cloud.lease.web.admin.vo.user.UserInfoQueryVo;

/**
* @author kami
* @description 针对表【user_info(用户信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.kami.cloud.lease.model.UserInfo
*/
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    IPage<UserInfo> pageUserInfo(IPage<UserInfo> page, UserInfoQueryVo queryVo);
}




