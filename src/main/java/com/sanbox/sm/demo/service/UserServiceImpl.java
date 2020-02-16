package com.sanbox.sm.demo.service;

import com.sanbox.sm.demo.domain.Payment;
import com.sanbox.sm.demo.domain.PaymentEvent;
import com.sanbox.sm.demo.domain.PaymentState;
import com.sanbox.sm.demo.domain.User;
import com.sanbox.sm.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    @Override
    public User newUser(User newUser) {
        return userRepository.saveAndFlush(newUser);
    }
}
