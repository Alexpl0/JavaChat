import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatClientGUI {
    private JFrame frame;
    private JTextPane chatPane;
    private StyledDocument doc;
    private JTextField inputField;
    private JButton sendButton;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ChatClientGUI() {
        initializeGUI();
        connectToServer();
    }

    private void initializeGUI() {
        frame = new JFrame("Cliente de Chat - Java");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 450);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setFont(new Font("SansSerif", Font.PLAIN, 14));
        doc = chatPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        sendButton = new JButton("Enviar");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        // Hacemos visible el frame al final, después de la autenticación.
    }
    
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 9090);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Tarea 6.4: Bucle de autenticación que maneja el rechazo de nombres.
            while (true) {
                String line = in.readLine();
                if (line == null) { // El servidor cerró la conexión antes de tiempo
                    JOptionPane.showMessageDialog(frame, "El servidor cerró la conexión inesperadamente.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (line.startsWith("SUBMITNAME")) {
                    String name = JOptionPane.showInputDialog(frame, "Elige un nombre de usuario:", "Nombre de Usuario", JOptionPane.PLAIN_MESSAGE);
                    out.println(name != null ? name.trim() : "");
                } else if (line.startsWith("NAME_REJECTED")) {
                    JOptionPane.showMessageDialog(frame, "Ese nombre de usuario ya está en uso. Por favor, elige otro.", "Nombre no disponible", JOptionPane.ERROR_MESSAGE);
                } else if (line.startsWith("NAME_ACCEPTED")) {
                    frame.setTitle("Java Chat - " + in.readLine()); // El servidor podría enviar el nombre final
                    frame.setVisible(true); // Mostramos la ventana principal solo después de ser aceptados
                    break; // Salimos del bucle de autenticación
                }
            }

            // Una vez autenticados, iniciamos el hilo principal de escucha.
            new Thread(new ServerListener()).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "No se pudo conectar al servidor:\n" + e.getMessage(), "Error de conexión", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    // El resto del código permanece igual...

    private void appendMessage(String message, AttributeSet attr) {
        try {
            doc.insertString(doc.getLength(), message + "\n", attr);
            chatPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private void appendErrorMessage(String message) {
        SimpleAttributeSet errorAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(errorAttr, Color.RED);
        StyleConstants.setBold(errorAttr, true);
        StyleConstants.setFontSize(errorAttr, 14);
        StyleConstants.setFontFamily(errorAttr, "SansSerif");
        appendMessage(message, errorAttr);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            inputField.setText("");
        }
    }
    
    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    final String finalMessage = serverMessage;
                    SwingUtilities.invokeLater(() -> {
                        // Aquí ya no necesitamos manejar la autenticación, solo mensajes de chat.
                        SimpleAttributeSet attr = new SimpleAttributeSet();
                        StyleConstants.setFontFamily(attr, "SansSerif");
                        StyleConstants.setFontSize(attr, 14);

                        if (finalMessage.contains("(Privado de") || finalMessage.contains("(Mensaje para")) {
                            StyleConstants.setItalic(attr, true);
                            StyleConstants.setForeground(attr, new Color(0x008B8B));
                        } else if (finalMessage.contains("[SERVER]")) {
                            StyleConstants.setBold(attr, true);
                            StyleConstants.setForeground(attr, Color.GRAY);
                        }
                        
                        appendMessage(finalMessage, attr);
                    });
                }
            } catch (IOException e) {
                appendErrorMessage("[ERROR] Conexión perdida con el servidor.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}
