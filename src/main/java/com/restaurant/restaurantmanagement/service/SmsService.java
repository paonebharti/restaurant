package com.restaurant.restaurantmanagement.service;

import com.restaurant.restaurantmanagement.config.TwilioConfig;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final TwilioConfig twilioConfig;

    public void sendOtp(String toPhone, String otp) {
        try {
            Message message = Message.creator(
                    new PhoneNumber("+91" + toPhone),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    "Your Restaurant OTP is: " + otp + ". Valid for 5 minutes. Do not share with anyone."
            ).create();

            log.info("OTP sent successfully to {} with SID: {}", toPhone, message.getSid());

        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", toPhone, e.getMessage());
            throw new RuntimeException("Failed to send OTP. Please try again.");
        }
    }
}
