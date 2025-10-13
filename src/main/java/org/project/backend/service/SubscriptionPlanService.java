package org.project.backend.service;

import org.project.backend.model.SubscriptionPlan;
import org.project.backend.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;

    public SubscriptionPlanService(SubscriptionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<SubscriptionPlan> getAllPlans() {
        return planRepository.findAll();
    }

    public Optional<SubscriptionPlan> getPlanById(Integer id) {
        return planRepository.findById(id);
    }

    public SubscriptionPlan createPlan(SubscriptionPlan plan) {
        return planRepository.save(plan);
    }

    public SubscriptionPlan updatePlan(Integer id, SubscriptionPlan updatedPlan) {
        return planRepository.findById(id)
                .map(plan -> {
                    plan.setName(updatedPlan.getName());
                    plan.setPrice(updatedPlan.getPrice());
                    plan.setScanLimitPerMonth(updatedPlan.getScanLimitPerMonth());
                    plan.setDescription(updatedPlan.getDescription());
                    return planRepository.save(plan);
                })
                .orElseThrow(() -> new RuntimeException("Plan not found"));
    }

    public void deletePlan(Integer id) {
        planRepository.deleteById(id);
    }
}
