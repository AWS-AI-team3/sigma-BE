package com.awsgbsa.sigma_BE.gateway.controller;

import com.awsgbsa.sigma_BE.common.ApiResponse;
import com.awsgbsa.sigma_BE.exception.CustomException;
import com.awsgbsa.sigma_BE.exception.ErrorCode;
import com.awsgbsa.sigma_BE.gateway.dto.GatewayTokenResponse;
import com.awsgbsa.sigma_BE.gateway.service.GatewayTokenService;
import com.awsgbsa.sigma_BE.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "게이트웨이 연결인증", description = "WebSocket Gateway 연결 시 필요한 임시 세션 토큰 발급 API")
@RestController
@RequestMapping("/v2/gateway")
@RequiredArgsConstructor
public class GatewayAuthController {

    private final JwtUtil jwtUtil;
    private final GatewayTokenService gatewayTokenService;

    @PostMapping("/token")
    @Operation(
            summary = "Gateway 연결용 임시 토큰 발급",
            description = "사용자의 AccessToken을 검증후 Gateway연결용 임시토큰 발급(유효시간 60초)," +
                    "webSocket 연결시 query parameter로 전달( wss://api.example.com/dev?gatewayToken=XXXXX... )" )
    public ResponseEntity<ApiResponse<?>> getGatewayToken(HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(jwtUtil.resolveToken(request), false);
        GatewayTokenResponse gatewayToken = gatewayTokenService.createGatewayToken(userId, 60);
        return ResponseEntity.ok(ApiResponse.success(gatewayToken));

    }

    @PostMapping("/token/valid-test")
    @Operation(
            summary = "(내부 검증용) Gateway 토큰 검증 및 복호화",
            description = "내부 검증용 API입니다. GatewayToken을 복호화 및 서명검증하여 유효성을 확인합니다." +
                    "- 토큰이 만료되었거나 위조된 경우 예외를 발생"
    )
    public ResponseEntity<ApiResponse<?>> getGatewayTokenValidTest(
            @RequestParam("gatewayToken") String gatewayToken
    ) {
        boolean isValid = gatewayTokenService.validateGatewayToken(gatewayToken);
        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success("토큰검증이 완료되었습니다."));
        } else {
            throw new CustomException(ErrorCode.INVALID_GATEWAY_TOKEN);
        }
    }

}
