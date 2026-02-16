package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import model.CsvHandler;

public class Student_Panel extends JPanel{
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private String filepath = "src/data/student_data.csv";

    public Student_Panel() {
        setLayout(new BorderLayout(20,20));
        setBackground(Color.decode("#F1ECE4"));

        /*title & search section*/
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Students");
        title.setFont(new Font("Kameron", Font.BOLD, 64));
        title.setForeground(Color.decode("#68191F"));

        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        search.setOpaque(false);
        JTextField tfSearch = new JTextField(20);
        tfSearch.setText("Search students...");
        search.add(tfSearch);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(search, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);


        /*buttons + filter */
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        buttons.setOpaque(false);

        JButton add = createStyledButton("Add a student", "+");
        JButton update = createStyledButton("Update Student", "✎");
        JButton delete = createStyledButton("Delete student", "🗑");

        //sorting filter
        String[] sortfilter = {"Sort filter", "ID No."}

    }
    }
}
