package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
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
        String[] sortfilter = {"Sort filter", "ID No.", "Last Name", "Program"};
        JComboBox<String> sort = new JComboBox<>(sortfilter);

        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(sort);

        JPanel centerContainer = new JPanel(new BorderLayout(10,10));
        centerContainer.setOpaque(false);
        centerContainer.add(buttons, BorderLayout.NORTH);

        /*main table */
        String[] columns = {"ID No.", "First Name", "Last Name", "Program", "Year", "Gender"};
        tableModel = new DefaultTableModel(columns, 0);
        studentTable = new JTable(tableModel);

        loadDatafromCSV();

        JScrollPane scroll = new JScrollPane(studentTable);
        centerContainer.add(scroll, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);

        /*button actions */
        add.addActionListener( e ->{
            //will add later
            /*StudentDialog dialog = new StudentDialog(null);
            dialog.setVisible(true);*/
            JOptionPane.showMessageDialog(null, "This will open overlay later");
        });
    }
    
        //designing buttons
        private JButton createStyledButton(String text, String symbol) {
            JButton bttn = new JButton(symbol + " " + text);
            bttn.setBackground(Color.decode("#68191F"));
            bttn.setForeground(Color.WHITE);
            bttn.setFocusPainted(false);
            return bttn;
        }

        private void loadDatafromCSV() {
            //reuse
            java.util.List<String[]> data = CsvHandler.readCSV(filepath);
            for (String[] row : data) {
                tableModel.addRow(row);
            }
    }
}
