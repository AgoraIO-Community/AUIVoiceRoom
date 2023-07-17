package io.agora.uikit.bean.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class UserKickOutReq {
    @NotBlank(message = "operatorId cannot be empty")
    private String operatorId;
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;
    @NotNull(message = "uid cannot be empty")
    private Long uid;
}
