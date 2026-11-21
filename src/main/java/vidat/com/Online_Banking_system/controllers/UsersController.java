package vidat.com.Online_Banking_system.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vidat.com.Online_Banking_system.dtos.LoginRequest;
import vidat.com.Online_Banking_system.dtos.LoginResponse;
import vidat.com.Online_Banking_system.dtos.RegisterRequest;
import vidat.com.Online_Banking_system.dtos.RegisterResponse;
import vidat.com.Online_Banking_system.services.AuthService;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")

public class UsersController {
    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginRequest));
    }
}
