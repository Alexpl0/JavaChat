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
        // Tarea 6.5: Cambiamos la operación de cierre para manejarla manualmente.
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(600, 450);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Tarea 6.5: Añadimos un WindowListener para interceptar el evento de cierre.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(frame,
                        "¿Estás seguro de que quieres salir del chat?",
                        "Confirmar Salida",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    // Si el usuario confirma, cerramos la aplicación.
                    // Esto también cerrará el socket y notificará al servidor.
                    System.exit(0);
                }
            }
        });

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
    }
    
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 9090);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String line = in.readLine();
                if (line == null) {
                    JOptionPane.showMessageDialog(frame, "El servidor cerró la conexión inesperadamente.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (line.startsWith("SUBMITNAME")) {
                    String name = JOptionPane.showInputDialog(frame, "Elige un nombre de usuario:", "Nombre de Usuario", JOptionPane.PLAIN_MESSAGE);
                    out.println(name != null ? name.trim() : "");
                } else if (line.startsWith("NAME_REJECTED")) {
                    JOptionPane.showMessageDialog(frame, "Ese nombre de usuario ya está en uso. Por favor, elige otro.", "Nombre no disponible", JOptionPane.ERROR_MESSAGE);
                } else if (line.startsWith("NAME_ACCEPTED")) {
                    // El servidor ahora debe enviar el nombre final para ponerlo en el título.
                    String finalName = in.readLine();
                    frame.setTitle("Java Chat - " + finalName);
                    frame.setVisible(true);
                    break;
                }
            }

            new Thread(new ServerListener()).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "No se pudo conectar al servidor:\n" + e.getMessage(), "Error de conexión", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
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
