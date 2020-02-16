package com.sanbox.sm.demo.config;

import com.sanbox.sm.demo.domain.Payment;
import com.sanbox.sm.demo.domain.PaymentEvent;
import com.sanbox.sm.demo.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(PaymentState.START)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PaymentState.PRE_AUTH_ERROR)
                .end(PaymentState.AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions
                .withExternal().source(PaymentState.START).target(PaymentState.START).event(PaymentEvent.PRE_AUTH)
                    .action(preAuthAction())
                .and()
                .withExternal().source(PaymentState.START).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED)
                .and()
                .withExternal().source(PaymentState.START).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(PaymentEvent.AUTH).action(authAction())
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(PaymentEvent.AUTH_DECLINED);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState,PaymentEvent> listener = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("State Transition from : [%s] to : [%s]", from.getId(), to.getId()));
            }
        };
        config.withConfiguration().listener(listener);
    }

    public Action<PaymentState, PaymentEvent> preAuthAction() {
        return stateContext -> {
            log.info("Pre-Auth action Invoked");
            if (new Random().nextInt(10) % 2 == 0) {
                stateContext.getStateMachine()
                        .sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                                .setHeader("payment_id", stateContext.getMessageHeader("payment_id"))
                                .build());
            } else {
                stateContext.getStateMachine()
                        .sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
                                .setHeader("payment_id", stateContext.getMessageHeader("payment_id"))
                                .build());
            }
        };
    }

    public Action<PaymentState, PaymentEvent> authAction() {
        return stateContext -> {
            log.info("Auth action Invoked");
           if (new Random().nextInt(10) % 2 == 0) {
                stateContext.getStateMachine()
                        .sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED)
                                .setHeader("payment_id", stateContext.getMessageHeader("payment_id"))
                                .build());
            } else {
               stateContext.getStateMachine()
                        .sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVED)
                                .setHeader("payment_id", stateContext.getMessageHeader("payment_id"))
                                .build());
            }
        };
    }
}
