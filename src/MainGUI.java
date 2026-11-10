import javax.swing.SwingUtilities;

public class MainGUI {
    public static void main(String[] args) {
        // Swing windows should be created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new TypingGameWindow().setVisible(true));
    }
}