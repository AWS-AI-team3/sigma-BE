package com.awsgbsa.sigma_BE.setting.dto;

import com.awsgbsa.sigma_BE.setting.domain.Motion;

public record GesturePatchRequest(
        Motion motionLeftClick,
        Motion motionRightClick,
        Motion motionWheelScroll,
        Motion motionRecordStart,
        Motion motionRecordStop
) {}
