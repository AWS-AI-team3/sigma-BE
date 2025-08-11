package com.awsgbsa.sigma_BE.setting.controller;

import com.awsgbsa.sigma_BE.common.ApiResponse;
import com.awsgbsa.sigma_BE.security.jwt.JwtUtil;
import com.awsgbsa.sigma_BE.setting.dto.GesturePatchRequest;
import com.awsgbsa.sigma_BE.setting.service.UserSettingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/settings")
@RequiredArgsConstructor
public class UserSettingController {

    private final UserSettingService userSettingService;
    private final JwtUtil jwtUtil;

    @PatchMapping("/motion")
    public ResponseEntity<ApiResponse<?>> patchGestures(
            HttpServletRequest request,
            @RequestBody GesturePatchRequest gesturePatchRequest
    ){
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.extractUserId(token, false);

        userSettingService.updateGesture(userId, gesturePatchRequest);
        return ResponseEntity.ok(ApiResponse.success("기능동작 변경 완료되었습니다."));
    }
}
