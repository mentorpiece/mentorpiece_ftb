package com.aerotravel.flightticketbooking;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.aerotravel.flightticketbooking.model.Role;
import com.aerotravel.flightticketbooking.model.User;
import com.aerotravel.flightticketbooking.repository.FlightRepository;
import com.aerotravel.flightticketbooking.repository.RoleRepository;
import com.aerotravel.flightticketbooking.services.UserDataInitializationService;
import com.aerotravel.flightticketbooking.services.UserService;
import com.aerotravel.flightticketbooking.services.aux.DataGenService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@EnableJpaRepositories("com.aerotravel.flightticketbooking.repository")
@EntityScan("com.aerotravel.flightticketbooking.model")
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Flight ticket booking API", version = "00.07",
        contact = @Contact(name = "FTB API Support",
                url = "https://mentorpiece.org",
                email = "1@mentorpiece.org")))
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "Basic authentication using username and password"
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token authentication"
)
@SecurityScheme(
        name = "sessionCookie",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        description = "Session cookie authentication (login via /login form first)"
)
@Slf4j
public class FlightticketbookingApplication {

    private final static String logEntryTemplate = "\n\n===================================\n\n" +
            "\t\t{}" +
            "\n\n===================================\n\n";
    @Autowired
    UserService userService;
    @Autowired
    RoleRepository roleRepository;

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    DataGenService dataGenService;

    @Autowired
    UserDataInitializationService userDataInitializationService;

    public static void main(String[] args) {
        SpringApplication.run(FlightticketbookingApplication.class, args);
    }

