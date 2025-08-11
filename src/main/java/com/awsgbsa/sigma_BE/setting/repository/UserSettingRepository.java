package com.awsgbsa.sigma_BE.setting.repository;

import com.awsgbsa.sigma_BE.setting.domain.UserSettings;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserSettingRepository {
    private final EntityManager em;

    public UserSettings save(UserSettings userSettings) {
        if(userSettings.getId()==null){
            em.persist(userSettings);
            return userSettings;
        } else {
            return em.merge(userSettings);  // 실제 반영된 영속 엔티티 반환
        }
    }

    public Optional<UserSettings> findByUserId(Long userId) {
        return em.createQuery(
                "SELECT us FROM UserSettings us WHERE us.user.id = :userId", UserSettings.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }

    public int updateShowSkeleton(Long userId, boolean value) {
        return em.createQuery("""
                    update UserSettings s
                    set s.showSkeleton = :val
                    where s.user.id = :userId
                """)
                .setParameter("val", value)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public int updateShowCursor(Long userId, boolean value) {
        return em.createQuery("""
                    update UserSettings s
                    set s.showCursor = :val
                    where s.user.id = :userId
                """)
                .setParameter("val", value)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
