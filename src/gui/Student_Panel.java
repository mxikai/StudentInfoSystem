package gui;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.CsvHandler;
import model.Student;

import java.util.ArrayList;
import java.util.List;

public class Student_Panel {
    private String filePath = "src/data/student_data.csv";
    private TableView<Student> table; // Shared class variable

    public Parent getView() {
        VBox layout = new VBox(25);
        layout.setPadding(new Insets(30));

        /*header */
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("top-header-bar");

        Label title = new Label("Students");
        title.getStyleClass().add("top-header-title");

        //search
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search students...");
        search.getStyleClass().add("search-bar");

        header.getChildren().addAll(title, spacer, search);

        /*action buttons */
        HBox toolbar = new HBox(15);
        Button add = new Button("+ Add a student");
        Button update = new Button("✎ Update student");
        Button delete = new Button("🗑 Delete student");

        add.getStyleClass().add("action-button");
        update.getStyleClass().add("action-button");
        delete.getStyleClass().add("action-button");

        toolbar.getChildren().addAll(add, update, delete);

        /*sort */
        HBox sortBox = new HBox();
        sortBox.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> sortFilter = new ComboBox<>();
        // Update the items list:
        sortFilter.getItems().addAll(
            "Sort filter ˅", 
            "ID No. (Asc)", "ID No. (Desc)", 
            "Last Name (Asc)", "Last Name (Desc)", 
            "Year (Asc)", "Year (Desc)", 
            "Gender (Asc)", "Gender (Desc)"
        );
        sortFilter.setValue("Sort filter");
        sortFilter.getStyleClass().add("sort-combo-box");
        sortBox.getChildren().add(sortFilter);

        /* table */
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Student, String> id = new TableColumn<>("ID No.");
        id.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Student, String> first = new TableColumn<>("First Name");
        first.setCellValueFactory(new PropertyValueFactory<>("firstName")); 

        TableColumn<Student, String> last = new TableColumn<>("Last Name");
        last.setCellValueFactory(new PropertyValueFactory<>("lastName")); 

        TableColumn<Student, String> program = new TableColumn<>("Program");
        program.setCellValueFactory(new PropertyValueFactory<>("programCode"));

        TableColumn<Student, Integer> year = new TableColumn<>("Year");
        year.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<Student, String> gender = new TableColumn<>("Gender");
        gender.setCellValueFactory(new PropertyValueFactory<>("gender"));

        table.getColumns().addAll(id, first, last, program, year, gender);

        loadData();

        /*action listeners */
        //sort logic
        sortFilter.setOnAction(e -> {
            String selected = sortFilter.getValue();
            table.getSortOrder().clear();

            if (selected.equals("Sort filter")) return;

            //asc or desc
            TableColumn.SortType sortType = selected.contains("(Desc)") ? 
                TableColumn.SortType.DESCENDING : TableColumn.SortType.ASCENDING;

            TableColumn<Student, ?> columnToSort = null;
            
            if (selected.startsWith("ID No.")) {
                columnToSort = id;
            } else if (selected.startsWith("Last Name")) {
                columnToSort = last;
            } else if (selected.startsWith("Year")) {
                columnToSort = year;
            } else if (selected.startsWith("Gender")) {
                columnToSort = gender;
            }

            //automatic
            if (columnToSort != null) {
                columnToSort.setSortType(sortType);
                table.getSortOrder().add(columnToSort);
                table.sort();
            }
        });

        //upd
        add.setOnAction(e -> showAddOrUpdateDialog(null));
        
        update.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAddOrUpdateDialog(selected);
            }
        });

        delete.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showDeleteConfirmation(selected);
            }
        });

        table.setRowFactory(tv -> {
            TableRow<Student> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    showStudentInfoDialog(row.getItem());
                }
            });
            return row;
        });
        
        layout.getChildren().addAll(header, toolbar, sortBox, table);
        return layout;
    }

    // Removed the parameter so it stops confusing VS Code
    private void loadData() {
        ObservableList<Student> students = FXCollections.observableArrayList();
        List<String[]> rawData = CsvHandler.readCSV(filePath);

        for (String[] row : rawData) {
            if(row.length >= 6 && !row[0].equalsIgnoreCase("id")) {
                try {
                    int yearValue = Integer.parseInt(row[4].trim()); 
                    Student s = new Student(row[0].trim(), row[1].trim(), row[2].trim(), row[3].trim(), yearValue, row[5].trim());
                    students.add(s);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid row: " + String.join(",", row));
                }
            }
        }
        table.setItems(students);
    }

    private void refreshCSV() {
        List<String> lines = new ArrayList<>();
        lines.add("id,firstName,lastName,programCode,year,gender");
        for (Student s : table.getItems()) {
            lines.add(s.toCSV());
        }
        CsvHandler.overwriteCSV(filePath, lines);
    }

    /*overlay */
    private void showAddOrUpdateDialog(Student studentToUpdate) {
        boolean isUpdate = (studentToUpdate != null);
        
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(StageStyle.TRANSPARENT);

        //overlay
        VBox root = new VBox(15);
        root.getStyleClass().add("modal-overlay");
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        //title n close
        HBox topHeader = new HBox();
        topHeader.setAlignment(Pos.CENTER_RIGHT);
        Label systemTitle = new Label("Simple Student Information System");
        systemTitle.setStyle("-fx-text-fill: #68191F; -fx-font-weight: bold; -fx-font-size: 14px; -fx-font-family: 'Courier New', monospace;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("X");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #68191F; -fx-font-weight: bold; -fx-font-size: 18px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> modal.close());
        
        topHeader.getChildren().addAll(systemTitle, spacer, closeBtn);

        //box
        VBox maroonBox = new VBox(15);
        maroonBox.getStyleClass().add("modal-content");
        maroonBox.setAlignment(Pos.CENTER);

        Label title = new Label(isUpdate ? "Update Student" : "Add Student");
        title.getStyleClass().add("modal-title");

        maroonBox.getChildren().add(title);

        TextField idField = createLabeledField(maroonBox, "ID Number", isUpdate ? studentToUpdate.getId() : "");
        TextField firstField = createLabeledField(maroonBox, "First Name", isUpdate ? studentToUpdate.getFirstName() : "");
        TextField lastField = createLabeledField(maroonBox, "Last Name", isUpdate ? studentToUpdate.getLastName() : "");
        TextField progField = createLabeledField(maroonBox, "Program", isUpdate ? studentToUpdate.getProgramCode() : "");
        TextField yearField = createLabeledField(maroonBox, "Year", isUpdate ? String.valueOf(studentToUpdate.getYear()) : "");
        TextField genderField = createLabeledField(maroonBox, "Gender", isUpdate ? studentToUpdate.getGender() : "");

        Button actionBtn = new Button(isUpdate ? "Update" : "Add");
        actionBtn.getStyleClass().add("modal-button");
        VBox.setMargin(actionBtn, new Insets(20, 0, 0, 0));

        actionBtn.setOnAction(e -> {
            try {
                String id = idField.getText();
                String first = firstField.getText();
                String last = lastField.getText();
                String prog = progField.getText();
                int year = Integer.parseInt(yearField.getText());
                String gender = genderField.getText();

                if (isUpdate) {
                    studentToUpdate.setId(id);
                    studentToUpdate.setFirstName(first);
                    studentToUpdate.setLastName(last);
                    studentToUpdate.setProgramCode(prog);
                    studentToUpdate.setYear(year);
                    studentToUpdate.setGender(gender);
                    table.refresh();
                } else {
                    Student newStudent = new Student(id, first, last, prog, year, gender);
                    table.getItems().add(newStudent);
                }
                refreshCSV();
                modal.close();
            } catch (NumberFormatException ex) {
                System.out.println("Invalid year input.");
            }
        });

        maroonBox.getChildren().add(actionBtn);
        root.getChildren().addAll(topHeader, maroonBox);
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private TextField createLabeledField(VBox parent, String labelText, String initialValue) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(labelText);
        label.getStyleClass().add("modal-label");
        TextField field = new TextField(initialValue);
        field.getStyleClass().add("modal-textfield");
        container.getChildren().addAll(label, field);
        
        parent.getChildren().add(container); 
        return field;
    }

    private void showStudentInfoDialog(Student student) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.getStyleClass().add("modal-overlay");
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        HBox topHeader = new HBox();
        topHeader.setAlignment(Pos.CENTER_RIGHT);
        Button closeBtn = new Button("X");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #68191F; -fx-font-weight: bold; -fx-font-size: 18px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> modal.close());
        topHeader.getChildren().add(closeBtn);

        VBox box = new VBox(15);
        box.getStyleClass().add("modal-content");
        box.setAlignment(Pos.CENTER);

        Circle profilePic = new Circle(40);
        profilePic.getStyleClass().add("profile-placeholder");

        Label nameTitle = new Label(student.getLastName() + "\n" + student.getFirstName());
        nameTitle.getStyleClass().add("modal-title");
        nameTitle.setStyle("-fx-font-size: 24px; -fx-text-alignment: center;");

        box.getChildren().addAll(profilePic, nameTitle);

        createLabeledField(box, "Program", student.getProgramCode()).setEditable(false);
        createLabeledField(box, "Year", String.valueOf(student.getYear())).setEditable(false);
        createLabeledField(box, "College", "[Requires DB Link]").setEditable(false); 
        createLabeledField(box, "Gender", student.getGender()).setEditable(false);

        root.getChildren().addAll(topHeader, box);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private void showDeleteConfirmation(Student student) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(StageStyle.TRANSPARENT);

        VBox box = new VBox(20);
        box.getStyleClass().add("modal-content");
        box.setAlignment(Pos.CENTER);

        Label prompt = new Label("Are you sure you want to delete\n" + student.getFirstName() + " " + student.getLastName() + "?");
        prompt.getStyleClass().add("modal-label");
        prompt.setStyle("-fx-font-size: 18px; -fx-text-alignment: center;");

        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);
        
        Button yesBtn = new Button("Delete " + student.getFirstName() + " " + student.getLastName());
        yesBtn.getStyleClass().add("modal-button");
        yesBtn.setOnAction(e -> {
            table.getItems().remove(student);
            refreshCSV();
            modal.close();
        });

        Button noBtn = new Button("Cancel");
        noBtn.getStyleClass().add("modal-button");
        noBtn.setOnAction(e -> modal.close());

        btnBox.getChildren().addAll(yesBtn, noBtn);
        box.getChildren().addAll(prompt, btnBox);

        VBox root = new VBox(box);
        root.getStyleClass().add("modal-overlay");
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        modal.setScene(scene);
        modal.showAndWait();
    }
}