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
        // Se ha eliminado la librería externa para simplificar.
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

        // Se elimina la barra superior y el botón de ajustes.
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }
    
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 9090);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        final String finalLine = line;
                        SwingUtilities.invokeLater(() -> {
                             if (finalLine.equals("SUBMITNAME")) {
                                String name = JOptionPane.showInputDialog(frame, "Ingresa tu nombre de usuario:");
                                out.println(name != null && !name.trim().isEmpty() ? name.trim() : "Anónimo");
                            } else {
                                SimpleAttributeSet attr = new SimpleAttributeSet();
                                StyleConstants.setFontFamily(attr, "SansSerif");
                                StyleConstants.setFontSize(attr, 14);

                                if (finalLine.startsWith("(Privado") || finalLine.startsWith("(Mensaje para")) {
                                    StyleConstants.setItalic(attr, true);
                                    StyleConstants.setForeground(attr, new Color(0x008B8B)); // Un color cian oscuro
                                } else if (finalLine.startsWith("[SERVER]")) {
                                    StyleConstants.setBold(attr, true);
                                    StyleConstants.setForeground(attr, Color.GRAY);
                                }
                                
                                appendMessage(finalLine, attr);
                            }
                        });
                    }
                } catch (IOException e) {
                    appendErrorMessage("[ERROR] Conexión perdida con el servidor.");
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "No se pudo conectar al servidor:\n" + e.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}
