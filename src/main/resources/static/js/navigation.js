/**
 * Navigation functionality for Aero Travel
 */

// Modern User Menu Toggle
function toggleUserMenu() {
    const dropdown = document.getElementById('userDropdownMenu');
    const trigger = document.getElementById('userMenuTrigger');

    if (dropdown && trigger) {
        const isShown = dropdown.classList.contains('show');

        if (isShown) {
            dropdown.classList.remove('show');
            trigger.setAttribute('aria-expanded', 'false');
        } else {
            dropdown.classList.add('show');
            trigger.setAttribute('aria-expanded', 'true');
        }
    }
}

// Close dropdown when clicking outside
document.addEventListener('click', function(event) {
    const dropdown = document.getElementById('userDropdownMenu');
    const trigger = document.getElementById('userMenuTrigger');
    const roleBadge = document.getElementById('currentRoleBadge');

    if (dropdown && trigger && !trigger.contains(event.target) && !dropdown.contains(event.target) && !roleBadge.contains(event.target)) {
        dropdown.classList.remove('show');
        trigger.setAttribute('aria-expanded', 'false');
    }
});

// Role switching functionality
function switchRole(roleName) {
    console.log('Switching to role:', roleName);

    // Close the dropdown
    const dropdown = document.getElementById('userDropdownMenu');
    const trigger = document.getElementById('userMenuTrigger');
    if (dropdown && trigger) {
        dropdown.classList.remove('show');
        trigger.setAttribute('aria-expanded', 'false');
    }

    // Create a form and submit it
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/switch-role';

    const roleInput = document.createElement('input');
    roleInput.type = 'hidden';
    roleInput.name = 'role';
    roleInput.value = roleName;

    // Add CSRF token if available (Spring Security disabled CSRF, but good practice)
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
    if (csrfToken && csrfHeader) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = '_csrf';
        csrfInput.value = csrfToken.getAttribute('content');
        form.appendChild(csrfInput);
    }

    form.appendChild(roleInput);
    document.body.appendChild(form);

    // Immediately update the dropdown text to show switching state
    const currentRoleDisplay = document.getElementById('currentRoleDisplay');
    if (currentRoleDisplay) {
        if (roleName === 'ROLE_USER') {
            currentRoleDisplay.textContent = 'User';
        } else if (roleName === 'ROLE_AGENT') {
            currentRoleDisplay.textContent = 'Agent';
        } else if (roleName === 'ROLE_ADMIN') {
            currentRoleDisplay.textContent = 'Admin';
        }
    }

    form.submit();
}

