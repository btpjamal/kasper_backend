package dev.jamal.kasper_backend.Service;


import dev.jamal.kasper_backend.DTO.AuthResponseDTO;
import dev.jamal.kasper_backend.DTO.LoginRequestDTO;
import dev.jamal.kasper_backend.DTO.RegisterRequestDTO;
import dev.jamal.kasper_backend.DTO.RegisterResponseDTO;
import dev.jamal.kasper_backend.Entity.Role;
import dev.jamal.kasper_backend.Entity.User;
import dev.jamal.kasper_backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public RegisterResponseDTO register(RegisterRequestDTO request) {

        String email = request.email()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        User user = User.builder()
                .name(request.name())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    public AuthResponseDTO login(LoginRequestDTO request) {

        String email = request.email()
                .trim()
                .toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                )
        );

        User user = userRepository
                .findByEmail(email)
                .orElseThrow();

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        String token =
                jwtService.generateToken(userDetails);

        return new AuthResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                token,
                "Bearer",
                jwtService.getExpirationSeconds()
        );
    }
}
