package com.pes.gadgetrepair.controller;

import com.pes.gadgetrepair.config.UserSessionManager;
import com.pes.gadgetrepair.enums.UserRole;
import com.pes.gadgetrepair.factory.UserFactory;
import com.pes.gadgetrepair.model.User;
import com.pes.gadgetrepair.model.Customer;
import com.pes.gadgetrepair.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
public class AuthController {

    private final ApplicationContext context;
    private final UserService userService;
    private final UserSessionManager sessionManager;

    public AuthController(ApplicationContext context,
                          UserService userService,
                          UserSessionManager sessionManager) {

        this.context = context;
        this.userService = userService;
        this.sessionManager = sessionManager;
    }

    // Login Form Fields
    @FXML
    private VBox loginForm;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    // Register Form Fields
    @FXML
    private VBox registerForm;

    @FXML
    private TextField regNameField;

    @FXML
    private TextField regEmailField;

    @FXML
    private TextField regPhoneField;

    @FXML
    private PasswordField regPasswordField;

    // Toggle between login and register forms
    @FXML
    private void showRegisterForm() {
        loginForm.setVisible(false);
        loginForm.setManaged(false);
        registerForm.setVisible(true);
        registerForm.setManaged(true);
    }

    @FXML
    private void showLoginForm() {
        registerForm.setVisible(false);
        registerForm.setManaged(false);
        loginForm.setVisible(true);
        loginForm.setManaged(true);
    }

    /*
     LOGIN BUTTON
     */
    @FXML
    private void handleLogin(ActionEvent event) {

        try {

            String email = emailField.getText();
            String password = passwordField.getText();

            User user = userService.authenticate(email,password);

            if(user == null){
                System.out.println("Invalid login");
                return;
            }

            navigateToDashboard(user, event);

        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    /*
     REGISTER BUTTON
     (CUSTOMER ONLY)
     */
    @FXML
    private void handleRegister(ActionEvent event) {

        try {

            String name = regNameField.getText().trim();
            String email = regEmailField.getText().trim();
            String phone = regPhoneField.getText().trim();
            String password = regPasswordField.getText();

            // Validation
            if(name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                System.out.println("All fields are required");
                return;
            }

            // Create Customer using UserFactory
            User customer = UserFactory.createUser(
                    UserRole.CUSTOMER,
                    name,
                    email,
                    phone,
                    password
            );

            User savedUser = userService.saveUser(customer);
            System.out.println("Customer registered successfully");

            // Navigate to customer dashboard
            navigateToDashboard(savedUser, event);

        }
        catch(org.springframework.dao.DataIntegrityViolationException e) {
            System.out.println("Error: Email already registered. Please use a different email or login instead.");
        }
        catch(Exception e){
            System.out.println("Registration failed: " + e.getMessage());
            e.printStackTrace();
        }

    }

    /*
     Navigate to appropriate dashboard based on user role
     */
    private void navigateToDashboard(User user, ActionEvent event) {

        try {
            // Store the current user in session so other controllers can access it
            sessionManager.setCurrentUser(user);

            String dashboard = "";

            if(user.getRole() == UserRole.CUSTOMER){
                dashboard = "/fxml/customer-dashboard.fxml";
            }
            else if(user.getRole() == UserRole.TECHNICIAN){
                dashboard = "/fxml/technician-dashboard.fxml";
            }
            else if(user.getRole() == UserRole.MANAGER){
                dashboard = "/fxml/manager-dashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(dashboard));
            loader.setControllerFactory(context::getBean);

            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        }
        catch(Exception e){
            System.out.println("Navigation failed: " + e.getMessage());
            e.printStackTrace();
        }

    }

}