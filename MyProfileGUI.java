package Project;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.stage.Stage;

//MyProfileGUI class
public class MyProfileGUI extends Application {
    private Stage primaryStage;  //Store the reference to the primary stage
    private Runnable onProfileSaved;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        //Main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: #f9f9f9;");

        //Name field
        Label nameLabel = new Label("Name");
        nameLabel.setFont(Font.font(12));
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");
        nameField.setPrefHeight(35);

        //Age field
        Label ageLabel = new Label("Age");
        ageLabel.setFont(Font.font(12));
        TextField ageField = new TextField();
        ageField.setPromptText("Enter your age");
        ageField.setPrefHeight(35);

        //Email account field
        Label emailAccountLabel = new Label("Email account");
        emailAccountLabel.setFont(Font.font(12));
        TextField emailAccountField = new TextField();
        emailAccountField.setPromptText("Enter email (For example: xxxxx@email.com)");
        emailAccountField.setPrefHeight(35);

        //Mobile number field
        Label mobileLabel = new Label("Mobile number");
        mobileLabel.setFont(Font.font(12));
        TextField mobileField = new TextField();
        mobileField.setPromptText("Enter mobile number");
        mobileField.setPrefHeight(35);

        //Gender label
        Label genderLabel = new Label("Gender");
        genderLabel.setFont(Font.font(12));

        //Create radio buttons for gender type selection
        RadioButton maleButton = new RadioButton("Male");
        RadioButton femaleButton = new RadioButton("Female");

        //Group the radio buttons so only one can be selected at a time
        ToggleGroup group = new ToggleGroup();
        maleButton.setToggleGroup(group);
        femaleButton.setToggleGroup(group);

        //Create a GridPane to hold the label and radio buttons
        GridPane genderPane = new GridPane();
        genderPane.setVgap(10); //Optional: Adjust vertical gap
        genderPane.setHgap(10); //Optional: Adjust horizontal gap

        //Add the label and radio buttons to the grid
        genderPane.add(genderLabel, 0, 0);  //Gender label in column 0, row 0
        genderPane.add(maleButton, 4, 0);   //Male radio button in column 1, row 0
        genderPane.add(femaleButton, 6, 0); //Female radio button in column 2, row 0

        //Optional: Set the font size for radio buttons
        maleButton.setFont(new Font(11));
        femaleButton.setFont(new Font(11));

        //Create an HBox to align gender options horizontally
        HBox genderBox = new HBox(15, maleButton, femaleButton);
        genderBox.setStyle("-fx-alignment: center-left;");

        //Location field
        Label locationLabel = new Label("Location");
        locationLabel.setFont(Font.font(12));
        TextField locationField = new TextField();
        locationField.setPromptText("Enter location (For example: USA)");
        locationField.setPrefHeight(35);

        //Save Change Button
        Button saveButton = new Button("Save Change");
        saveButton.setPrefWidth(150);
        saveButton.setPrefHeight(40);
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5; -fx-background-radius: 5;");

        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailAccountField.getText().trim();
            String mobileNumber = mobileField.getText().trim();
            String location = locationField.getText().trim();
            String gender = maleButton.isSelected() ? "Male" : femaleButton.isSelected() ? "Female" : null;

            int age;
            try {
                age = Integer.parseInt(ageField.getText().trim());
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid age.");
                return;
            }

            if (name.isEmpty() || email.isEmpty() || gender == null || mobileNumber.isEmpty() || location.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Missing Input", "Please fill in all fields.");
                return;
            }

            DatabaseHandler dbHandler = new DatabaseHandler();
            boolean success = dbHandler.updateUserProfile(email, name, age, gender, mobileNumber, location);
            if (success) {
                showUserInfoWindow(name, age, email, gender, mobileNumber, location);
                if(onProfileSaved != null) {
                    onProfileSaved.run(); //Notify CombinedApp
                }
                primaryStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update profile. Check your email.");
            }
        });

        //Footer Label
        Label footerLabel = new Label("©️ 2025 Ch@tify");
        footerLabel.setFont(Font.font(12));
        footerLabel.setStyle("-fx-text-fill: grey;");
        footerLabel.setAlignment(Pos.CENTER);

        //Form layout
        GridPane formLayout = new GridPane();
        formLayout.setHgap(15);
        formLayout.setVgap(20);
        formLayout.setPadding(new Insets(20, 0, 20, 0));
        formLayout.add(nameLabel, 0, 0);
        formLayout.add(nameField, 1, 0);
        formLayout.add(ageLabel, 0, 1);
        formLayout.add(ageField, 1, 1);
        formLayout.add(genderLabel, 0, 2);
        formLayout.add(genderBox, 1, 2);
        formLayout.add(emailAccountLabel, 0, 3);
        formLayout.add(emailAccountField, 1, 3);
        formLayout.add(mobileLabel, 0, 4);
        formLayout.add(mobileField, 1, 4);
        formLayout.add(locationLabel, 0, 5);
        formLayout.add(locationField, 1, 5);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(70);
        formLayout.getColumnConstraints().addAll(col1, col2);

        //Main layout setup
        mainLayout.getChildren().addAll(formLayout, saveButton, footerLabel);

        //Scene setup
        Scene scene = new Scene(mainLayout, 700, 500);
        primaryStage.setTitle("Update Profile");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void showUserInfoWindow(String name, int age, String email, String gender, String mobileNumber, String location) {
        Stage userInfoStage = new Stage();
        userInfoStage.setTitle("My Profile");

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #ffffff;");

        Label titleLabel = new Label("User Profile Information");
        titleLabel.setFont(new Font(16));

        Label nameLabel = new Label("Name: " + name);
        Label ageLabel = new Label("Age: " + age);
        Label emailLabel = new Label("Email: " + email);
        Label genderLabel = new Label("Gender: " + gender);
        Label mobileLabel = new Label("Mobile Number: " + mobileNumber);
        Label locationLabel = new Label("Location: " + location);

        nameLabel.setFont(new Font(14));
        ageLabel.setFont(new Font(14));
        emailLabel.setFont(new Font(14));
        genderLabel.setFont(new Font(14));
        mobileLabel.setFont(new Font(14));
        locationLabel.setFont(new Font(14));

        Button closeButton = new Button("Close");
        closeButton.setPrefWidth(100);
        closeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        closeButton.setOnAction(e -> userInfoStage.close());

        Button updateButton = new Button("Update");
        updateButton.setPrefWidth(100);
        updateButton.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white;");
        updateButton.setOnAction(e -> {
            userInfoStage.close(); //Close the user info window
            start(new Stage()); //Reopen the profile settings window
        });

        Label footerLabel = new Label("©️ 2025 Ch@tify");
        footerLabel.setFont(new Font(12));
        footerLabel.setStyle("-fx-text-fill: #888888; -fx-alignment: center;");

        layout.getChildren().addAll(titleLabel, nameLabel, ageLabel, emailLabel, genderLabel,
                mobileLabel, locationLabel, closeButton, updateButton, footerLabel);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 400);
        userInfoStage.setScene(scene);
        userInfoStage.show();
    }

    public void setOnProfileSaved(Runnable onProfileSaved) {
        this.onProfileSaved = onProfileSaved;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
