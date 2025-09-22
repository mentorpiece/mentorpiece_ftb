package com.aerotravel.flightticketbooking.services.servicesimpl;

import com.aerotravel.flightticketbooking.model.Role;
import com.aerotravel.flightticketbooking.model.User;
import com.aerotravel.flightticketbooking.repository.RoleRepository;
import com.aerotravel.flightticketbooking.repository.UserRepository;
import com.aerotravel.flightticketbooking.services.UserDataInitializationService;
import com.aerotravel.flightticketbooking.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@Transactional
@Validated
public class UserServiceImpl extends AbstractEntityServiceImpl<User> implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserDataInitializationService userDataInitializationService;
    private final String[] sortBy = new String[]{"username"};

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                          BCryptPasswordEncoder passwordEncoder, UserDataInitializationService userDataInitializationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDataInitializationService = userDataInitializationService;
    }

    private static Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // Only use the current active role for Spring Security authorities
        return AuthorityUtils.createAuthorityList(user.getCurrentRole());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username " + username + " not found"));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                getAuthorities(user));
    }

    @Override
    protected JpaRepository<User, Long> getRepository() {
        return userRepository;
    }

    @Override
    protected String[] getSortByProperties() {
        return sortBy;
    }

    @Override
    public User registerNewUser(User user) {
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assign all roles so users can switch between them
        Role agentRole = roleRepository.findByName("ROLE_AGENT")
                .orElseThrow(() -> new RuntimeException("AGENT role not found"));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        user.setRoles(List.of(userRole, agentRole, adminRole));
        user.setCurrentRole("ROLE_USER"); // Default to USER role

        User savedUser = userRepository.save(user);

        // Initialize sample data for the new user
        try {
            userDataInitializationService.initializeUserData(savedUser);
        } catch (Exception e) {
            log.error("Failed to initialize sample data for user: {}", savedUser.getUsername(), e);
            // Don't fail registration if sample data creation fails
        }

        return savedUser;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found in database"));
    }

    @Override
    public User switchUserRole(String roleName) {
        User currentUser = getCurrentUser();

        // Validate the role exists and user has access to it
        if (!canSwitchToRole(roleName)) {
            throw new RuntimeException("Cannot switch to role: " + roleName);
        }

        // Update the current role in database
        currentUser.setCurrentRole(roleName);
        User updatedUser = userRepository.save(currentUser);

        // Update the Spring Security context with new authorities
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Collection<? extends GrantedAuthority> newAuthorities = getAuthorities(updatedUser);
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    authentication.getCredentials(),
                    newAuthorities
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }

        return updatedUser;
    }

    @Override
    public boolean canSwitchToRole(String roleName) {
        User currentUser = getCurrentUser();
        return currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }
}
