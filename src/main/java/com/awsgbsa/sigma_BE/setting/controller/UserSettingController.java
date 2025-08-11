package com.awsgbsa.sigma_BE.setting.controller;

import com.awsgbsa.sigma_BE.common.ApiResponse;
import com.awsgbsa.sigma_BE.security.jwt.JwtUtil;
import com.awsgbsa.sigma_BE.setting.domain.UserSettings;
import com.awsgbsa.sigma_BE.setting.dto.GesturePatchRequest;
import com.awsgbsa.sigma_BE.setting.dto.UserSettingsResponse;
import com.awsgbsa.sigma_BE.setting.service.UserSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "기능-모션정의", description = "사용자의 개별적인 모션정의관리 API")
@RestController
@RequestMapping("/v1/settings")
@RequiredArgsConstructor
public class UserSettingController {

    private final UserSettingService userSettingService;
    private final JwtUtil jwtUtil;

    @GetMapping("/motion")
    @Operation(summary = "사용자 개별 모션정의 로드",
        description = "사용자가 개별적으로 정의한 모션정의의 내용을 로드 / 정의하지 않는 경우 기본값으로 세팅되어 반환")
    public ResponseEntity<ApiResponse<?>> getUserSettings(
            HttpServletRequest request
    ){
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.extractUserId(token, false);

        UserSettingsResponse gestureSetting = userSettingService.getUserSettings(userId);
        return ResponseEntity.ok(ApiResponse.success(gestureSetting));
    }

    @PatchMapping("/motion")
    @Operation(summary = "사용자 개별 모션정의 수정",
            description = "사용자가 개별적으로 정의한 모션정의를 수행\n" +
                    "1. 기능은 반드시 motion enum값중의 유효한 값( M1, M2, M3, M4, M5, M6, M7, M8, M9, UNASSIGNED )으로 매핑\n" +
                    "2. 기능에 매핑된 motion값에는 반드시 중복이 없어야함(UNASSIGNED 제외)")
    public ResponseEntity<ApiResponse<?>> patchGestures(
            HttpServletRequest request,
            @Parameter(description = "새롭게 정의하는 motion값 매핑(기재하지않으면 이전값 그대로 유지)", required = true)
            @RequestBody GesturePatchRequest gesturePatchRequest
    ){
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.extractUserId(token, false);

        userSettingService.updateGesture(userId, gesturePatchRequest);
        return ResponseEntity.ok(ApiResponse.success("기능동작 변경 완료되었습니다."));
    }
}
