package io.agora.uikit.bean.enums;

import lombok.Getter;

@Getter
public enum ProcessRemovedActionTypeEnum {
    ACCEPTED_SUCCESS(1),
    DECLINE(2),
    CANCEL(3),
    TIMEOUT(4),
    ACCEPTED_FAILED(5);

    private final Integer code;

    ProcessRemovedActionTypeEnum(Integer code) {
        this.code = code;
    }
}
