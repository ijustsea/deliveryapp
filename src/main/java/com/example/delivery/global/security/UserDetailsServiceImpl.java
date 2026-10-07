package com.example.delivery.global.security;

import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId){
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(()->new UsernameNotFoundException("Login ID not found"));

        return new UserDetailsImpl(user);
    }

}