    @PostConstruct
    public void postConstructInit() throws InterruptedException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        createUsersAndRolesIfNeeded();
        createDataIfNeeded();
    }

    public void createUsersAndRolesIfNeeded() {
        log.info(logEntryTemplate, "Check roles available.");
        var roles = roleRepository.findAll();
        if (roles.isEmpty()) {
            var adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .build();
            var agentRole = Role.builder()
                    .name("ROLE_AGENT")
                    .build();
            var userRole = Role.builder()
                    .name("ROLE_USER")
                    .build();
            log.info(logEntryTemplate, "Inserting ADMIN role.");
            roleRepository.save(adminRole);
            log.info(logEntryTemplate, "Inserting AGENT role.");
            roleRepository.save(agentRole);
            log.info(logEntryTemplate, "Inserting USER role.");
            roleRepository.save(userRole);
            roles = roleRepository.findAll();
        }

        // Update existing users to have both roles if they don't already
        updateExistingUsersWithBothRoles(roles);

        var users = userService.getAll();
        log.info(logEntryTemplate, "Check user accounts available.");
        createUserIfNeeded(roles, users, "ADMIN", "john", "$2a$10$dRM33.Fy7SYDraG5vMagXOgIhsB6Tl40VI9pwMlNhB4yfLaZpQj.m", "JohnTheAdmin@ftb.com", "John", "O.", "Ad-Mi", "\n\t\tAdmin user created: {}");
        createUserIfNeeded(roles, users, "ADMIN", "adm", "$2a$10$dRM33.Fy7SYDraG5vMagXOgIhsB6Tl40VI9pwMlNhB4yfLaZpQj.m", "SimplyTheAdmin@ftb.com", "Vasja", "O.", "Stojkov", "\n\t\tVasja-Admin has joined the chat: {}");
        createUserIfNeeded(roles, users, "AGENT", "mike", "$2a$10$vukSIdxmmtLYcy/uNMBUHeyj/qbNPcaX8lqTbXGciJ9HxaLQOmRO.", "MikeTheAgent@ftb.com", "Mike", "A.", "Gent", "\n\t\tAgent user created: {}");
        createUserIfNeeded(roles, users, "USER", "user", "$2a$10$vukSIdxmmtLYcy/uNMBUHeyj/qbNPcaX8lqTbXGciJ9HxaLQOmRO.", "UserTheUser@ftb.com", "User", "U.", "Ser", "\n\t\tUser account created: {}");
    }

    private void updateExistingUsersWithBothRoles(List<Role> roles) {
        log.info(logEntryTemplate, "Updating existing users to have USER, ADMIN and AGENT roles.");

        Role adminRole = roles.stream()
                .filter(r -> r.getName().equals("ROLE_ADMIN"))
                .findFirst().orElse(null);
        Role agentRole = roles.stream()
                .filter(r -> r.getName().equals("ROLE_AGENT"))
                .findFirst().orElse(null);
        Role userRole = roles.stream()
                .filter(r -> r.getName().equals("ROLE_USER"))
                .findFirst().orElse(null);

        if (adminRole == null || agentRole == null || userRole == null) {
            log.warn("Could not find all required roles - skipping user update");
            return;
        }

        List<Role> allRoles = List.of(userRole, agentRole, adminRole);
        var allUsers = userService.getAll();

        for (User user : allUsers) {
            boolean needsUpdate = false;

            // Check if user has all required roles
            boolean hasAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
            boolean hasAgent = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_AGENT"));
            boolean hasUser = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_USER"));

            // Update if: missing any of the three roles
            if (!hasAdmin || !hasAgent || !hasUser) {
                log.info("Updating user {} to have USER, ADMIN and AGENT roles", user.getUsername());
                user.setRoles(allRoles);

                // Set default role to USER if current role is empty or invalid
                if (user.getCurrentRole() == null || user.getCurrentRole().isEmpty()) {
                    user.setCurrentRole("ROLE_USER");
                }

                userService.save(user);
                needsUpdate = true;
            }

            if (needsUpdate) {
                log.info("User {} updated successfully", user.getUsername());
            }
        }
    }

    private void createUserIfNeeded(List<Role> roles, List<User> users, String roleSign, String username, String password, String email, String name, String middlename, String lastname, String logMsg) {
        // Check if user already exists
        var candidate = users.stream()
                .filter(u -> Objects.nonNull(u) && u.getUsername().equals(username))
                .findFirst().orElse(null);

        if (null == candidate) {
            // Get roles
            Role adminRole = roles.stream()
                    .filter(r -> r.getName().equals("ROLE_ADMIN"))
                    .findFirst().orElse(null);
            Role agentRole = roles.stream()
                    .filter(r -> r.getName().equals("ROLE_AGENT"))
                    .findFirst().orElse(null);
            Role userRole = roles.stream()
                    .filter(r -> r.getName().equals("ROLE_USER"))
                    .findFirst().orElse(null);

            // Assign roles based on user type
            List<Role> userRoles = new ArrayList<>();
            String defaultRole;

            if ("USER".equals(roleSign)) {
                // USER gets only USER role
                if (userRole != null) userRoles.add(userRole);
                defaultRole = "ROLE_USER";
            } else {
                // ADMIN and AGENT get both roles so they can switch between them
                if (adminRole != null) userRoles.add(adminRole);
                if (agentRole != null) userRoles.add(agentRole);
                defaultRole = "ROLE_AGENT";
            }

            var user = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .firstname(name)
                    .middlename(middlename)
                    .lastname(lastname)
                    .roles(userRoles)
                    .currentRole(defaultRole)
                    .build();

            log.info(logEntryTemplate, "Inserting user with both roles, default role: " + defaultRole);
            var result = userService.save(user);
            log.info(logMsg, result);
        }
    }

    private void createDataIfNeeded() {
        log.info(logEntryTemplate, "Check Flights available.");
        var existingFlights = flightRepository.findAll();
        if (existingFlights.isEmpty()) {
            log.info(logEntryTemplate, "Generating and inserting sample admin data.");

            // Create shared airports only - no user-specific data here
            // User-specific data will be created when users register
            var adminUsers = userService.getAll().stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getName().equals("ROLE_ADMIN")))
                    .toList();

            if (!adminUsers.isEmpty()) {
                // Initialize complete sample data for each admin user
                // This will include user-specific airports, aircraft, flights, and passengers
                for (User adminUser : adminUsers) {
                    userDataInitializationService.initializeUserData(adminUser);
                    log.info(logEntryTemplate, "Initialized complete sample data for admin user: " + adminUser.getUsername());
                }
            }
        }
    }
}