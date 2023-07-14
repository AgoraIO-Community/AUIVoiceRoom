package io.agora.uikit.bean.req;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class NcsPayloadReq {
    // Channel name
    private String channelName;
    // User id
    private Integer uid;
    // Timestamp
    private Integer ts;
    // Account
    private String account;
}
