package com.awsgbsa.sigma_BE.face.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIVerifyRequestDto {
    private String src_key;
    private String tgt_key;
}
