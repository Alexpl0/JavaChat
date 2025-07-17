import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * La clase Server es el programa principal que actúa como el centro de nuestro chat.
 */
public class Server {

    private static final int PORT = 9090;
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

    public static void main(String[] args) {
        System.out.println("[SERVER] El servidor de chat se está iniciando...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Servidor iniciado en el puerto: " + PORT);
            System.out.println("[SERVER] Esperando conexiones de clientes...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Nuevo cliente conectado: " + clientSocket.getInetAddress().getHostAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                // No añadimos el cliente a la lista principal hasta que tenga un nombre válido.
                // Se hará desde el propio ClientHandler.
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("[SERVER_ERROR] Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tarea 6.3: Verifica si un nombre de usuario ya está en uso.
     * Es 'synchronized' para evitar problemas de concurrencia si dos usuarios
     * intentan registrarse con el mismo nombre al mismo tiempo.
     * @param username El nombre a verificar.
     * @return true si el nombre ya está tomado, false en caso contrario.
     */
    public static synchronized boolean isUsernameTaken(String username) {
        return clients.stream().anyMatch(client -> username.equalsIgnoreCase(client.getUsername()));
    }

    /**
     * Tarea 6.3: Añade un cliente a la lista de clientes activos.
     * @param clientHandler El manejador del cliente a añadir.
     */
    public static void addClient(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public static void broadcastMessage(String message) {
        String timestamp = dtf.format(LocalDateTime.now());
        String messageWithTimestamp = "[" + timestamp + "] " + message;
        
        for (ClientHandler client : clients) {
            client.sendMessage(messageWithTimestamp);
        }
    }
    
    public static void sendPrivateMessage(String message, ClientHandler sender, String recipientUsername) {
        Optional<ClientHandler> recipient = clients.stream()
                .filter(client -> recipientUsername.equalsIgnoreCase(client.getUsername()))
                .findFirst();
        
        String timestamp = dtf.format(LocalDateTime.now());

        if (recipient.isPresent()) {
            recipient.get().sendMessage("[" + timestamp + "] (Privado de " + sender.getUsername() + "): " + message);
            sender.sendMessage("[" + timestamp + "] (Mensaje para " + recipientUsername + "): " + message);
        } else {
            sender.sendMessage("[" + timestamp + "] [SERVER] Usuario '" + recipientUsername + "' no encontrado o no está conectado.");
        }
    }

    public static void broadcastUserList() {
        String userList = clients.stream()
                                 .map(ClientHandler::getUsername)
                                 .filter(username -> username != null && !username.isEmpty())
                                 .collect(Collectors.joining(","));
        for (ClientHandler client : clients) {
            client.sendMessage("!USERLIST " + userList);
        }
    }

    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        if (clientHandler.getUsername() != null) {
            System.out.println("[SERVER] Cliente desconectado: " + clientHandler.getUsername() + ". Clientes restantes: " + clients.size());
            broadcastMessage("[SERVER] " + clientHandler.getUsername() + " ha abandonado el chat.");
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

            // --- Tarea 6.3: Bucle de autenticación ---
            // El cliente no será añadido a la lista principal hasta que tenga un nombre válido.
            while (true) {
                out.println("SUBMITNAME");
                String name = in.readLine();

                if (name == null || name.trim().isEmpty()) {
                    name = "Anonimo-" + (int)(Math.random() * 1000);
                }

                if (!Server.isUsernameTaken(name)) {
                    this.username = name;
                    Server.addClient(this); // Añadimos el cliente a la lista de activos
                    out.println("NAME_ACCEPTED"); // Enviamos la confirmación al cliente
                    break; // Salimos del bucle de autenticación
                } else {
                    out.println("NAME_REJECTED"); // Enviamos el rechazo al cliente
                }
            }
            
            System.out.println("[SERVER] " + clientSocket.getInetAddress().getHostAddress() + " ahora es " + username);
            Server.broadcastMessage("[SERVER] " + username + " se ha unido al chat.");
            Server.broadcastUserList();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("/msg")) {
                    String[] parts = inputLine.split(" ", 3);
                    if (parts.length == 3) {
                        Server.sendPrivateMessage(parts[2], this, parts[1]);
                    } else {
                        String timestamp = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now());
                        sendMessage("[" + timestamp + "] [SERVER] Formato incorrecto. Usa: /msg <usuario> <mensaje>");
                    }
                } else {
                    String formattedMessage = "[" + username + "]: " + inputLine;
                    System.out.println("[MESSAGE_RECEIVED] De " + username + ": " + inputLine);
                    Server.broadcastMessage(formattedMessage);
                }
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
