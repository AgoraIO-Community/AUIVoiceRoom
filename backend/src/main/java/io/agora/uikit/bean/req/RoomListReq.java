package io.agora.uikit.bean.req;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RoomListReq {
    private Long lastCreateTime;

    // Page size
    @Max(value = 50, message = "pageSize must be less than or equal to 50")
    @Min(value = 1, message = "pageSize must be greater than 0")
    private int pageSize;
}
