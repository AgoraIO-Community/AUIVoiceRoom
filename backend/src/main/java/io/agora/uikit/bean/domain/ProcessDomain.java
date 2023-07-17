package io.agora.uikit.bean.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ProcessDomain {
    private Boolean isEnabled;

    private Integer timeout;

    private Integer maxWait;

    private Integer maxAccept;

    private List<ProcessQueueDomain> queue;

    private List<ProcessRemovedDomain> removed;
}
