package io.agora.uikit.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.agora.uikit.bean.dto.R;

@RestController
@RequestMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
public class HealthController {
    /**
     * Check
     * 
     * @return
     */
    @RequestMapping("/check")
    public R<Void> check() {
        return R.success(null);
    }
}