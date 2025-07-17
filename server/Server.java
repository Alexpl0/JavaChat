// server/Server.java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
// Tarea 6.2: Importamos las clases necesarias para manejar fechas y horas.
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
    // Tarea 6.2: Creamos un formateador de tiempo estático para usarlo en todo el servidor.
    // El formato "HH:mm" mostrará la hora y los minutos, por ejemplo: "17:30".
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
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("[SERVER_ERROR] Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retransmite un mensaje a todos los clientes, añadiéndole un timestamp.
     * @param message El mensaje original a enviar.
     */
    public static void broadcastMessage(String message) {
        // Tarea 6.2: Obtenemos la hora actual y la formateamos.
        String timestamp = dtf.format(LocalDateTime.now());
        String messageWithTimestamp = "[" + timestamp + "] " + message;
        
        for (ClientHandler client : clients) {
            client.sendMessage(messageWithTimestamp);
        }
    }
    
    /**
     * Envía un mensaje privado, añadiéndole un timestamp.
     * @param message El mensaje a enviar.
     * @param sender El manejador del cliente que envía el mensaje.
     * @param recipientUsername El nombre de usuario del destinatario.
     */
    public static void sendPrivateMessage(String message, ClientHandler sender, String recipientUsername) {
        Optional<ClientHandler> recipient = clients.stream()
                .filter(client -> recipientUsername.equalsIgnoreCase(client.getUsername()))
                .findFirst();
        
        // Tarea 6.2: Obtenemos la hora actual para añadirla a los mensajes.
        String timestamp = dtf.format(LocalDateTime.now());

        if (recipient.isPresent()) {
            // Formateamos el mensaje para el destinatario y el remitente, ambos con timestamp.
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
        // El comando !USERLIST es un comando de sistema, por lo que no le añadimos timestamp.
        // Lo enviamos directamente a través del método de cada cliente.
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

            out.println("SUBMITNAME");
            this.username = in.readLine();
            if (this.username == null || this.username.trim().isEmpty()) {
                this.username = "Anonimo-" + (int)(Math.random() * 1000);
            }
            
            System.out.println("[SERVER] " + clientSocket.getInetAddress().getHostAddress() + " ahora es " + username);
            Server.broadcastMessage("[SERVER] " + username + " se ha unido al chat.");
            Server.broadcastUserList();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("/msg")) {
                    String[] parts = inputLine.split(" ", 3);
                    if (parts.length == 3) {
                        String recipient = parts[1];
                        String message = parts[2];
                        Server.sendPrivateMessage(message, this, recipient);
                    } else {
                        // Este es un mensaje de error solo para este usuario, así que lo enviamos directamente.
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

    /**
     * Envía un mensaje directamente a este cliente.
     * @param message El mensaje completo ya formateado.
     */
    public void sendMessage(String message) {
        out.println(message);
    }
}
