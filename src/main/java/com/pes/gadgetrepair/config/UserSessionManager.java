package com.pes.gadgetrepair.config;

import com.pes.gadgetrepair.model.User;
import org.springframework.stereotype.Component;

/*
 Design Pattern: Singleton (managed by Spring)

 Purpose:
 Stores the current logged-in user session so that controllers
 can access the user's information (ID, role, etc.) after authentication.

 Usage:
 - AuthController sets the user after successful login/registration
 - Other controllers retrieve the user to access their ID and details
*/

@Component
public class UserSessionManager {

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public void clearSession() {
        this.currentUser = null;
    }
}
