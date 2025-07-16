import javax.swing.*;

public class ChatClientGUI {

    private JFrame frame;

    public ChatClientGUI() {
        frame = new JFrame("Cliente de Chat");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}
