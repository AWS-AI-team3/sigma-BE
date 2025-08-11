package com.awsgbsa.sigma_BE.setting.service;

import com.awsgbsa.sigma_BE.exception.CustomException;
import com.awsgbsa.sigma_BE.exception.ErrorCode;
import com.awsgbsa.sigma_BE.setting.domain.Motion;
import com.awsgbsa.sigma_BE.setting.domain.UserSettings;
import com.awsgbsa.sigma_BE.setting.dto.GesturePatchRequest;
import com.awsgbsa.sigma_BE.setting.repository.UserSettingRepository;
import com.awsgbsa.sigma_BE.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserSettingService {
    private final UserSettingRepository userSettingRepository;

    @Transactional
    public void updateGesture(Long userId, GesturePatchRequest req) {
        UserSettings settings = userSettingRepository.findByUserId(userId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_SETTINGS_NOT_FOUND));

        if(req.motionLeftClick() != null) settings.setMotionLeftClick(req.motionLeftClick());
        if(req.motionRightClick() != null) settings.setMotionRightClick(req.motionRightClick());
        if(req.motionWheelScroll() != null) settings.setMotionWheelScroll(req.motionWheelScroll());
        if(req.motionRecordStart() != null) settings.setMotionRecordStart(req.motionRecordStart());
        if(req.motionRecordStop() != null) settings.setMotionRecordStop(req.motionRecordStop());

        validateUniqueMotions(settings);
        userSettingRepository.save(settings); // prepersist, preupdate로 자동중복검사
    }

    private void validateUniqueMotions(UserSettings settings) {
        // UNASSIGNED(사용안함) 제외하고 중복 금지
        List<Motion> motions = List.of(
                settings.getMotionLeftClick(),
                settings.getMotionRightClick(),
                settings.getMotionWheelScroll(),
                settings.getMotionRecordStart(),
                settings.getMotionRecordStop()
        );
        Set<Motion> used = EnumSet.noneOf(Motion.class);
        for (Motion m : motions) {
            if (m == null || m == Motion.UNASSIGNED) continue;
            if (!used.add(m)) {
                throw new CustomException(ErrorCode.DUPLICATE_MOTION_UPDATE);
            }
        }
    }
}