// Function to update menu visibility and role display based on current role
function updateMenuVisibility() {
    fetch('/api/current-user')
        .then(response => {
            // Check if the response is ok (status 200-299)
            if (!response.ok) {
                // If 403 (Forbidden) or 401 (Unauthorized), user is not authenticated
                if (response.status === 403 || response.status === 401) {
                    throw new Error('User not authenticated');
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            // Check if response has content before parsing JSON
            const contentLength = response.headers.get('content-length');
            if (contentLength === '0') {
                throw new Error('Empty response from server');
            }
            
            return response.json();
        })
        .then(userInfo => {
            let roleDisplayText = 'User';

            // Update role-based menu visibility using CSS classes instead of inline styles
            const roleRestrictedItems = document.querySelectorAll('.role-restricted');

            if (userInfo.currentRole === 'ROLE_USER') {
                roleDisplayText = 'User';
                // Hide all role-restricted items for regular users
                roleRestrictedItems.forEach(item => {
                    item.classList.add('role-hidden');
                    item.classList.remove('role-agent-only', 'role-visible');
                });
            } else if (userInfo.currentRole === 'ROLE_AGENT') {
                roleDisplayText = 'Agent';
                // Show booking for agents, hide aircraft/airport management
                roleRestrictedItems.forEach(item => {
                    if (item.dataset.menu === 'booking') {
                        item.classList.add('role-visible');
                        item.classList.remove('role-hidden', 'role-agent-only');
                    } else {
                        item.classList.add('role-hidden');
                        item.classList.remove('role-visible', 'role-agent-only');
                    }
                });
            } else if (userInfo.currentRole === 'ROLE_ADMIN') {
                roleDisplayText = 'Admin';
                // Show all role-restricted items for admins
                roleRestrictedItems.forEach(item => {
                    item.classList.add('role-visible');
                    item.classList.remove('role-hidden', 'role-agent-only');
                });
            }

            // Update the role badge with icon
            const currentRoleBadge = document.getElementById('currentRoleBadge');
            if (currentRoleBadge) {
                // Get the appropriate icon for the role
                let iconName = 'user';
                if (userInfo.currentRole === 'ROLE_AGENT') {
                    iconName = 'briefcase';
                } else if (userInfo.currentRole === 'ROLE_ADMIN') {
                    iconName = 'settings';
                }

                // Update the content with icon and text
                currentRoleBadge.innerHTML = `
                    <i data-feather="${iconName}" class="nav-icon"></i>
                    <span>${roleDisplayText}</span>
                `;

                // Reinitialize feather icons
                if (typeof feather !== 'undefined') {
                    feather.replace();
                }
            }

            // Update mobile role display
            updateMobileRoleDisplay(roleDisplayText);
        })
        .catch(error => {
            // Handle authentication errors gracefully
            if (error.message === 'User not authenticated') {
                // User is not authenticated, hide all role-restricted items and show login option
                handleUnauthenticatedState();
            } else {
                console.error('Error fetching current user:', error);
                // For other errors, default to basic user state
                handleDefaultUserState();
            }
        });
}

// Function to handle unauthenticated state
function handleUnauthenticatedState() {
    // Hide all role-restricted menu items for unauthenticated users
    const roleRestrictedItems = document.querySelectorAll('.role-restricted');
    roleRestrictedItems.forEach(item => {
        item.classList.add('role-hidden');
        item.classList.remove('role-visible', 'role-agent-only');
    });

    // Only hide user menu if we're actually on a public page or definitely not authenticated
    // Check if we're on login/register pages or if Spring Security says not authenticated
    const isPublicPage = window.location.pathname.includes('/login') || 
                        window.location.pathname.includes('/register') ||
                        window.location.pathname === '/';
    
    if (isPublicPage) {
        const currentRoleBadge = document.getElementById('currentRoleBadge');
        if (currentRoleBadge) {
            currentRoleBadge.style.display = 'none';
        }
        
        const userAvatar = document.getElementById('userMenuTrigger');
        if (userAvatar) {
            userAvatar.style.display = 'none';
        }
    } else {
        // For other pages, just show default user state instead of hiding completely
        handleDefaultUserState();
        return;
    }
    
    // Update mobile role display
    updateMobileRoleDisplay('Guest');
}

// Function to handle default user state (for errors)
function handleDefaultUserState() {
    // Default to hide all role-restricted menu items on error (assume user role)
    const roleRestrictedItems = document.querySelectorAll('.role-restricted');
    roleRestrictedItems.forEach(item => {
        item.classList.add('role-hidden');
        item.classList.remove('role-visible', 'role-agent-only');
    });

    // Reset role display with icon
    const currentRoleBadge = document.getElementById('currentRoleBadge');
    if (currentRoleBadge) {
        currentRoleBadge.innerHTML = `
            <i data-feather="user" class="nav-icon"></i>
            <span>User</span>
        `;
        currentRoleBadge.style.display = 'flex';
        
        // Reinitialize feather icons
        if (typeof feather !== 'undefined') {
            feather.replace();
        }
    }

    // Update mobile role display
    updateMobileRoleDisplay('User');
}

// Function to check URL parameters and update role if needed
function checkUrlForRoleChange() {
    const urlParams = new URLSearchParams(window.location.search);
    const roleChanged = urlParams.get('roleChanged');
    if (roleChanged) {
        console.log('Role change detected in URL:', roleChanged);
        // Single immediate update instead of multiple delayed ones
        updateMenuVisibility();
    }
}

// Function to update active navigation state
function updateActiveNavigation() {
    const currentPath = window.location.pathname;

    // Remove active class from all nav links
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => link.classList.remove('active'));

    // Add active class to current page
    let activeLink = null;

    if (currentPath === '/') {
        activeLink = document.getElementById('aHome');
    } else if (currentPath.startsWith('/flights')) {
        activeLink = document.getElementById('aFlights');
    } else if (currentPath.startsWith('/aircrafts')) {
        activeLink = document.getElementById('aAircrafts');
    } else if (currentPath.startsWith('/airports')) {
        activeLink = document.getElementById('aAirports');
    } else if (currentPath.startsWith('/flight/book')) {
        activeLink = document.getElementById('aBooking');
    } else if (currentPath.startsWith('/flight/search')) {
        activeLink = document.getElementById('aSearch');
    }

    if (activeLink) {
        activeLink.classList.add('active');
    }
}

// Mobile menu toggle functionality
function toggleMobileMenu() {
    const menu = document.getElementById('mobileNavMenu');
    const toggle = document.getElementById('mobileMenuToggle');

    if (menu && toggle) {
        const isOpen = menu.classList.contains('show');

        if (isOpen) {
            menu.classList.remove('show');
            toggle.classList.remove('active');
            document.body.style.overflow = '';
        } else {
            menu.classList.add('show');
            toggle.classList.add('active');
            document.body.style.overflow = 'hidden';

            // Re-animate menu items
            const menuItems = menu.querySelectorAll('.mobile-nav-item');
            menuItems.forEach((item, index) => {
                item.style.animation = 'none';
                setTimeout(() => {
                    item.style.animation = `slideInStaggered 0.3s ease-out forwards`;
                    item.style.animationDelay = `${0.05 * (index + 1)}s`;
                }, 10);
            });
        }
    }
}

// Close mobile menu when clicking outside
document.addEventListener('click', function(event) {
    const menu = document.getElementById('mobileNavMenu');
    const toggle = document.getElementById('mobileMenuToggle');
    const content = document.querySelector('.mobile-nav-content');

    // Check if mobile menu is open
    if (menu && menu.classList.contains('show')) {
        // Close menu if clicking outside the menu content or on the toggle button
        if (!content.contains(event.target) && !toggle.contains(event.target)) {
            toggleMobileMenu();
        }
    }
});

// Update mobile role display when role changes
function updateMobileRoleDisplay(roleText) {
    const mobileRole = document.getElementById('mobileCurrentRole');
    if (mobileRole) {
        mobileRole.textContent = roleText;
    }
}

// Add a block to the main screen with a background of 9 pictures
function addWelcomeBlock() {
    const mainScreen = document.getElementById('mainScreen');

    if (mainScreen) {
        const welcomeBlock = document.createElement('div');
        welcomeBlock.id = 'welcomeBlock';
        welcomeBlock.innerHTML = `
            <div class="welcome-message">
                <h1 id="welcomeTitle">Welcome to FTB</h1>
            </div>
            <div class="background-grid">
                <div class="grid-item" style="background-image: url('/static/img/pic1.jpg');"></div>
                <div class="grid-item" style="background-image: url('/static/img/pic2.jpg');"></div>
                <div class="grid-item" style="background-image: url('/static/img/pic3.jpg');"></div>
                <div class="grid-item" style="background-image: url('/static/img/pic4.jpg');"></div>
                <div class="grid-item" style="background-image: url('/static/img/pic5.jpg');"></div>
                <div class="grid-item" style="background-image: url('/static/img/pic6.jpg');"></div>
                <div class="grid-item" style="background-image: url('/static/img/pic7.jpg');"></div>
                <div class="grid-item" style="background-image: url('/static/img/pic8.jpg');"></div>
                <div class="grid-item" style="background-image: url('/static/img/pic9.jpg');"></div>
            </div>
        `;

        mainScreen.appendChild(welcomeBlock);
        
        // Update the welcome title based on current locale/brand
        // This could be enhanced to read from server-side locale data
        updateWelcomeTitle();
    }
}

// Function to update welcome title (can be enhanced with locale detection)
function updateWelcomeTitle() {
    const welcomeTitle = document.getElementById('welcomeTitle');
    if (welcomeTitle) {
        // For now, use the brand from navigation or default to FTB
        const brandElement = document.querySelector('.navbar-brand');
        const brandText = brandElement ? brandElement.textContent.trim() : 'FTB';
        welcomeTitle.textContent = `Welcome to ${brandText}`;
    }
}

// Initialize navigation functionality
document.addEventListener('DOMContentLoaded', function() {
    updateMenuVisibility();
    updateActiveNavigation();
    checkUrlForRoleChange();

    // Close mobile menu on resize to desktop and update menu visibility
    window.addEventListener('resize', function() {
        if (window.innerWidth >= 992) {
            const menu = document.getElementById('mobileNavMenu');
            const toggle = document.getElementById('mobileMenuToggle');

            if (menu && menu.classList.contains('show')) {
                menu.classList.remove('show');
                toggle.classList.remove('active');
                document.body.style.overflow = '';
            }
        }
        // Update menu visibility to respect new screen size
        updateMenuVisibility();
    });

    // Call the function to add the welcome block
    addWelcomeBlock();
});

// Also update on window load to catch any delayed redirects
window.addEventListener('load', function() {
    updateMenuVisibility();
    checkUrlForRoleChange();
});

// Removed periodic checks - they were causing UI interference