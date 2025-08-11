package com.awsgbsa.sigma_BE.setting.domain;

import com.awsgbsa.sigma_BE.exception.CustomException;
import com.awsgbsa.sigma_BE.exception.ErrorCode;
import com.awsgbsa.sigma_BE.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_setting_id", nullable = false)
    private Long Id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "show_skeleton", nullable = false)
    private boolean showSkeleton;

    @Column(name = "show_cursor", nullable = false)
    private boolean showCursor;

    @Enumerated(EnumType.STRING)
    @Column(name = "motion_left_click", nullable = false)
    private Motion motionLeftClick; // 좌클릭 기능

    @Enumerated(EnumType.STRING)
    @Column(name = "motion_right_click", nullable = false)
    private Motion motionRightClick; // 우클릭 기능

    @Enumerated(EnumType.STRING)
    @Column(name = "motion_wheel_scroll", nullable = false)
    private Motion motionWheelScroll; // 스크롤다운 기능

    @Enumerated(EnumType.STRING)
    @Column(name = "motion_record_start", nullable = false)
    private Motion motionRecordStart; // 녹음시작 기능

    @Enumerated(EnumType.STRING)
    @Column(name = "motion_record_stop", nullable = false)
    private Motion motionRecordStop; // 녹음중지 기능

    public static UserSettings defaults(User user) {
        return UserSettings.builder()
                .user(user)
                .showSkeleton(true)
                .showCursor(true)
                .motionLeftClick(Motion.M1)
                .motionRightClick(Motion.M2)
                .motionWheelScroll(Motion.M3)
                .motionRecordStart(Motion.M4)
                .motionRecordStop(Motion.M5)
        .build();
    }

}
