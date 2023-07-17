package io.agora.uikit.bean.req;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class NcsReq {
    // Event type
    private Integer eventType;
    // Notice id
    private String noticeId;
    // Notify million second
    private Long notifyMs;
    // Payload
    private NcsPayloadReq payload;
    // Product id
    private Integer productId;
    // Sid
    private String sid;
}
