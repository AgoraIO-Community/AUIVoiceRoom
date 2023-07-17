package io.agora.uikit.bean.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
public class ChatUserDto {
    @JsonProperty("userUuid")
    private String userUUID;

    private String userName;

    private String appKey;

    private String accessToken;
}
