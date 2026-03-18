package com.restaurant.restaurantmanagement.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.restaurant.restaurantmanagement.dto.request.InitiatePaymentRequest;
import com.restaurant.restaurantmanagement.dto.request.VerifyPaymentRequest;
import com.restaurant.restaurantmanagement.dto.response.BillResponse;
import com.restaurant.restaurantmanagement.dto.response.PaymentResponse;
import com.restaurant.restaurantmanagement.entity.*;
import com.restaurant.restaurantmanagement.enums.*;
import com.restaurant.restaurantmanagement.repository.BillRepository;
import com.restaurant.restaurantmanagement.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final BillService billService;
    private final RazorpayClient razorpayClient;

    @org.springframework.beans.factory.annotation.Value("${razorpay.key.secret}")
    private String razorpaySecret;

    // ─── Initiate Payment ──────────────────────────────────

    @Transactional
    public PaymentResponse initiatePayment(Customer customer,
                                           InitiatePaymentRequest request) {

        Bill bill = billRepository
                .findByCustomerIdAndStatus(customer.getId(), BillStatus.UNPAID)
                .orElseThrow(() -> new RuntimeException(
                        "No active bill found. Please generate bill first."));

        // Check payment not already initiated
        paymentRepository.findByBillId(bill.getId()).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.SUCCESS) {
                throw new RuntimeException("Payment already completed for this bill");
            }
        });

        PaymentMethod method = PaymentMethod
                .valueOf(request.getPaymentMethod().toUpperCase());

        // Cash payment — direct checkout
        if (method == PaymentMethod.CASH) {
            return processCashPayment(customer, bill);
        }

        // Online payment — create Razorpay order
        return initiateRazorpayPayment(customer, bill, method);
    }

    // ─── Cash Payment ──────────────────────────────────────

    private PaymentResponse processCashPayment(Customer customer, Bill bill) {

        Payment payment = new Payment();
        payment.setBill(bill);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setAmount(bill.getTotalAmount());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        // Trigger checkout
        billService.checkout(customer);

        log.info("Cash payment completed for bill: {}", bill.getId());

        return mapToPaymentResponse(saved, "Cash payment successful! Thank you.");
    }

    // ─── Razorpay Payment ──────────────────────────────────

    private PaymentResponse initiateRazorpayPayment(Customer customer,
                                                    Bill bill,
                                                    PaymentMethod method) {
        try {
            // Amount in paise (Razorpay uses smallest currency unit)
            int amountInPaise = bill.getTotalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "bill_" + bill.getId());

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

            // Save pending payment
            Payment payment = new Payment();
            payment.setBill(bill);
            payment.setPaymentMethod(method);
            payment.setAmount(bill.getTotalAmount());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setRazorpayOrderId(razorpayOrder.get("id"));

            Payment saved = paymentRepository.save(payment);

            log.info("Razorpay order created: {} for bill: {}",
                    razorpayOrder.get("id"), bill.getId());

            return mapToPaymentResponse(saved,
                    "Payment initiated. Complete payment using Razorpay.");

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new RuntimeException("Payment initiation failed. Please try again.");
        }
    }

    // ─── Verify Payment ────────────────────────────────────

    @Transactional
    public PaymentResponse verifyPayment(Customer customer,
                                         VerifyPaymentRequest request) {

        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        // Verify signature — this is the security critical step
        boolean isValid = verifyRazorpaySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment verification failed. Invalid signature.");
        }

        // Update payment
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Trigger checkout
        billService.checkout(customer);

        log.info("Payment verified successfully for order: {}",
                request.getRazorpayOrderId());

        return mapToPaymentResponse(payment, "Payment successful! Thank you.");
    }

    // ─── Signature Verification ────────────────────────────

    private boolean verifyRazorpaySignature(String orderId,
                                            String paymentId,
                                            String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpaySecret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString().equals(signature);

        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    // ─── Mapper ────────────────────────────────────────────

    private PaymentResponse mapToPaymentResponse(Payment payment, String message) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentMethod().name(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getRazorpayOrderId(),
                message,
                payment.getCreatedAt()
        );
    }
}
