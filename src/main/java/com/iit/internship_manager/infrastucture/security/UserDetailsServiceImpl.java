package com.iit.internship_manager.infrastucture.security;

import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository utilisateurRepository;

    @Override
    public final UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // The standard User constructor: (username, password, enabled,
        // accountNonExpired, credentialsNonExpired, accountNonLocked, authorities)
        return new User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(), // <--- THIS IS THE MAGIC: if false, login fails!
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
    // checks if teacher is responsable or not
    public boolean isResponsable(String email) {
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return user.getRole().name().equals("RESPONSABLE");
    }
}