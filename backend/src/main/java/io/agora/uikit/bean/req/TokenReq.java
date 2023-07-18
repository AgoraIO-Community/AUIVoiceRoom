package io.agora.uikit.bean.req;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class TokenReq {
    // Channel name
    @NotBlank(message = "channelName cannot be empty")
    private String channelName;

    // User id
    @NotBlank(message = "userId cannot be empty")
    private String userId;
}
