package com.awsgbsa.sigma_BE.gateway.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatewayTokenResponse {
    private String gatewayToken;
}
