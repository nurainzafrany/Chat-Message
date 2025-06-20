package Project;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Login extends Application {

    @Override
    public void start(Stage primaryStage) {

        //Title Label
        Label titleLabel = new Label("Welcome to Ch@tify!");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        //Name Field
        Label nameLabel = new Label("Your name");
        nameLabel.setStyle("-fx-text-fill: white;");
        TextField nameField = new TextField();

        //Email Field
        Label emailLabel = new Label("Your e-mail");
        emailLabel.setStyle("-fx-text-fill: white;");
        TextField emailField = new TextField();

        //Password Field
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-text-fill: white;");
        PasswordField passwordField = new PasswordField();

        //Form Layout
        VBox formLayout = new VBox(10, nameLabel, nameField, emailLabel, emailField, passwordLabel, passwordField);
        formLayout.setPadding(new Insets(20));

        //Buttons
        Button createAccountButton = new Button("Create account");
        createAccountButton.setStyle("-fx-background-color: orange; -fx-text-fill: white; -fx-font-size: 14px;");
        Button signInButton1 = new Button("Sign in");
        signInButton1.setStyle("-fx-background-color: white; -fx-text-fill: blue; -fx-font-size: 14px;");

        createAccountButton.setOnAction(e -> showNextGUI(primaryStage));

        signInButton1.setOnAction(e -> {
            String username = nameField.getText().trim();
            if (username.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Please enter a username!");
            }
            //Get the password from the nameField
            String password = passwordField.getText().trim();
            if (password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Please enter a username!");
            }//Get the email from the nameField
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Please enter an email address!");
            }
            else {
                DatabaseHandler dbHandler = new DatabaseHandler();
                boolean isValidUser = dbHandler.validateUser(email, password);
                if (isValidUser) {
                    //Start the CombinedApp GUI and pass the username
                    CombinedApp combinedApp = new CombinedApp(username, email);//Pass email to CombinedApp
                    combinedApp.start(new Stage()); //Start CombinedApp on a new Stage
                    primaryStage.close(); //Close Login window
                } else {
                    showAlert(Alert.AlertType.ERROR, "Invalid credentials!");
                }
            }
        });

        HBox buttonLayout = new HBox(10, createAccountButton, signInButton1);
        buttonLayout.setPadding(new Insets(20));

        //Image Section
        ImageView imageView = new ImageView(new Image("C:\\Users\\user\\Desktop\\CSC3104-PROGRAMMING\\Project\\login.jpg")); // Replace with your image path
        imageView.setFitWidth(350); //Adjust the width to make the image larger
        imageView.setFitHeight(500); //Adjust the height to make the image larger
        imageView.setPreserveRatio(true);

        VBox imageLayout = new VBox(imageView);
        imageLayout.setAlignment(Pos.TOP_LEFT); //Center the image vertically on the left
        imageLayout.setPadding(new Insets(10));

        //Main Layout with Image on Left
        HBox contentLayout = new HBox(20, imageLayout, new VBox(20, titleLabel, formLayout, buttonLayout));
        contentLayout.setAlignment(Pos.CENTER);
        contentLayout.setPadding(new Insets(20));

        BorderPane root = new BorderPane();
        root.setCenter(contentLayout);
        root.setStyle("-fx-background-color: blue;");

        //Footer
        Label footer = new Label("©️ 2025 Ch@tify");
        footer.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        footer.setPadding(new Insets(10));
        footer.setAlignment(Pos.CENTER);

        root.setBottom(footer); //Add footer to the bottom of the layout

        //Scene and Stage
        Scene scene = new Scene(root, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Welcome");
        primaryStage.show();
    }

    private void showNextGUI(Stage primaryStage) {
        Label titleLabel = new Label("Create Account");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: blue");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField emailField = new TextField();
        emailField.setPromptText("E-mail");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button signUpButton = new Button("Sign Up");
        signUpButton.setStyle("-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff); -fx-text-fill: white; -fx-font-size: 14px;");
        Button signInButton2 = new Button("Sign In");
        signInButton2.setStyle("-fx-background-color: lightgray; -fx-text-fill: black; -fx-font-size: 14px;");

        signUpButton.setOnAction(e -> {
            String username = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "All fields are required!");
            } else {
                DatabaseHandler dbHandler = new DatabaseHandler();
                boolean isRegistered = dbHandler.registerUser(username, email, password);
                if (isRegistered) {
                    showAlert(Alert.AlertType.INFORMATION, "Account created successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed to create account. Email might already exist.");
                }
            }
        });

        signInButton2.setOnAction(e -> showLogin(primaryStage));

        //Footer label
        Label footerLabel = new Label("©️ 2025 Ch@tify");
        footerLabel.setFont(new Font(12));
        footerLabel.setStyle("-fx-text-fill: #888888; -fx-alignment: center;");

        VBox formLayout = new VBox(10, nameField, emailField, passwordField, signUpButton, signInButton2, footerLabel);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setPadding(new Insets(20));

        VBox mainLayout = new VBox(20, titleLabel, formLayout);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));

        Scene nextScene = new Scene(mainLayout, 400, 400);
        primaryStage.setTitle("New User");
        primaryStage.setScene(nextScene);
    }

    private void showLogin(Stage primaryStage) {
        start(primaryStage);
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
