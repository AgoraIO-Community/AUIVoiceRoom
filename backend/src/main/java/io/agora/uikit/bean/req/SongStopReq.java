package io.agora.uikit.bean.req;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class SongStopReq {
    // Room id
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;

    // User id
    @NotBlank(message = "userId cannot be empty")
    private String userId;

    // Song code
    @NotBlank(message = "songCode cannot be empty")
    private String songCode;
}
