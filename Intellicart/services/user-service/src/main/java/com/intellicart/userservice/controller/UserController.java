package com.intellicart.userservice.controller;

import com.intellicart.userservice.dto.AuthResponse;
import com.intellicart.userservice.model.Role;
import com.intellicart.userservice.model.User;
import com.intellicart.userservice.repository.RoleRepository;
import com.intellicart.userservice.repository.UserRepository;
import com.intellicart.userservice.security.JwtUtil;
import com.intellicart.userservice.security.UserPrincipal;
import com.intellicart.userservice.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Transactional
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
       String email  = SecurityContextHolder.getContext().getAuthentication().getName();
       return userRepository.findByEmail(email)
               .map(ResponseEntity::ok)
               .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/me/toggle-seller")
    public ResponseEntity<AuthResponse> toggleSellerRole() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).map(user -> {
            boolean isSeller = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_SELLER"));
            if (isSeller) {
                user.getRoles().removeIf(r -> r.getName().equals("ROLE_SELLER"));
            } else {
                Optional<Role> sellerRole = roleRepository.findByName("ROLE_SELLER");
                sellerRole.ifPresent(role -> user.getRoles().add(role));
            }
            userRepository.saveAndFlush(user);

            // Generar token manualmente con los roles actualizados para evitar esperas de commit
            var userPrincipal = new UserPrincipal(user);
            var token = jwtUtil.generateToken(userPrincipal);

            return ResponseEntity.ok(new AuthResponse(token));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(@RequestBody User userDetails){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(user -> {
                    user.setFirstName(userDetails.getFirstName());
                    user.setLastName(userDetails.getLastName());
                    return ResponseEntity.ok(userRepository.save(user));
                }).orElse(ResponseEntity.notFound().build());
    }
}
