import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.util.*;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;

public class ChatClientGUI {
    private JFrame frame;
    private JTextPane chatPane;
    private StyledDocument doc;
    private JTextField inputField;
    private JButton sendButton;
    private JButton settingsButton;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private boolean isDarkMode = false;

    public ChatClientGUI() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {}

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
        chatPane.setBackground(UIManager.getColor("Panel.background"));
        doc = chatPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        sendButton = new JButton("Enviar");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        sendButton.setBackground(new Color(0x007BFF));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Barra superior con botón de ajustes
        settingsButton = new JButton("Ajustes");
        settingsButton.setFocusPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        settingsButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topBar.add(settingsButton, BorderLayout.EAST);

        // Menú emergente para modo claro/oscuro
        JPopupMenu settingsMenu = new JPopupMenu();
        JMenuItem lightMode = new JMenuItem("Modo Claro");
        JMenuItem darkMode = new JMenuItem("Modo Oscuro");

        lightMode.addActionListener(e -> switchTheme(false));
        darkMode.addActionListener(e -> switchTheme(true));

        settingsMenu.add(lightMode);
        settingsMenu.add(darkMode);

        settingsButton.addActionListener(e -> {
            settingsMenu.show(settingsButton, 0, settingsButton.getHeight());
        });

        frame.add(topBar, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void switchTheme(boolean dark) {
        try {
            isDarkMode = dark;
            UIManager.setLookAndFeel(dark ? new FlatDarkLaf() : new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "No se pudo cambiar el tema", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
                        if (line.equals("SUBMITNAME")) {
                            String name = JOptionPane.showInputDialog(frame, "Ingresa tu nombre de usuario:");
                            out.println(name != null && !name.trim().isEmpty() ? name.trim() : "Anónimo");
                        } else {
                            animateMessage(line);
                        }
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

    // Animación de llegada del mensaje
    private void animateMessage(String message) {
        int fontMin = 8;
        int fontMax = 14;

        // Estilo inicial con fuente pequeña
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setFontSize(attr, fontMin);
        StyleConstants.setFontFamily(attr, "SansSerif");

        int insertPos = doc.getLength();

        try {
            doc.insertString(insertPos, message + "\n", attr);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }

        // Timer para animar el cambio de tamaño
        Timer timer = new Timer(5, null);
        final int[] size = {fontMin};

        timer.addActionListener(e -> {
            if (size[0] < fontMax) {
                size[0]++;
                StyleConstants.setFontSize(attr, size[0]);

                // Aplicar nuevo tamaño solo al mensaje recién insertado
                doc.setCharacterAttributes(insertPos, message.length(), attr, true);
                chatPane.setCaretPosition(doc.getLength());
            } else {
                timer.stop();
            }
        });

        timer.start();
    }

    private void appendErrorMessage(String message) {
        SimpleAttributeSet errorAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(errorAttr, Color.RED);
        StyleConstants.setBold(errorAttr, true);
        StyleConstants.setFontSize(errorAttr, 14);
        StyleConstants.setFontFamily(errorAttr, "SansSerif");

        try {
            doc.insertString(doc.getLength(), message + "\n", errorAttr);
            chatPane.setCaretPosition(((JTextComponent) doc).getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
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