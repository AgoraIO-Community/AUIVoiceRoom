package io.agora.uikit.bean.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ApplicationRemoveReq {
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;

    @NotBlank(message = "fromUserId cannot be empty")
    private String fromUserId;

    @NotBlank(message = "toUserId cannot be empty")
    private String toUserId;
}
