import gui.Student_Panel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class App {
    private static CardLayout cardLayout ;
    private static JPanel mainContent;

    public static void main(String[] args) {
        /*look and feel */
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
        } catch (Exception e) {
            System.out.println("Look and Feel failed.");
        }

        /*main window*/
        JFrame frame = new JFrame("Simple Student Information System");
        frame.setSize(1000,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        /*sidebar */
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.decode("#68191F"));
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        /*titel*/
        JLabel title = new JLabel("<html>Simple<br>Student<br>Information<br>System</html>");
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        /*tabs */
        JButton students = createNavButton("Students");
        JButton programs = createNavButton("Programs");
        JButton colleges = createNavButton("Colleges");

        sidebar.add(title);
        sidebar.add(Box.createRigidArea(new Dimension(0,80)));
        sidebar.add(students);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(programs);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(colleges);


        /*main pt 2 */
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(Color.decode("#68191F"));

        Student_Panel studentpanel = new Student_Panel();
        JPanel programpanel = new JPanel(); //placeholder
        JPanel collegepanel = new JPanel(); //placeholder

        mainContent.add(studentpanel, "STUDENTS");
        mainContent.add(programpanel, "PROGRAMS");
        mainContent.add(collegepanel, "COLLEGES");

        students.addActionListener(e -> cardLayout.show(mainContent, "STUDENTS"));
        programs.addActionListener(e -> cardLayout.show(mainContent, "PROGRAMS"));
        colleges.addActionListener(e -> cardLayout.show(mainContent, "COLLEGES"));
        
        frame.add(sidebar, BorderLayout.WEST);
        frame.add(mainContent, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private static JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(Color.decode("#68191F"));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        return btn;
    }
}

