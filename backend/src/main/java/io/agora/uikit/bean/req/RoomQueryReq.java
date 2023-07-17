package io.agora.uikit.bean.req;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RoomQueryReq {
    // Room id
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;
}
