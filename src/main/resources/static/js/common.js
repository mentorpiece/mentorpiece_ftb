/**
 * Common JavaScript functionality for Aero Travel
 */

// Initialize Feather Icons
document.addEventListener('DOMContentLoaded', function() {
    if (typeof feather !== 'undefined') {
        feather.replace();
    }
});

// Utility function to initialize page-specific functionality
window.AeroTravel = {
    // Initialize common features
    init: function() {
        this.initFeatherIcons();
        this.initAnimations();
    },

    // Initialize Feather Icons
    initFeatherIcons: function() {
        if (typeof feather !== 'undefined') {
            feather.replace();
        }
    },

    // Initialize animations
    initAnimations: function() {
        // Add intersection observer for fade-in animations
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animate-fade-in');
                }
            });
        }, observerOptions);

        // Observe all elements with animation classes
        document.querySelectorAll('.animate-on-scroll').forEach(el => {
            observer.observe(el);
        });
    },

    // Utility function for smooth scrolling
    smoothScrollTo: function(target) {
        const element = document.querySelector(target);
        if (element) {
            element.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    },

    // Show toast notification
    showToast: function(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <div class="toast-content">
                <i data-feather="${this.getToastIcon(type)}"></i>
                <span>${message}</span>
            </div>
        `;

        document.body.appendChild(toast);

        // Trigger animation
        setTimeout(() => {
            toast.classList.add('show');
        }, 100);

        // Auto remove
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => {
                document.body.removeChild(toast);
            }, 300);
        }, 3000);

        // Update icons
        this.initFeatherIcons();
    },

    // Get appropriate icon for toast type
    getToastIcon: function(type) {
        const icons = {
            'success': 'check-circle',
            'error': 'alert-circle',
            'warning': 'alert-triangle',
            'info': 'info'
        };
        return icons[type] || 'info';
    }
};

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    window.AeroTravel.init();
});