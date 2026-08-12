package com.kami.cloud.lease.web.admin.vo.system.user;

import com.kami.cloud.lease.model.entity.SystemUser;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data

@Schema(description = "后台管理系统用户基本信息实体")
public class SystemUserItemVo extends SystemUser {

    @Schema(description = "岗位名称")
    @TableField(value = "post_name")
    private String postName;

}
