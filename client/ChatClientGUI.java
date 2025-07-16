import java.awt.*;
import javax.swing.*;  

public class ChatClientGUI {

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;

    public ChatClientGUI() {
   
        frame = new JFrame("Cliente de Chat");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());  

    
        chatArea = new JTextArea();
        chatArea.setEditable(false);

        inputField = new JTextField();

        
        frame.add(chatArea, BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}
