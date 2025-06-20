package Project;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 1099;
    private static final Set<ClientHandler> clientHandlers = new CopyOnWriteArraySet<>();
    private static final List<String> activeUsers = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println("Chat Server started...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                //Accept new client connections
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                //Start a new thread for each new client
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Broadcast a message to all connected clients.
    public static void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(message);
        }
    }

    //Update and send the active user list to all connected clients.
    public static void updateUserList() {
        String userList = "USERLIST:" + String.join(",", activeUsers);
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(userList);
        }
    }

    //Remove a client from the server's active list.
    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        synchronized (activeUsers) {
            activeUsers.remove(clientHandler.clientName);
        }
        //Notify all clients of the updated user list
        updateUserList();
    }

    //ClientHandler to handle each client's communication.
    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                //Step 1: Read the username from the client
                clientName = in.readLine();

                //Step 2: Validate the username
                if (clientName == null || clientName.trim().isEmpty() || activeUsers.contains(clientName)) {
                    out.println("ERROR: Invalid or duplicate username!");
                    return; //Disconnect the client if invalid
                }

                //Step 3: Add the username to the active user list
                synchronized (activeUsers) {
                    activeUsers.add(clientName);
                }

                //Step 4: Notify the new client with the current active user list
                String userList = "USERLIST:" + String.join(",", activeUsers);
                out.println(userList); //Send the active user list directly to the new client

                //Step 5: Notify the client and other users
                out.println("Welcome to the chat, " + clientName + "!");
                System.out.println(clientName + " has joined the chat.");
                ChatServer.broadcastMessage(clientName + " has joined the chat.");
                ChatServer.updateUserList();
                clientHandlers.add(this);

                //Step 6: Handle messages from this client
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    }
                    ChatServer.broadcastMessage(clientName + ": " + message);
                }
            } catch (IOException e) {
                System.out.println("Connection with " + clientName + " lost.");
            } finally {
                cleanup();
            }
        }

        //Cleanup resources and notify others of the disconnection
        private void cleanup() {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ChatServer.removeClient(this);
            ChatServer.broadcastMessage(clientName + " has left the chat.");
            System.out.println(clientName + " has left the chat.");
        }

        //Send a message to this client
        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }
}