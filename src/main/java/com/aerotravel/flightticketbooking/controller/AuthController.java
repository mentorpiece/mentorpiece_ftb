package com.aerotravel.flightticketbooking.controller;

import com.aerotravel.flightticketbooking.model.Role;
import com.aerotravel.flightticketbooking.model.User;
import com.aerotravel.flightticketbooking.repository.RoleRepository;
import com.aerotravel.flightticketbooking.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Controller
public class AuthController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    @Autowired
    public AuthController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("usernameError", "Username already exists");
            return "register";
        }

        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("emailError", "Email already exists");
            return "register";
        }

        try {
            userService.registerNewUser(user);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/switch-role")
    public String switchRole(@RequestParam("role") String roleName, Model model) {
        try {
            System.out.println("=== ROLE SWITCH REQUEST ===");
            System.out.println("Requested role: " + roleName);

            User updatedUser = userService.switchUserRole(roleName);

            System.out.println("Updated user current role: " + updatedUser.getCurrentRole());
            System.out.println("User available roles: " + updatedUser.getRoles().stream().map(r -> r.getName()).toList());
            System.out.println("=== ROLE SWITCH COMPLETE ===");

            return "redirect:/?roleChanged=" + roleName;
        } catch (Exception e) {
            System.out.println("=== ROLE SWITCH ERROR ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to switch role: " + e.getMessage());
            return "redirect:/?error=role-switch-failed";
        }
    }

    @GetMapping("/current-user")
    public String getCurrentUserInfo(Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("availableRoles", currentUser.getRoles());
            return "user-info";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/api/current-user")
    @ResponseBody
    @Operation(
        summary = "Get current user information",
        description = "Get current authenticated user's information including roles",
        security = { @SecurityRequirement(name = "basicAuth"), @SecurityRequirement(name = "sessionCookie") }
    )
    public Map<String, Object> getCurrentUserJson() {
        try {
            User currentUser = userService.getCurrentUser();

            System.out.println("=== API CURRENT USER REQUEST ===");
            System.out.println("Username: " + currentUser.getUsername());
            System.out.println("Current role: " + currentUser.getCurrentRole());
            System.out.println("Available roles: " + currentUser.getRoles().stream().map(Role::getName).toList());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", currentUser.getUsername());
            userInfo.put("currentRole", currentUser.getCurrentRole());
            userInfo.put("availableRoles", currentUser.getRoles().stream()
                    .map(Role::getName)
                    .toList());

            System.out.println("Returning JSON: " + userInfo);
            System.out.println("=== API RESPONSE SENT ===");

            return userInfo;
        } catch (Exception e) {
            System.out.println("=== API ERROR ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "User not authenticated: " + e.getMessage());
            return error;
        }
    }

    @PostMapping("/api/switch-role")
    @ResponseBody
    @Operation(
        summary = "Switch user role",
        description = "Switch the current user's active role to one of their available roles",
        security = { @SecurityRequirement(name = "basicAuth"), @SecurityRequirement(name = "sessionCookie") }
    )
    public Map<String, Object> switchRole(@Parameter(description = "Role name (ROLE_USER, ROLE_ADMIN, ROLE_AGENT)") @RequestParam("role") String roleName) {
        try {
            System.out.println("=== API ROLE SWITCH REQUEST ===");
            System.out.println("Requested role: " + roleName);

            User updatedUser = userService.switchUserRole(roleName);

            System.out.println("Updated user current role: " + updatedUser.getCurrentRole());
            System.out.println("User available roles: " + updatedUser.getRoles().stream().map(r -> r.getName()).toList());
            System.out.println("=== API ROLE SWITCH COMPLETE ===");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Role switched successfully");
            response.put("newRole", updatedUser.getCurrentRole());
            response.put("availableRoles", updatedUser.getRoles().stream().map(Role::getName).toList());
            return response;
        } catch (Exception e) {
            System.out.println("=== API ROLE SWITCH ERROR ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to switch role: " + e.getMessage());
            return error;
        }
    }

    @PostMapping("/api/fix-user-roles")
    @ResponseBody
    public Map<String, Object> fixUserRoles() {
        try {
            // Manual trigger to fix user roles without restarting
            User currentUser = userService.getCurrentUser();

            // Get all roles
            var allRoles = roleRepository.findAll();
            Role adminRole = allRoles.stream()
                    .filter(r -> r.getName().equals("ROLE_ADMIN"))
                    .findFirst().orElse(null);
            Role agentRole = allRoles.stream()
                    .filter(r -> r.getName().equals("ROLE_AGENT"))
                    .findFirst().orElse(null);
            Role userRole = allRoles.stream()
                    .filter(r -> r.getName().equals("ROLE_USER"))
                    .findFirst().orElse(null);

            if (adminRole != null && agentRole != null && userRole != null) {
                List<Role> allUserRoles = List.of(adminRole, agentRole, userRole);
                currentUser.setRoles(allUserRoles);

                // If current role is USER, keep it as USER
                if (!"ROLE_USER".equals(currentUser.getCurrentRole()) &&
                    !"ROLE_ADMIN".equals(currentUser.getCurrentRole()) &&
                    !"ROLE_AGENT".equals(currentUser.getCurrentRole())) {
                    currentUser.setCurrentRole("ROLE_USER");
                }

                userService.save(currentUser);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "User roles updated successfully");
                result.put("newRoles", allUserRoles.stream().map(Role::getName).toList());
                result.put("currentRole", currentUser.getCurrentRole());
                return result;
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Could not find required roles");
                return error;
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error updating roles: " + e.getMessage());
            return error;
        }
    }
}