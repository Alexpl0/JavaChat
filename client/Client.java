// Importamos las clases necesarias para la comunicación en red y manejo de I/O.
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * La clase Client representa el programa que los usuarios ejecutarán para chatear.
 * Se conecta al servidor central, envía los mensajes que el usuario escribe y
 * escucha los mensajes que el servidor le reenvía.
 */
public class Client {

    // La dirección IP del servidor. "localhost" o "127.0.0.1" se usa para conectar
    // a un servidor que se ejecuta en la misma máquina.
    // Para conectar con otra máquina en la red, aquí iría su IP.
    private static final String SERVER_ADDRESS = "localhost";

    // El puerto debe ser el mismo que el servidor está escuchando.
    private static final int SERVER_PORT = 9090;

    public static void main(String[] args) {
        System.out.println("[CLIENT] Intentando conectar al servidor en " + SERVER_ADDRESS + ":" + SERVER_PORT);

        // Usamos un try-with-resources para el Socket y el Scanner,
        // asegurando que se cierren automáticamente al finalizar.
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner consoleScanner = new Scanner(System.in)) {

            System.out.println("[CLIENT] Conexión establecida con el servidor.");

            // Creamos e iniciamos un hilo separado para escuchar los mensajes del servidor.
            // Si no lo hiciéramos en un hilo aparte, el programa se bloquearía esperando
            // mensajes del servidor y no podríamos escribir los nuestros, o viceversa.
            ServerListener serverListener = new ServerListener(socket);
            new Thread(serverListener).start();

            // Bucle principal para leer los mensajes del usuario desde la consola y enviarlos.
            String userInput;
            System.out.println("Escribe tus mensajes (o 'salir' para desconectar):");
            
            // El bucle se mantiene mientras el usuario escribe en la consola.
            while ((userInput = consoleScanner.nextLine()) != null) {
                // Si el usuario escribe "salir", rompemos el bucle para cerrar la conexión.
                if ("salir".equalsIgnoreCase(userInput)) {
                    break;
                }
                // Enviamos el mensaje del usuario al servidor.
                out.println(userInput);
            }

        } catch (UnknownHostException e) {
            // Este error ocurre si la dirección del servidor no se puede resolver (ej. mal escrita).
            System.err.println("[CLIENT_ERROR] Host desconocido: " + SERVER_ADDRESS);
        } catch (IOException e) {
            // Este error puede ocurrir si el servidor no está en línea o rechaza la conexión.
            System.err.println("[CLIENT_ERROR] No se pudo conectar al servidor: " + e.getMessage());
        } finally {
            System.out.println("[CLIENT] Te has desconectado del chat.");
        }
    }
}

/**
 * La clase ServerListener se encarga de escuchar los mensajes entrantes del servidor.
 * Implementa Runnable para poder ejecutarse en su propio hilo.
 */
class ServerListener implements Runnable {
    private Socket socket;
    private BufferedReader in;

    public ServerListener(Socket socket) throws IOException {
        this.socket = socket;
        // Inicializamos el flujo de entrada para recibir mensajes del servidor.
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            String serverMessage;
            // Bucle infinito que espera mensajes del servidor.
            // readLine() se bloqueará hasta que llegue un mensaje.
            // Si el servidor se cae o cierra la conexión, readLine() devolverá null.
            while ((serverMessage = in.readLine()) != null) {
                // Imprimimos el mensaje del servidor en la consola del cliente.
                System.out.println(serverMessage);
            }
        } catch (IOException e) {
            // Este error suele indicar que la conexión con el servidor se ha perdido.
            System.out.println("[LISTENER_INFO] Se ha perdido la conexión con el servidor.");
        } finally {
            // Cerramos el flujo de entrada cuando el bucle termina.
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
