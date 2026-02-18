package gui;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.CsvHandler;
import model.Student;

import java.util.List;

public class Student_Panel {
    private String filePath = "src/data/student_data.csv";

    public Parent getView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));

        /*header */
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Students");
        title.getStyleClass().add("header-title");

        //search
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search students...");
        search.setStyle("-fx-background-radius: 20; -fx-padding: 10;");

        header. getChildren().addAll(title, spacer, search);

        //action buttons
        HBox toolbar = new HBox(15);
        Button add = new Button("+ Add a student");
        Button update = new Button("✎ Update student");
        Button delete = new Button("🗑 Delete student");

        add.getStyleClass().add("action-button");
        update.getStyleClass().add("action-button");
        delete.getStyleClass().add("action-button");

        toolbar.getChildren().addAll(add, update, delete);

        //table
        TableView<Student> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Student, String> id = new TableColumn<>("ID No.");
        id.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Student, String> first = new TableColumn<>("First Name");
        first.setCellValueFactory(new PropertyValueFactory<>("first"));

        TableColumn<Student, String> last = new TableColumn<>("Last Name");
        last.setCellValueFactory(new PropertyValueFactory<>("last"));

        TableColumn<Student, String> program = new TableColumn<>("Program");
        program.setCellValueFactory(new PropertyValueFactory<>("program"));

        TableColumn<Student, String> year = new TableColumn<>("Year");
        year.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<Student, String> gender = new TableColumn<>("Gender");
        gender.setCellValueFactory(new PropertyValueFactory<>("gender"));

        table.getColumns().addAll(id, first, last, program, year, gender);

        loadData(table);

        layout.getChildren().addAll(header, toolbar, table);
        return layout;
    }

    private void loadData(TableView<Student> table) {
    ObservableList<Student> students = FXCollections.observableArrayList();
    List<String[]> rawData = CsvHandler.readCSV(filePath);

        for (String[] row : rawData) {
            // Only process if the row has enough columns AND the first column isn't "ID" (header)
            if(row.length >= 6 && !row[0].equalsIgnoreCase("id")) {
                try {
                    // .trim() removes hidden spaces that cause parsing errors
                    int yearValue = Integer.parseInt(row[4].trim()); 
                    Student s = new Student(row[0], row[1], row[2], row[3], yearValue, row[5]);
                    students.add(s);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid row: " + String.join(",", row));
                }
            }
        }
        table.setItems(students);
    }
}