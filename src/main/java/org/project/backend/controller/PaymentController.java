package org.project.backend.controller;

import org.project.backend.model.PaymentTransaction;
import org.project.backend.model.SubscriptionPlan;
import org.project.backend.model.User;
import org.project.backend.repository.PaymentTransactionRepository;
import org.project.backend.repository.SubscriptionPlanRepository;
import org.project.backend.repository.UserRepository;
import org.project.backend.service.EmailService;
import org.project.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentTransactionRepository paymentRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionPlanRepository planRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(
            @RequestParam Long userId,
            @RequestParam Integer planId,
            @RequestParam String transactionCode) {

        try {
            // Validate user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate plan exists
            SubscriptionPlan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("Plan not found"));

            // Create payment transaction record
            BigDecimal amount = plan.getPrice();
            String orderInfo = "Thanh toan goi " + plan.getName();

            PaymentTransaction transaction = new PaymentTransaction(
                    userId,
                    planId,
                    amount,
                    transactionCode,
                    orderInfo
            );
            transaction.setStatus("SUCCESS");
            paymentRepo.save(transaction);

            // Update user subscription (30 days)
//            userService.updateUserPlan(userId, planId, 30);

            // Get updated user data
            user = userRepository.findById(userId).orElseThrow();
            Timestamp endDate = user.getSubscriptionEndDate();

            // Send success email
            emailService.sendPaymentSuccessEmail(
                    user,
                    plan,
                    amount,
                    Timestamp.from(Instant.now()),
                    endDate
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thanh toán thành công!");
            response.put("subscriptionEndDate", endDate);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/plan-info/{planId}")
    public ResponseEntity<?> getPlanInfo(@PathVariable Integer planId) {
        try {
            SubscriptionPlan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("Plan not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("planName", plan.getName());
            response.put("amount", plan.getPrice());
            response.put("description", plan.getDescription());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}