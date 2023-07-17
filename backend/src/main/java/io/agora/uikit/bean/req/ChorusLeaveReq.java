package io.agora.uikit.bean.req;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChorusLeaveReq {
    // Room id
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;

    // Song code
    @NotBlank(message = "songCode cannot be empty")
    private String songCode;

    // User id
    @NotBlank(message = "userId cannot be empty")
    private String userId;
}
