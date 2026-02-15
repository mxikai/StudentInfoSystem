import gui.Student_Panel;
import javax.swing.*;

public class App {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Simple Student Information System");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Student_Panel studentPanel = new Student_Panel();
        frame.add(studentPanel);

        frame.setVisible(true);
    }
}

