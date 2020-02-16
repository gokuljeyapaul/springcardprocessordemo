package com.sanbox.sm.demo.service;

import com.sanbox.sm.demo.domain.Payment;
import com.sanbox.sm.demo.domain.PaymentEvent;
import com.sanbox.sm.demo.domain.PaymentState;
import com.sanbox.sm.demo.interceptor.PaymentStateChangeInterceptor;
import com.sanbox.sm.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;


    @Transactional
    @Override
    public Payment newPayment(Payment payment) {
        payment.setPaymentState(PaymentState.START);
        return paymentRepository.save(payment);
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTH);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> authPayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTH);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINED);
        return sm;
    }

    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event) {
        Message<PaymentEvent> message = MessageBuilder.withPayload(event)
                .setHeader("payment_id", paymentId)
                .build();

        sm.sendEvent(message);
    }

    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {
        Payment payment = paymentRepository.getOne(paymentId);

        StateMachine<PaymentState, PaymentEvent> stateMachineById = stateMachineFactory.getStateMachine(Long.toString(payment.getId()));

        stateMachineById.stop();

        stateMachineById.getStateMachineAccessor().doWithAllRegions(sma ->{
            sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<>(payment.getPaymentState(), null, null, null));
        });

        stateMachineById.start();

        return stateMachineById;
    }
}
