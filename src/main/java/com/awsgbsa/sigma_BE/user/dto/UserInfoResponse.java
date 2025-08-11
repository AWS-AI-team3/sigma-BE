package com.awsgbsa.sigma_BE.user.dto;

import com.awsgbsa.sigma_BE.user.domain.SubscriptStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
    private String userName;
    private String profileUrl;
    private String subscriptStatus;
}
