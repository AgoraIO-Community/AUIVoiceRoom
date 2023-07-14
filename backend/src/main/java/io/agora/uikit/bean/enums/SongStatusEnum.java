package io.agora.uikit.bean.enums;

import java.util.Objects;

import lombok.Getter;

@Getter
public enum SongStatusEnum {
    SONG_STATUS_NOT_PLAYING(0),
    SONG_STATUS_PLAYING(1),
    ;

    private Integer code;

    SongStatusEnum(Integer code) {
        this.code = code;
    }

    public static SongStatusEnum getEnumByCode(Integer code) {
        for (SongStatusEnum songStatusEnum : SongStatusEnum.values()) {
            if (Objects.equals(songStatusEnum.code, code)) {
                return songStatusEnum;
            }
        }
        return null;
    }
}
