package io.agora.uikit.bean.process;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;

@Data
@Accessors(chain = true)
public class ProcessReq {
    private String fromUserId;

    private String toUserId;

    private HashMap<String, Object> payload;
}
