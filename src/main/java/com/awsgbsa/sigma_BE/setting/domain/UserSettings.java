package com.awsgbsa.sigma_BE.setting.domain;

import com.awsgbsa.sigma_BE.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter @Service
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

    private boolean showSkeleton;

    private boolean showCursor;

    @Enumerated(EnumType.STRING)
    private Motion motionLeftClick; // 좌클릭 기능

    @Enumerated(EnumType.STRING)
    private Motion motionRightClick; // 우클릭 기능

    @Enumerated(EnumType.STRING)
    private Motion motionWheelScroll; // 스크롤다운 기능

    @Enumerated(EnumType.STRING)
    private Motion motionRecordStart; // 녹음시작 기능

    @Enumerated(EnumType.STRING)
    private Motion motionRecordStop; // 녹음중지 기능

    @PrePersist
    @PreUpdate
    private void validateUniqueMotions() {
        // UNASSIGNED 제외하고 중복 금지
        List<Motion> motions = List.of(
                motionLeftClick, motionRightClick, motionWheelScroll,
                motionRecordStart, motionRecordStop
        );
        Set<Motion> used = EnumSet.noneOf(Motion.class);
        for (Motion m : motions) {
            if (m == null || m == Motion.UNASSIGNED) continue;
            if (!used.add(m)) {
                throw new IllegalArgumentException("동일 모션을 여러 컨트롤에 중복 지정할 수 없습니다: " + m);
            }
        }
    }

}
