package io.agora.uikit.bean.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProcessRemovedDomain {
    private Integer actionType;

    private String processUuid;

    private String userId;

    private ProcessMicSeatPayloadDomain payload;

    private Long createTime;

    private Long updateTime;
}
