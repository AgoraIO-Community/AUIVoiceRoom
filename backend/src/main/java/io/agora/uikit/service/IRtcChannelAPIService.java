package io.agora.uikit.service;

import feign.Headers;
import feign.RequestLine;
import io.agora.uikit.bean.dto.CreateKickOutRuleDto;
import io.agora.uikit.bean.req.CreateKickOutRule;

public interface IRtcChannelAPIService {
    @RequestLine("POST /dev/v1/kicking-rule")
    @Headers("Content-Type:application/json")
    CreateKickOutRuleDto createKickOutRule(CreateKickOutRule kickOutRule);
}
