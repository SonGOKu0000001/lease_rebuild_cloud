package com.kami.cloud.lease.web.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kami.cloud.lease.model.entity.SystemUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kami.cloud.lease.web.admin.vo.system.user.SystemUserItemVo;
import com.kami.cloud.lease.web.admin.vo.system.user.SystemUserQueryVo;

/**
* @author kami
* @description 针对表【system_user(员工信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.kami.cloud.lease.model.SystemUser
*/
public interface SystemUserMapper extends BaseMapper<SystemUser> {

    IPage<SystemUserItemVo> pageSystemUserItemVo(IPage<SystemUserItemVo> page, SystemUserQueryVo queryVo);

    SystemUserItemVo getSystemUserItemVoById(Long id);

    SystemUser selectOneByUsername(String username);
}




