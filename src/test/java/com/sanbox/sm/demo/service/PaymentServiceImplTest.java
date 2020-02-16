package com.sanbox.sm.demo.service;

import com.sanbox.sm.demo.domain.Payment;
import com.sanbox.sm.demo.domain.PaymentEvent;
import com.sanbox.sm.demo.domain.PaymentState;
import com.sanbox.sm.demo.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import javax.transaction.Transactional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("11.11")).build();
    }

    @Transactional
    @Test
    void preAuthTest() {
        Payment savedPayment = paymentService.newPayment(payment);

        System.out.println("Initial State :> "+savedPayment);

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());

        Payment preAuthedPayment = paymentRepository.getOne(savedPayment.getId());

        System.out.println("Preauth State :> "+sm.getState().getId());

        System.out.println("Saved Payment :> "+preAuthedPayment);
    }

    @Transactional
    @Test
    void authTest() {
        Payment savedPayment = paymentService.newPayment(payment);

        System.out.println("Initial State :> "+savedPayment);

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());

        Payment preAuthedPayment = paymentRepository.getOne(savedPayment.getId());

        System.out.println("Preauth State :> "+sm.getState().getId());

        System.out.println("Pre-Authed Payment :> "+preAuthedPayment);

        if (savedPayment.getPaymentState() == PaymentState.PRE_AUTH) {
            sm = paymentService.authPayment(savedPayment.getId());

            Payment authedPayment = paymentRepository.getOne(savedPayment.getId());

            System.out.println("Authed Payment :> "+preAuthedPayment);
        }

    }

    @Transactional
    @Test
    void authDeclineTest() {
        Payment savedPayment = paymentService.newPayment(payment);

        System.out.println("Initial State :> "+savedPayment);

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());

        Payment preAuthedPayment = paymentRepository.getOne(savedPayment.getId());

        System.out.println("Preauth State :> "+sm.getState().getId());

        System.out.println("Pre-Authed Payment :> "+preAuthedPayment);

        if (savedPayment.getPaymentState() == PaymentState.PRE_AUTH) {
            sm = paymentService.declineAuth(savedPayment.getId());

            Payment declinedAuthPayment = paymentRepository.getOne(savedPayment.getId());

            System.out.println("Declined Auth Payment :> "+declinedAuthPayment);

        }

    }
}