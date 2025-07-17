import java.awt.*;
import javax.swing.*;

public class ChatWindow extends JPanel {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    public ChatWindow() {
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("Enviar");

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    public JTextArea getChatArea() { return chatArea; }
    public JTextField getInputField() { return inputField; }
    public JButton getSendButton() { return sendButton; }
}
