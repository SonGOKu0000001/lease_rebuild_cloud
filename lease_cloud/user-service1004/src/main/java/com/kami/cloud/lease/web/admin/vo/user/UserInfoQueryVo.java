package com.kami.cloud.lease.web.admin.vo.user;

import com.kami.cloud.lease.model.enums.BaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户信息查询实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoQueryVo {

    @Schema(description = "用户手机号码")
    private String phone;


    @Schema(description = "用户账号状态")
    private BaseStatus status;
}
