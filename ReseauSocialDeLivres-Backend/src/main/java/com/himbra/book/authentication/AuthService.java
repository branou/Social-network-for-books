package com.himbra.book.authentication;

import com.himbra.book.email.EmailService;
import com.himbra.book.email.EmailTemplateName;
import com.himbra.book.role.Role;
import com.himbra.book.role.RoleRepository;
import com.himbra.book.security.JwtService;
import com.himbra.book.user.Token;
import com.himbra.book.user.TokenRepository;
import com.himbra.book.user.User;
import com.himbra.book.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Value("${mail.activation-url}")
    private String activationUrl;
    private final AuthenticationManager authenticationManager;

    public void register(registerRequest regReq) throws MessagingException {
        Role role = roleRepository.findByName("USER").orElseThrow(() -> new IllegalStateException("ROLE user was not initiated"));
        var user = User.builder().firstname(regReq.getFirstname())
                .lastname(regReq.getLastname()).email(regReq.getEmail()).password(passwordEncoder.encode(regReq.getPassword()))
                .accountLocked(false).enabled(false).roles(List.of(role)).build();
        userRepository.save(user);
        sendValidationEmail(user);
    }

    private String generateAndSaveActivationToken(User user) {
        // Generate a token
        String generatedToken = generateActivationCode(6);
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);

        return generatedToken;
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"
        );
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }

    public AuthenticationResponse login(RequestToLogin request) {
        String jwt=null;
        try{
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
            var claims = new HashMap<String, Object>();
            var user = (User) authentication.getPrincipal();
            claims.put("fullname",user.getFullName());
            jwt=jwtService.generateToken(claims,user);
        }catch (Exception e){
            e.printStackTrace();
        }
        return AuthenticationResponse.builder()
                .token(jwt)
                .build();
    }
    @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired \n A new token has been send to the same email address");
        }
        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }

    public void resendOtp(String email) throws MessagingException {
        User user=userRepository.findByEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("User not found with email: " + email));;
        sendValidationEmail(user);
    }
}
