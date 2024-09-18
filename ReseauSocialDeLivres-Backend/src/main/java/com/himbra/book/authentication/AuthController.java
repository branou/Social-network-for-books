package com.himbra.book.authentication;

import com.himbra.book.security.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
@Tag(name="Authentication")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid registerRequest regReq) throws MessagingException {
        authService.register(regReq);
        return ResponseEntity.accepted().build();
    }
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> longin(@RequestBody @Valid RequestToLogin request){
        return ResponseEntity.ok(authService.login(request));
    }
    @GetMapping("/activate-account")
    public void activateAccount(@RequestParam String req) throws MessagingException {
         authService.activateAccount(req);
    }
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) throws MessagingException {
            authService.resendOtp(email);
            return ResponseEntity.accepted().build();
    }
}

