package com.awsgbsa.sigma_BE.setting.dto;

import com.awsgbsa.sigma_BE.setting.domain.Motion;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class UserSettingsResponse {
    private boolean showSkeleton;
    private boolean showCursor;
    private String motionLeftClick;
    private String motionRightClick;
    private String motionWheelScroll;
    private String motionRecordStart;
    private String motionRecordStop;
}
