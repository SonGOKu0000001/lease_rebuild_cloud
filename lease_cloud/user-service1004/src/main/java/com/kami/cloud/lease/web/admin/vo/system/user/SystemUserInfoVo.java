package com.kami.cloud.lease.web.admin.vo.system.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "员工基本信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemUserInfoVo {

    @Schema(description = "用户姓名")
    private String name;

    @Schema(description = "用户头像")
    private String avatarUrl;

}
