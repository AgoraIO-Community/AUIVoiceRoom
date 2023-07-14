package io.agora.uikit.service;

import io.agora.uikit.bean.dto.KickOutRuleDto;

public interface IUserService {
    KickOutRuleDto kickOut(String operatorId, String cname, Long uid) throws Exception;
}
