package vidat.com.Online_Banking_system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vidat.com.Online_Banking_system.entities.Users;
import vidat.com.Online_Banking_system.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class MyUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users =userRepository.findByUserName(username).orElseThrow();
        return User.withUsername(users.getUserName())
                .password(users.getPassword_hash())
                .roles(users.getRole().name())
                .build();
    }
}
