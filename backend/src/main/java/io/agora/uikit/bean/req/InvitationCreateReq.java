package io.agora.uikit.bean.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class InvitationCreateReq {
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;

    @NotBlank(message = "fromUserId cannot be empty")
    private String fromUserId;

    @NotBlank(message = "toUserId cannot be empty")
    private String toUserId;

    @NotNull(message = "payload cannot be null")
    private MicSeatPayload payload;
}
