package com.kami.cloud.lease.web.admin.vo.system.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "员工查询实体")
public class SystemUserQueryVo {

    @Schema(description= "员工姓名")
    private String name;

    @Schema(description= "手机号码")
    private String phone;
}
