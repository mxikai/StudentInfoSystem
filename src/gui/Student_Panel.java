package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Student_Panel extends JPanel{
    private JTextField tfId, tfFirstName, tfLastName, tfProgram, tfYear, tfGender;
    private JTable studentTable;
    private DefaultTableModel tablemodel;
    private String filePath = "src/data/student_data.csv";

    public Student_Panel() {
        setLayout(new BorderLayout(10,10));

        /*text fields*/
        JPanel formPanel = new JPanel(new GridLayout(6,2,5,5));
        formPanel.add(new JLabel("ID (YYYY-NNNN):"));
        tfId = new JTextField();
        formPanel.add(tfId);

        formPanel.add(new JLabel("First Name:"));
        tfFirstName = new JTextField();
        formPanel.add(tfFirstName);

        formPanel.add(new JLabel("Last Name:"));
        tfLastName = new JTextField();
        formPanel.add(tfLastName);

        formPanel.add(new JLabel("Program Code (ex. BSCS):"));
        tfProgram = new JTextField();
        formPanel.add(tfProgram);

        formPanel.add(new JLabel("Year Level:"));
        tfYear = new JTextField();
        formPanel.add(tfYear);

        formPanel.add(new JLabel("Gender:"));
        tfGender = new JTextField();
        formPanel.add(tfGender);

        add(formPanel, BorderLayout.WEST);

        /*table*/
        String[] column = {"ID", "First Name", "Last Name", "Program Code", "Year Level", "Gender"};
        tablemodel = new DefaultTableModel(column, 0);
        studentTable = new JTable(tablemodel);
        JScrollPane scroll = new JScrollPane(studentTable);
        add(scroll, BorderLayout.CENTER);

        /*buttons */
        JPanel button = new JPanel();
        JButton add = new JButton("Add Student");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");

        button.add(add);
        button.add(update);
        button.add(delete);
        
        add(button, BorderLayout.SOUTH);
    }
}
