import java.awt.*;
import javax.swing.*;

public class ChatClientGUI {

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    public ChatClientGUI() {
        frame = new JFrame("Cliente de Chat");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        inputField = new JTextField();

        sendButton = new JButton("Enviar");
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(chatArea);

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}
