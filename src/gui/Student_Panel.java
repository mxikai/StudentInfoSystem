package gui;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.*;
import model.CsvHandler;
import model.Student;

import java.util.ArrayList;
import java.util.Comparator;
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
        search.setPrefWidth(350);

        header.getChildren().addAll(title, spacer, search);

        /*action buttons */
        HBox toolbar = new HBox(15);
        Button add = new Button("+ Add a student");
        Button update = new Button("✎ Update student");
        Button delete = new Button("🗑 Delete student");

        add.getStyleClass().add("action-button");
        update.getStyleClass().add("action-button");
        delete.getStyleClass().add("action-button");

        add.setMaxWidth(Double.MAX_VALUE);
        update.setMaxWidth(Double.MAX_VALUE);
        delete.setMaxWidth(Double.MAX_VALUE);
        
        HBox.setHgrow(add, Priority.ALWAYS);
        HBox.setHgrow(update, Priority.ALWAYS);
        HBox.setHgrow(delete, Priority.ALWAYS);

        toolbar.getChildren().addAll(add, update, delete);

        /*sort */
        HBox sortBox = new HBox();
        sortBox.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll(
            "Sort filter", 
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
        table.getColumns().forEach(col -> {
            col.setSortable(false);
            col.setReorderable(false); 
        });

        loadData();

        /*action listeners */
        //sort logic
        sortFilter.setOnAction(e -> {
            String selected = sortFilter.getValue();
            if (selected == null || selected.equals("Sort filter")) return;

            ObservableList<Student> items = table.getItems();

            if (selected.equals("ID No. (Asc)")) {
                items.sort(Comparator.comparing(Student::getId));
            } else if (selected.equals("ID No. (Desc)")) {
                items.sort(Comparator.comparing(Student::getId).reversed());
            } else if (selected.equals("Last Name (Asc)")) {
                items.sort(Comparator.comparing(Student::getLastName));
            } else if (selected.equals("Last Name (Desc)")) {
                items.sort(Comparator.comparing(Student::getLastName).reversed());
            } else if (selected.equals("Year (Asc)")) {
                items.sort(Comparator.comparingInt(Student::getYear));
            } else if (selected.equals("Year (Desc)")) {
                items.sort(Comparator.comparingInt(Student::getYear).reversed());
            } else if (selected.equals("Gender (Asc)")) {
                items.sort(Comparator.comparing(Student::getGender));
            } else if (selected.equals("Gender (Desc)")) {
                items.sort(Comparator.comparing(Student::getGender).reversed());
            }
        });

        //search logic
        search.setOnAction(e -> {
            String query = search.getText().trim().toLowerCase();
            
            // If search is empty, clear selection and RESET the table order!
            if (query.isEmpty()) {
                table.getSelectionModel().clearSelection();
                table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                loadData(); 
                return;
            }

            boolean isIdFormat = query.matches("^\\d{4}-\\d{4}$");
            boolean hasNumbers = query.matches(".*\\d.*");

            if (hasNumbers && !isIdFormat) {
                showWarningDialog("Invalid Search.\nPlease enter a full ID Number, Student Name, or Program.");
                search.clear();
                return;
            }

            if (query.equals("male") || query.equals("female") || query.equals("other") || 
                query.equals("m") || query.equals("f")) {
                showWarningDialog("Invalid Search.\nYou can only search by Student Name, ID Number, or Program.");
                search.clear();
                return;
            }

            ObservableList<Student> items = table.getItems();
            List<Student> programMatches = new ArrayList<>();
            
            //program search
            for (Student s : items) {
                if (s.getProgramCode().toLowerCase().equals(query)) {
                    programMatches.add(s);
                }
            }

            if (!programMatches.isEmpty()) {
                items.removeAll(programMatches);
                items.addAll(0, programMatches);
                
                table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                table.getSelectionModel().clearSelection();
                
                for (Student s : programMatches) {
                    table.getSelectionModel().select(s);
                }
                
                table.scrollTo(0);
                return; 
            }

            //normal student search
            table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            boolean foundMatch = false;
            
            for (Student s : items) {
                boolean match = false;
                
                if (isIdFormat) {
                    match = s.getId().toLowerCase().equals(query);
                } else {
                    match = s.getFirstName().toLowerCase().contains(query) || 
                            s.getLastName().toLowerCase().contains(query);
                }

                if (match) {
                    table.getSelectionModel().clearSelection();
                    table.getSelectionModel().select(s);
                    table.scrollTo(s);
                    showStudentInfoDialog(s);
                    foundMatch = true;
                    break;
                }
            }

            if (!foundMatch) {
                showWarningDialog("No student found matching: " + search.getText());
                search.clear();
            }
        });

        //action buttons
        add.setOnAction(e -> {
            List<String[]> programs = CsvHandler.readCSV("src/data/program_data.csv");
            if (programs.isEmpty() || programs.size() <= 1) {
                showWarningDialog("Action Blocked.\nYou must create at least one Program in the Programs tab before you can add a student.");
            } else {
                showAddOrUpdateDialog(null);
            }
        });
        
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

        /*clear highlight outside table */
        layout.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
            javafx.scene.Node target = (javafx.scene.Node) event.getTarget();
            boolean clickedOnRow = false;
            boolean clickedOnButton = false;
            
            while (target != null) {
                if (target instanceof TableRow) {
                    clickedOnRow = true;
                    break;
                }
                if (target instanceof Button) {
                    clickedOnButton = true;
                    break;
                }
                target = target.getParent();
            }
            
            if (!clickedOnRow && !clickedOnButton) {
                table.getSelectionModel().clearSelection();
            }
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

        //id number
        TextField idField = createLabeledField(maroonBox, "ID Number", isUpdate ? studentToUpdate.getId() : "");
        
        //first name
        TextField firstField = createLabeledField(maroonBox, "First Name", isUpdate ? studentToUpdate.getFirstName() : "");
        
        //last name
        TextField lastField = createLabeledField(maroonBox, "Last Name", isUpdate ? studentToUpdate.getLastName() : "");

        //program dropdown
        VBox progContainer = new VBox(5);
        progContainer.setAlignment(Pos.CENTER_LEFT);
        Label progLabel = new Label("Program");
        progLabel.getStyleClass().add("modal-label");
        
        ComboBox<String> progBox = new ComboBox<>();
        progBox.setVisibleRowCount(8);
        List<String[]> programsList = CsvHandler.readCSV("src/data/program_data.csv");
        for (String[] row : programsList) {
            if (row.length > 0 && !row[0].equalsIgnoreCase("code")) {
                progBox.getItems().add(row[0].toUpperCase());
            }
        }
        
        if (isUpdate) {
            String currentProg = studentToUpdate.getProgramCode().toUpperCase();
            if (!progBox.getItems().contains(currentProg)) {
                progBox.getItems().add(currentProg);
            }
            progBox.setValue(currentProg);
        } else if (!progBox.getItems().isEmpty()) {
            progBox.setValue(progBox.getItems().get(0));
        } else {
            progBox.setValue("NO PROGRAMS FOUND");
        }
        
        progBox.setStyle("-fx-background-color: #F1ECE4; -fx-background-radius: 15; -fx-pref-width: 300px; -fx-font-size: 14px; -fx-padding: 2; -fx-cursor: hand;");
        progBox.setVisibleRowCount(5);
        progContainer.getChildren().addAll(progLabel, progBox);
        maroonBox.getChildren().add(progContainer); 

        //year
        TextField yearField = createLabeledField(maroonBox, "Year", isUpdate ? String.valueOf(studentToUpdate.getYear()) : "");

        //gender
        VBox genderContainer = new VBox(5);
        genderContainer.setAlignment(Pos.CENTER_LEFT);
        Label genderLabel = new Label("Gender");
        genderLabel.getStyleClass().add("modal-label");
        
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Female", "Male", "Other");
        
        if (isUpdate) {
            String current = capitalizeWords(studentToUpdate.getGender());
            genderBox.setValue(genderBox.getItems().contains(current) ? current : "Other");
        } else {
            genderBox.setValue("Female"); 
        }
        
        genderBox.setStyle("-fx-background-color: #F1ECE4; -fx-background-radius: 15; -fx-pref-width: 300px; -fx-font-size: 14px; -fx-padding: 2; -fx-cursor: hand;");
        genderContainer.getChildren().addAll(genderLabel, genderBox);
        maroonBox.getChildren().add(genderContainer);

        //button actions
        Button actionBtn = new Button(isUpdate ? "Update" : "Add");
        actionBtn.getStyleClass().add("modal-button");
        VBox.setMargin(actionBtn, new Insets(20, 0, 0, 0));

        actionBtn.setOnAction(e -> {
            String id = idField.getText().trim();
            String first = firstField.getText().trim();
            String last = lastField.getText().trim();
            String prog = progBox.getValue();
            String yearText = yearField.getText().trim();
            String gender = genderBox.getValue();

            //id constraints
            if (!id.matches("^(201[0-9]|202[0-6])-\\d{4}$")) {
                showWarningDialog("Invalid ID Format.\nPlease use YYYY-NNNN (Year must be 2010-2026).");
                return; 
            }

            if (first.isEmpty() || last.isEmpty()) {
                showWarningDialog("Name fields cannot be empty.");
                return;
            }

            int year;
            try {
                year = Integer.parseInt(yearText);
                if (year < 1 || year > 4) {
                    showWarningDialog("Invalid Year.\nYear must be between 1 and 4.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showWarningDialog("Invalid Year.\nPlease enter a numerical digit.");
                return;
            }

            if (prog.isEmpty()) {
                showWarningDialog("Program cannot be empty.");
                return;
            }

            first = capitalizeWords(first);
            last = capitalizeWords(last);

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

        createLabeledField(box, "Program", getFullProgramName(student.getProgramCode())).setEditable(false);
        createLabeledField(box, "Year", String.valueOf(student.getYear())).setEditable(false);
        createLabeledField(box, "College", getFullCollegeName(student.getProgramCode())).setEditable(false);
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
        
        Button yesBtn = new Button("Delete " + student.getId());
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

    private void showWarningDialog(String message) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(StageStyle.TRANSPARENT);

        VBox box = new VBox(20);
        box.getStyleClass().add("modal-content");
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(350); // Make it wide enough for the text

        Label title = new Label("Invalid Search");
        title.getStyleClass().add("modal-title");
        title.setStyle("-fx-font-size: 24px;");

        Label prompt = new Label(message);
        prompt.getStyleClass().add("modal-label");
        prompt.setStyle("-fx-font-size: 16px; -fx-text-alignment: center; -fx-wrap-text: true;");

        Button okBtn = new Button("Understood");
        okBtn.getStyleClass().add("modal-button");
        okBtn.setOnAction(e -> modal.close());

        box.getChildren().addAll(title, prompt, okBtn);

        VBox root = new VBox(box);
        root.getStyleClass().add("modal-overlay");
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        
        String[] words = str.split("\\s+");
        StringBuilder capitalized = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
            }
        }
        return capitalized.toString().trim();
    }

    //for student info
    private String getFullProgramName(String code) {
        List<String[]> programs = CsvHandler.readCSV("src/data/program_data.csv");
        for (String[] row : programs) {
            if (row.length >= 2 && row[0].equalsIgnoreCase(code)) {
                return row[1];
            }
        }
        return code;
    }

    //for college student info
    private String getFullCollegeName(String progCode) {
        String targetCollegeCode = "";
        
        List<String[]> programs = CsvHandler.readCSV("src/data/program_data.csv");
        for (String[] row : programs) {
            if (row.length >= 3 && row[0].equalsIgnoreCase(progCode)) {
                targetCollegeCode = row[2];
                break;
            }
        }

        if (targetCollegeCode.isEmpty()) return "Unknown College";

        List<String[]> colleges = CsvHandler.readCSV("src/data/college_data.csv");
        for (String[] row : colleges) {
            if (row.length >= 2 && row[0].equalsIgnoreCase(targetCollegeCode)) {
                return row[1];
            }
        }
        
        return targetCollegeCode; //incase name not found
    }
}