package com.sanbox.sm.demo.service;

import com.sanbox.sm.demo.domain.Payment;
import com.sanbox.sm.demo.domain.PaymentEvent;
import com.sanbox.sm.demo.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {

    Payment newPayment(Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authPayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);
}
