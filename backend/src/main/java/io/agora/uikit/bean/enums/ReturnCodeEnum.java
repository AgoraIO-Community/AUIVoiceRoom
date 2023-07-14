package io.agora.uikit.bean.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum ReturnCodeEnum {
    SUCCESS(0, "Success"),
    ERROR(-1, "error"),
    PARAMS_ERROR(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    SERVER_ERROR(500, "Internal Server Error"),
    METHOD_ERROR(501, "Not Implemented"),

    RTM_METADATA_NO_DATA_ERROR(10001, "RTM metadata no data"),
    RTM_SET_CHANNEL_METADATA_ERROR(10002, "RTM set channel failed"),
    RTM_UPDATE_CHANNEL_METADATA_ERROR(10003, "RTM update channel failed"),
    RTM_REMOVE_CHANNEL_METADATA_ERROR(10004, "RTM remove channel failed"),

    ROOM_ACQUIRE_LOCK_ERROR(10101, "Acquire lock failed"),
    ROOM_RELEASE_LOCK_ERROR(10102, "Release lock failed"),
    ROOM_NOT_OWNER_ERROR(10103, "Not room owner"),
    ROOM_NOT_EXISTS_ERROR(10104, "Room not exists"),

    MIC_SEAT_NUMBER_NOT_EXISTS_ERROR(10201, "Mic seat number not exists"),
    MIC_SEAT_NUMBER_NOT_IDLE_ERROR(10202, "Mic seat number not idle"),
    MIC_SEAT_NUMBER_ALREADY_ON_ERROR(10203, "Already on mic seat"),
    MIC_SEAT_NUMBER_NOT_ON_ERROR(10204, "Not on mic seat"),
    MIC_SEAT_NUMBER_IDLE_ERROR(10205, "Mic seat number idle"),
    MIC_SEAT_NUMBER_NOT_LOCKED_ERROR(10206, "Mic seat number not locked"),

    CHORUS_ALREADY_JOINED_ERROR(10301, "Already joined"),
    CHORUS_ACQUIRE_LOCK_ERROR(10302, "Acquire lock failed"),

    SONG_ALREADY_EXISTS_ERROR(10401, "Song already exists"),
    SONG_NOT_EXISTS_ERROR(10402, "Song not exists"),
    SONG_NOT_PLAYING_ERROR(10403, "Song not playing"),
    SONG_ALREADY_PLAYING_ERROR(10404, "Song already playing"),
    SONG_NOT_OWNER_ERROR(10405, "Not song owner"),

    NCS_SIGN_ERROR(10501, "NCS sign error"),

    USER_KICK_OUT_ERROR(10601, "kick out user err"),

    CHATROOM_NOT_EXISTS_ERROR(10701, "ChatRoom not exists");

    private Integer code;
    private String message;

    ReturnCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ReturnCodeEnum getEnumByCode(Integer code) {
        for (ReturnCodeEnum retCodeEnum : ReturnCodeEnum.values()) {
            if (Objects.equals(retCodeEnum.code, code)) {
                return retCodeEnum;
            }
        }
        return null;
    }
}
