package vidat.com.Online_Banking_system.services;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vidat.com.Online_Banking_system.dtos.LoginRequest;
import vidat.com.Online_Banking_system.dtos.LoginResponse;
import vidat.com.Online_Banking_system.dtos.RegisterRequest;
import vidat.com.Online_Banking_system.dtos.RegisterResponse;
import vidat.com.Online_Banking_system.entities.Role;
import vidat.com.Online_Banking_system.entities.Users;
import vidat.com.Online_Banking_system.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    @Transactional
    public RegisterResponse register(RegisterRequest request){
        if(userRepository.existsByUserName(request.getUsername())){
            throw new IllegalStateException("Username already taken");
        }
        String hash= passwordEncoder.encode(request.getPassword());
        Users users=new Users();
        users.setUserName(request.getUsername());
        users.setPassword_hash(hash);
        users.setRole(Role.USER);
        Users savedUser=userRepository.save(users);
        return RegisterResponse.from(savedUser);
    }

    public LoginResponse login(LoginRequest loginRequest){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword())
        );
        Users users=userRepository.findByUserName(loginRequest.getUsername()).orElseThrow();

        String token= jwtService.generateToken(users.getUserName(),users.getRole());
        return new LoginResponse(token, "Login Succesfully");

    }
}
