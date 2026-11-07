package com.team.idp.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team.idp.controller.HelloController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Only checks what SecurityConfig decides. "Allowed" means the request is not answered with
 * 401 or 403 (it may be 404 here, because the real controllers are not loaded in this test).
 */
@WebMvcTest(controllers = HelloController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    private MockHttpSession login(String profileName) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("profileName", profileName);
        return session;
    }

    private static ResultMatcher allowed() {
        return result -> {
            int status = result.getResponse().getStatus();
            if (status == 401 || status == 403) {
                throw new AssertionError("Blocked by security, status " + status);
            }
        };
    }

    @Test
    void guestCanSearchButNotUseCustomerIdps() throws Exception {
        mvc.perform(get("/api/guest/idps")).andExpect(allowed());
        mvc.perform(get("/api/idps")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/idps/5")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/categories")).andExpect(allowed());
        mvc.perform(get("/api/companies/3")).andExpect(allowed());
    }

    @Test
    void guestCannotUseFavourites() throws Exception {
        mvc.perform(get("/api/favourites")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/favourites")).andExpect(status().isUnauthorized());
    }

    @Test
    void onlyCustomerCanUseFavourites() throws Exception {
        mvc.perform(get("/api/favourites").session(login("Customer"))).andExpect(allowed());
        mvc.perform(get("/api/favourites").session(login("Interior Designer"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/favourites").session(login("Platform Manager"))).andExpect(status().isForbidden());
    }

    @Test
    void onlyPlatformManagerCanChangeCategoriesAndSeeReports() throws Exception {
        mvc.perform(post("/api/categories")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/categories").session(login("Customer"))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/categories/2").session(login("Platform Manager"))).andExpect(allowed());
        mvc.perform(get("/api/reports/daily").session(login("Customer"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/reports/daily").session(login("Platform Manager"))).andExpect(allowed());
    }

    @Test
    void onlyDesignerCanManageOwnCompany() throws Exception {
        mvc.perform(get("/api/companies/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/companies/me").session(login("Customer"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/companies/me").session(login("Interior Designer"))).andExpect(allowed());
        mvc.perform(post("/api/companies").session(login("Interior Designer"))).andExpect(allowed());
    }

    @Test
    void onlyUserAdminCanManageProfiles() throws Exception {
        mvc.perform(get("/api/admin/profiles")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/profiles").session(login("Customer"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/profiles").session(login("User Admin"))).andExpect(allowed());
    }

    @Test
    void anyLoggedInRoleCanUseOwnProfileButGuestCannot() throws Exception {
        mvc.perform(get("/api/profile")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/profile").session(login("Customer"))).andExpect(allowed());
    }

    @Test
    void loginAndLogoutAreOpen() throws Exception {
        mvc.perform(post("/api/auth/login")).andExpect(allowed());
        mvc.perform(post("/api/auth/logout")).andExpect(allowed());
    }
}