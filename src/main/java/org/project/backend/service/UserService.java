package org.project.backend.service;

import org.project.backend.model.SubscriptionPlan;
import org.project.backend.model.User;
import org.project.backend.repository.SubscriptionPlanRepository;
import org.project.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;

    public UserService(UserRepository userRepository, SubscriptionPlanRepository planRepository) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
    }

    public void updateUserPlan(Long userId, Integer planId, int durationInDays) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        user.setPlan(plan);
        user.setSubscriptionEndDate(
                Timestamp.from(Instant.now().plusSeconds(86400L * durationInDays))
        );
        userRepository.save(user);
    }

}