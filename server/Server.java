package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * La clase Server es el programa principal que actúa como el centro de nuestro chat.
 */
public class Server {

    private static final int PORT = 9090;
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println("[SERVER] El servidor de chat se está iniciando...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Servidor iniciado en el puerto: " + PORT);
            System.out.println("[SERVER] Esperando conexiones de clientes...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Nuevo cliente conectado: " + clientSocket.getInetAddress().getHostAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("[SERVER_ERROR] Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retransmite un mensaje a todos los clientes conectados.
     * @param message El mensaje a enviar.
     */
    public static void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * Retransmite la lista actualizada de usuarios a todos los clientes.
     */
    public static void broadcastUserList() {
        // Recolectamos los nombres de todos los clientes conectados.
        String userList = clients.stream()
                                 .map(ClientHandler::getUsername)
                                 .filter(username -> username != null && !username.isEmpty())
                                 .collect(Collectors.joining(","));
        // Enviamos el comando especial con la lista.
        broadcastMessage("!USERLIST " + userList);
    }

    /**
     * Elimina un cliente de la lista y anuncia su partida.
     * @param clientHandler El manejador del cliente a eliminar.
     */
    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("[SERVER] Cliente desconectado: " + clientHandler.getUsername() + ". Clientes restantes: " + clients.size());
        if (clientHandler.getUsername() != null) {
            broadcastMessage("[SERVER] " + clientHandler.getUsername() + " ha abandonado el chat.");
            // Actualizamos la lista de usuarios para todos los que quedan.
            broadcastUserList();
        }
    }
}

/**
 * La clase ClientHandler se encarga de la comunicación con un único cliente.
 */
class ClientHandler implements Runnable {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("SUBMITNAME");
            this.username = in.readLine();
            if (this.username == null || this.username.trim().isEmpty()) {
                this.username = "Anonimo-" + (int)(Math.random() * 1000);
            }
            
            System.out.println("[SERVER] " + clientSocket.getInetAddress().getHostAddress() + " ahora es " + username);
            Server.broadcastMessage("[SERVER] " + username + " se ha unido al chat.");
            // Enviamos la lista de usuarios actualizada a todos.
            Server.broadcastUserList();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String formattedMessage = "[" + username + "]: " + inputLine;
                System.out.println("[MESSAGE_RECEIVED] De " + username + ": " + inputLine);
                Server.broadcastMessage(formattedMessage);
            }
        } catch (IOException e) {
            System.out.println("[HANDLER_ERROR] Conexión perdida con " + (username != null ? username : "un cliente"));
        } finally {
            Server.removeClient(this);
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
