package popup;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

import static java.awt.BorderLayout.AFTER_LINE_ENDS;

/**
 * Manages the popup that will indicate to the user the process of the installation/update
 */
public enum PopupManager {
    INSTANCE;

    private static final System.Logger log = System.getLogger(PopupManager.class.getName());

    private JFrame jFrame;
    private JPanel jPanel;
    private JLabel progressBarJLabel;
    private static final Font DEFAULT_FONT = new Font("Verdana", Font.PLAIN, 18);

    PopupManager() {
        createJFrame();
    }

    private void createJFrame() {
        jFrame = new JFrame();
        jPanel = new JPanel();
        BoxLayout boxlayout = new BoxLayout(jPanel, BoxLayout.Y_AXIS);

        jPanel.setLayout(boxlayout);
        jFrame.add(jPanel);
        jFrame.setTitle("Ere.health application maintenance");
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jFrame.setSize(700, 500);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        jFrame.setLocation(dim.width / 2 - jFrame.getSize().width / 2, dim.height / 2 - jFrame.getSize().height / 2);
        jFrame.setVisible(true);

        displayERELogo();
    }

    private void displayERELogo() {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("ere_logo.png");
        ImageIcon icon= null;

        try {
            icon = new ImageIcon(ImageIO.read(resourceAsStream));
        } catch (IOException e) {
            log.log(System.Logger.Level.ERROR, "Could not load the ERE logo");
            e.printStackTrace();
        }

        JLabel iconLabel = new JLabel(icon);
        jPanel.add(iconLabel);
        jPanel.revalidate();
        jPanel.repaint();
    }

    public void addTextToPanelAndLog(String text) {
        //html tag for text wrap-up
        JLabel jLabel = new JLabel("<html>"+ text +"</html>");
        jLabel.setFont(DEFAULT_FONT);

        jPanel.add(jLabel, BorderLayout.AFTER_LINE_ENDS);
        jPanel.add(new JLabel(" "));
        jPanel.revalidate();
        jPanel.repaint();

        log.log(System.Logger.Level.INFO, text);
    }

    public void closePopup() {
        jFrame.dispatchEvent(new WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING));
    }

    public void startProgressBar() {
        progressBarJLabel = new JLabel();
        progressBarJLabel.setFont(DEFAULT_FONT);
        jPanel.add(progressBarJLabel, AFTER_LINE_ENDS);
        jPanel.add(new JLabel(" "));
    }

    public void closeProgressBar() {
        jPanel.remove(progressBarJLabel);
    }

    public void updateProgressBar(String progress) {
        progressBarJLabel.setText("<html>"+ progress +"</html>");
        jPanel.revalidate();
        jPanel.repaint();
    }
}
