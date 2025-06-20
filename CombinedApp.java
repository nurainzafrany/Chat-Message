package Project;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class CombinedApp extends Application {
    private static final String SERVER_ADDRESS = "10.206.48.15"; //Server's IP
    private static final int SERVER_PORT = 1099;

    private PrintWriter out;
    private ObservableList<String> activeUsers = FXCollections.observableArrayList(); //List of active users
    private String username; //Store the username from Login class
    private String emailAdd; //Store the email from Login class
    private Stage primaryStage; //To manage navigation between GUIs

    private TextArea chatArea;
    private ListView<String> userListView;
    private TextField inputField;
    private VBox chatSection;
    private VBox sidebar;
    private VBox menuOptions;
    private Label activeUser;
    private HBox statusBox;
    private Label statusLabel;
    private Label nameLabel;
    private Label emailLabel;
    private Button myProfileButton;
    private Button logoutButton;
    private boolean isProfileSet = false;

    public CombinedApp(String username, String emailAdd) {
        this.username = username; //Assign passed username
        this.emailAdd = emailAdd; //Assign passed email
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        BorderPane root = new BorderPane();

        //Left Sidebar (User Profile Section)
        sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #cccccc;");

        //Display the username dynamically
        nameLabel = new Label(username); //Display the username
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        emailLabel = new Label(emailAdd); //Example email
        emailLabel.setStyle("-fx-text-fill: #888888;");

        VBox profileDetails = new VBox(5, nameLabel, emailLabel);

        //Menu Options
        menuOptions = new VBox(10);

        myProfileButton = createSidebarButton("👤 My Profile");

        MenuButton notificationMenu = new MenuButton("🔔 Notification");
        MenuItem muteItem = new MenuItem("Mute");
        MenuItem allowItem = new MenuItem("Allow");
        notificationMenu.getItems().addAll(muteItem, allowItem);

        logoutButton = createSidebarButton("🔒 Logout");

        //Right Main Chat Section
        chatSection = new VBox(10);
        chatSection.setPadding(new Insets(10));
        chatSection.setStyle("-fx-background-color: #ffffff;");

        //Chat Theme ComboBox
        ComboBox<String> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll("Light Theme", "Dark Theme");
        themeComboBox.setValue("Light Theme"); //Default theme
        themeComboBox.setOnAction(e -> changeChatTheme(themeComboBox.getValue(), chatSection));

        //Add action handlers for navigation
        myProfileButton.setOnAction(e -> {MyProfileGUI myProfileGUI = new MyProfileGUI();

        if (!isProfileSet) {
            //First-time click: Open profile settings window
            myProfileGUI.setOnProfileSaved(() -> isProfileSet = true); //Update flag when saved
            myProfileGUI.start(new Stage()); //Open settings
        } else {
            //Subsequent clicks: Fetch profile info and show user info window
            DatabaseHandler dbHandler = new DatabaseHandler();
            String[] userInfo = dbHandler.getUserProfile(emailAdd);

            if (userInfo != null) {
                String name = userInfo[0];
                int age = Integer.parseInt(userInfo[1]);
                String email = userInfo[2];
                String gender = userInfo[3];
                String mobileNumber = userInfo[4];
                String location = userInfo[5];

                myProfileGUI.showUserInfoWindow(name, age, email, gender, mobileNumber, location);
            } else {
                throw new IllegalStateException("Failed to fetch user profile.");
            }
        }
    });

        menuOptions.getChildren().addAll(myProfileButton, notificationMenu, logoutButton, themeComboBox);

        sidebar.getChildren().addAll(profileDetails, new Separator(), menuOptions);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(300);

        userListView = new ListView<>(activeUsers);
        userListView.setPrefHeight(100);
        userListView.setStyle("-fx-border-color: #cccccc;");

        inputField = new TextField();
        inputField.setPromptText("Type a message...");

        Button sendButton = createStyledButton("Send", "#4CAF50", "white");
        Button emojiButton = createStyledButton("😊 Emojis", "#FFA500", "white");
        Button clearChatButton = createStyledButton("Clear Chat", "#FF6347", "white");
        ComboBox<String> statusDropdown = new ComboBox<>();
        statusDropdown.getItems().addAll("Online", "Away", "Offline", "Busy");
        statusDropdown.setValue("Online");

        HBox inputBox = new HBox(10, inputField, sendButton, emojiButton, clearChatButton);

        statusLabel = new Label("[Status]");

        statusBox = new HBox(10, statusLabel, statusDropdown);

        activeUser = new Label ("Active Users:");

        chatSection.getChildren().addAll(chatArea, activeUser, userListView, inputBox, statusBox);

        root.setLeft(sidebar);
        root.setCenter(chatSection);

        Scene scene = new Scene(root, 900, 700);
        primaryStage.setTitle("User Chat Area");
        primaryStage.setScene(scene);
        primaryStage.show();

        //Connect to the server (dummy implementation for now)
        connectToServer(chatArea);

        //Send button action
        sendButton.setOnAction(event -> {
            String message = inputField.getText().trim();
            if (!message.isEmpty() && out != null) {
                out.println(message); //Send only the message, not the username
                inputField.clear();
            }
        });

        //Emoji button action
        emojiButton.setOnAction(event -> {
            showEmojiSelector(inputField);
        });

        //Clear Chat button action
        clearChatButton.setOnAction(event -> {
            chatArea.clear(); //Clear the chat area
        });

        //Context menu for private messaging
        ContextMenu userContextMenu = new ContextMenu();
        MenuItem privateMessageItem = new MenuItem("Send Private Message");
        userContextMenu.getItems().add(privateMessageItem);

        userListView.setContextMenu(userContextMenu);

        //Context menu action for private messaging
        privateMessageItem.setOnAction(event -> {
            String selectedUser = userListView.getSelectionModel().getSelectedItem();
            if (selectedUser != null && !selectedUser.equals(username)) {
                openPrivateChatWindow(selectedUser);
            }
        });

        //Status change action
        statusDropdown.setOnAction(event -> {
            String status = statusDropdown.getValue();
            if (out != null) {
                out.println("[Status]: " + status); //Notify the server about the status change
            }
        });

        //Logout button action
        logoutButton.setOnAction(event -> {
            if (out != null) {
                out.println("[LOGOUT]:" + username);
            }
            primaryStage.close(); //Close the application window
        });

    }

    private void changeChatTheme(String selectedTheme, VBox chatSection) {
        if ("Light Theme".equals(selectedTheme)) {

            //Apply background color to the whole chat section
            chatSection.setStyle("-fx-background-color: white; -fx-border-color: #D3D3D3;");

            //Update the styles of the TextArea (chatArea)
            chatArea.setStyle("-fx-background-color: white; -fx-text-fill: black;");

            //Apply the same background and text color to the user list (ListView)
            userListView.setStyle("-fx-border-color: #D3D3D3; -fx-background-color: white;");

            //Update the input field style
            inputField.setStyle("-fx-background-color: light gray; -fx-text-fill: black; -fx-border-color: #D3D3D3;");

            //Apply the theme to the sidebar
            sidebar.setStyle("-fx-background-color: light gray; -fx-border-color: #D3D3D3;");

            activeUser.setStyle("-fx-text-fill: black;");

            statusBox.setStyle("-fx-text-fill: black;");

            statusLabel.setStyle("-fx-text-fill: black;");

            nameLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");

            emailLabel.setStyle("-fx-text-fill: black;");

            myProfileButton.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-padding: 5 10 5 10; -fx-text-fill: #000000;");

            logoutButton.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-padding: 5 10 5 10; -fx-text-fill: #000000;");

            //Change text color of labels inside menu options
            for (Node node : menuOptions.getChildren()) {
                if (node instanceof Button) {
                    Button button = (Button) node;
                    button.setTextFill(javafx.scene.paint.Color.valueOf("-fx-text-fill: black;"));  //Change label text color
                } else if (node instanceof MenuButton) {
                    MenuButton menuButton = (MenuButton) node;
                    menuButton.setStyle("-fx-text-fill: black;");
                }
            }

            //Apply the background color for menu options
            menuOptions.setStyle("-fx-background-color: light gray;");

        } else {
            //dark theme
            //Apply background color to the whole chat section
            chatSection.setStyle("-fx-background-color: #383f52;");

            //Update the styles of the TextArea (chatArea)
            chatArea.setStyle("-fx-background-color: #383f52; -fx-text-fill: #464f66;");

            //Update the input field style
            inputField.setStyle("-fx-background-color: #1f2638; -fx-text-fill: white;");

            //Apply the theme to the sidebar
            sidebar.setStyle("-fx-background-color: #1f2638; -fx-border-color: #464f66;");

            activeUser.setStyle("-fx-text-fill: white;");

            statusBox.setStyle("-fx-text-fill: white;");

            statusLabel.setStyle("-fx-text-fill: white;");

            nameLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

            emailLabel.setStyle("-fx-text-fill: white;");

            myProfileButton.setStyle("-fx-background-color: #1f2638; -fx-font-size: 14px; -fx-padding: 5 10 5 10; -fx-text-fill: white;");

            logoutButton.setStyle("-fx-background-color: #1f2638; -fx-font-size: 14px; -fx-padding: 5 10 5 10; -fx-text-fill: white;");

            //Change text color of labels inside menu options
            for (Node node : menuOptions.getChildren()) {
                if (node instanceof Button) {
                    Button button = (Button) node;
                    button.setTextFill(javafx.scene.paint.Color.valueOf("-fx-text-fill: white;"));  //Change label text color
                } else if (node instanceof MenuButton) {
                    MenuButton menuButton = (MenuButton) node;
                    menuButton.setStyle("-fx-text-fill: white;");
                }
            }

            //Apply the background color for menu options
            menuOptions.setStyle("-fx-background-color:#383f52;");
        }
    }

    private Button createStyledButton(String text, String bgColor, String textColor) {
        Button button = new Button(text);
        button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-size: 14px; -fx-padding: 5 10 5 10; -fx-background-radius: 5;",
                bgColor, textColor));
        return button;
    }

    private Button createSidebarButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-padding: 5 10 5 10; -fx-text-fill: #000000;");
        return button;
    }

    private void connectToServer(TextArea chatArea) {
        new Thread(() -> {
            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println(username);

                //Simulate receiving messages from the server
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    if (serverMessage.startsWith("USERLIST:")) {
                        updateUserList(serverMessage.substring(9)); //Update active users
                    } else {
                        chatArea.appendText(serverMessage + "\n"); //Append messages to chat
                    }
                }
            } catch (IOException e) {
                chatArea.appendText("Error connecting to the server: " + e.getMessage() + "\n");
            }
        }).start();
    }

    //Show emoji selector and append emoji to input field
    private void showEmojiSelector(TextField inputField) {
        Stage emojiStage = new Stage();
        VBox emojiRoot = new VBox(10);
        emojiRoot.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9;");

        //Extended list of emojis
        String[] emojis = {
                "😊", "😂", "❤", "👍", "🎉", "😢", "🔥", "🎶", "✨", "😍", "😎", "🥳",
                "😁", "🤔", "🙌", "👀", "🎂", "🥺", "🏆", "😭", "🚀", "🐱", "🍕", "🌟", "📚", "🎵", "🌈",
                "🐶", "🐼", "🦄", "🍎", "🍔", "🥗", "🍩", "☕", "🌸", "💡", "🎁", "🚗",
                "✈", "🛳", "🏡", "📱", "💻", "📷", "🎨", "🏖", "🌅", "🛏", "🪴", "🎸",
                "🥋", "⚽", "🏀", "🏓", "🚴‍♂", "🏋‍♀", "🎯", "🥊", "🎱", "🧩", "🃏", "🎮",
                "🕹", "📖", "✏", "🖌", "📅", "📞", "🔔", "📦", "🛒", "🎵", "💎", "🪙"
        };

        //Create a GridPane for emojis
        GridPane emojiGrid = new GridPane();
        emojiGrid.setHgap(10);
        emojiGrid.setVgap(10);

        int column = 0;
        int row = 0;
        for (String emoji : emojis) {
            Button emojiButton = createStyledButton(emoji, "#ffffff", "#000000");
            emojiButton.setStyle(emojiButton.getStyle() + "-fx-border-color: #cccccc;");
            emojiButton.setOnAction(event -> {
                inputField.appendText(emoji);
                emojiStage.close();
            });
            emojiGrid.add(emojiButton, column, row);

            column++;
            if (column == 6) { //6 emojis per row
                column = 0;
                row++;
            }
        }

        //Add GridPane to a ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(emojiGrid);
        scrollPane.setFitToWidth(true); //Ensure the scroll pane fits the width
        scrollPane.setStyle("-fx-background-color: #f9f9f9;"); //Match the background
        scrollPane.setPrefHeight(300); //Set preferred height

        emojiRoot.getChildren().add(scrollPane);
        Scene emojiScene = new Scene(emojiRoot, 400, 400); //Increase the size for better display
        emojiStage.setScene(emojiScene);
        emojiStage.setTitle("Select Emoji");
        emojiStage.show();
    }

    private void openPrivateChatWindow(String recipient) {
        Stage privateChatStage = new Stage();
        VBox privateChatRoot = new VBox(10);
        privateChatRoot.setPadding(new Insets(10));
        privateChatRoot.setStyle("-fx-background-color: #f0f8ff;");

        TextArea privateChatArea = new TextArea();
        privateChatArea.setEditable(false);
        privateChatArea.setPrefHeight(300);

        TextField privateInputField = new TextField();
        privateInputField.setPromptText("Type a private message...");

        Button privateSendButton = new Button("Send");
        privateSendButton.setOnAction(event -> {
            String privateMessage = privateInputField.getText().trim();
            if (!privateMessage.isEmpty() && out != null) {
                out.println("(Private from " + username + " to " + recipient + " ) -> " + privateMessage);
                privateChatArea.appendText("Me: " + privateMessage + "\n");
                privateInputField.clear();
            }
        });

        privateChatRoot.getChildren().addAll(privateChatArea, privateInputField, privateSendButton);

        Scene privateChatScene = new Scene(privateChatRoot, 400, 300);
        privateChatStage.setScene(privateChatScene);
        privateChatStage.setTitle("Private Chat with " + recipient);
        privateChatStage.show();
    }

    //Update the active user list in the GUI
    private void updateUserList(String userList) {
        activeUsers.clear();
        String[] users = userList.split(",");
        activeUsers.addAll(users);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
