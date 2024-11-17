package com.example.demo.controller.userLR;
import com.example.demo.data.user.User;
import com.example.demo.security.request.LoginRequest;
import com.example.demo.repository.mySql.UserRepository;
import com.example.demo.security.request.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth/login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Check if the user exists
            Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                // Verify password
                if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                    // Generate JWT token
                    String token = jwtUtil.generateToken(user.getUsername());

                    // Return the token as a JSON object
                    return ResponseEntity.ok().body(Map.of("token", token));
                } else {
                    return new ResponseEntity<>("Invalid password.", HttpStatus.UNAUTHORIZED);
                }
            } else {
                return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error during login: ", e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}