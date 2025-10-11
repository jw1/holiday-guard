package com.jw.holidayguard.react;

import com.jw.holidayguard.repository.ConditionalOnManagement;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller that serves the React-based admin UI.
 *
 * <p>This controller is only active when the repository implementation supports
 * management operations (CRUD). The admin UI requires the ability to create and
 * update schedules, which is only available with SQL-based repositories.
 *
 * <p>With read-only implementations (like JSON), this controller won't be
 * registered, and requests to admin UI routes will return 404.
 *
 * <p>The controller handles SPA routing by forwarding all non-API, non-static
 * requests to index.html, allowing the React Router to handle client-side routing.
 *
 * <p>Security note: These routes require authentication as configured in SecurityConfig.
 * Static assets (/assets/**, /vite.svg) remain publicly accessible for the login page.
 */
@Controller
@ConditionalOnManagement
public class AdminUiController {

    /**
     * Serves the React admin UI for SPA routes.
     * This enables client-side routing in the React SPA.
     *
     * <p>Does not match:
     * <ul>
     *   <li>/api/** - handled by REST controllers</li>
     *   <li>/assets/** - static resources (CSS, JS)</li>
     *   <li>/vite.svg - favicon</li>
     * </ul>
     *
     * @return forward to index.html
     */
    @GetMapping(value = {
            "/",
            "/admin",
            "/admin/**",
            "/schedules",
            "/schedules/**",
            "/dashboard",
            "/dashboard/**",
            "/login"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
