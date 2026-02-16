import gui.Student_Panel;
import javax.swing.*;

import com.formdev.flatlaf.FlatDarkLaf;

public class App {
    public static void main(String[] args) {

        try {
            FlatDarkLaf.setup(); 
        } catch (Exception e) {
            System.out.println("Look and Feel failed to initialize");
        }
        
        JFrame frame = new JFrame("Simple Student Information System");
        frame.setSize(1440, 1024);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Student_Panel studentPanel = new Student_Panel();
        frame.add(studentPanel);

        frame.setVisible(true);
    }
}

