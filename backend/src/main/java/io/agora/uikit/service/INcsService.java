package io.agora.uikit.service;

import io.agora.uikit.bean.req.NcsReq;

public interface INcsService {
    /**
     * Check sign
     * 
     * @param signature
     * @param requestBody
     * @throws Exception
     */
    void checkSign(String signature, String requestBody) throws Exception;

    /**
     * Process event
     * 
     * @param ncsReq
     */
    void processEvent(NcsReq ncsReq) throws Exception;
}
