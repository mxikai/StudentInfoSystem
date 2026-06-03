package gui;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.*;
import model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Program_Panel {
    private String filePath = "src/data/program_data.csv";
    private TableView<Program> table;

    public Parent getView() {
        VBox layout = new VBox(25);
        layout.setPadding(new Insets(30));

        /*header */
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("top-header-bar");

        Label title = new Label("Programs");
        title.getStyleClass().add("top-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search programs...");
        search.getStyleClass().add("search-bar");
        search.setPrefWidth(350);

        header.getChildren().addAll(title, spacer, search);

        /*action buttons */
        HBox toolbar = new HBox(15);
        Button add = new Button("+ Add a program");
        Button update = new Button("✎ Update program");
        Button delete = new Button("🗑 Delete program");

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

        /*sort filter */
        HBox sortBox = new HBox();
        sortBox.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll(
            "Sort filter", 
            "Program Code (Asc)", "Program Code (Desc)", 
            "Program Name (Asc)", "Program Name (Desc)", 
            "College (Asc)", "College (Desc)"
        );
        sortFilter.setValue("Sort filter");
        sortFilter.getStyleClass().add("sort-combo-box");
        sortFilter.setVisibleRowCount(8);
        sortBox.getChildren().add(sortFilter);

        /*table */
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Program, String> codeCol = new TableColumn<>("Program Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Program, String> nameCol = new TableColumn<>("Program Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name")); 

        TableColumn<Program, String> collegeCol = new TableColumn<>("College");
        collegeCol.setCellValueFactory(new PropertyValueFactory<>("college")); 

        table.getColumns().addAll(codeCol, nameCol, collegeCol);
        table.getColumns().forEach(col -> {
            col.setSortable(false);
            col.setReorderable(false); 
        });

        loadData();

        /*action logic */        
        //search logic
        search.setOnAction(e -> {
            String query = search.getText().trim().toLowerCase();


            if (query.isEmpty()) {
                table.getSelectionModel().clearSelection();
                table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                loadData();
                return;
            }

            ObservableList<Program> items = table.getItems();
            List<Program> collegeMatches = new ArrayList<>();
            
            //college search
            for (Program p : items) {
                if (p.getCollege().toLowerCase().equals(query)) {
                    collegeMatches.add(p);
                }
            }

            if (!collegeMatches.isEmpty()) {
                items.removeAll(collegeMatches);
                items.addAll(0, collegeMatches);
                
                table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                table.getSelectionModel().clearSelection();
                
                for (Program p : collegeMatches) {
                    table.getSelectionModel().select(p);
                }
                
                table.scrollTo(0);
                return; 
            }

            //program search
            table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            boolean foundMatch = false;
            
            for (Program p : items) {
                boolean match = p.getCode().toLowerCase().contains(query) || 
                                p.getName().toLowerCase().contains(query);

                if (match) {
                    table.getSelectionModel().clearSelection();
                    table.getSelectionModel().select(p);
                    table.scrollTo(p);
                    foundMatch = true;
                    break;
                }
            }

            if (!foundMatch) {
                showWarningDialog("No program found matching: " + search.getText());
                search.clear();
            }
        });

        //sort logic
        sortFilter.setOnAction(e -> {
            String selected = sortFilter.getValue();
            if (selected == null || selected.equals("Sort filter")) return;

            ObservableList<Program> items = table.getItems();
            if (selected.equals("Program Code (Asc)")) items.sort(Comparator.comparing(Program::getCode));
            else if (selected.equals("Program Code (Desc)")) items.sort(Comparator.comparing(Program::getCode).reversed());
            else if (selected.equals("Program Name (Asc)")) items.sort(Comparator.comparing(Program::getName));
            else if (selected.equals("Program Name (Desc)")) items.sort(Comparator.comparing(Program::getName).reversed());
            else if (selected.equals("College (Asc)")) items.sort(Comparator.comparing(Program::getCollege));
            else if (selected.equals("College (Desc)")) items.sort(Comparator.comparing(Program::getCollege).reversed());
        });

        add.setOnAction(e -> {
            List<String[]> colleges = CsvHandler.readCSV("src/data/college_data.csv");
            if (colleges.isEmpty() || colleges.size() <=1) {
                showWarningDialog("Action Blocked.\nYou must create at least one College in the Colleges Tab before you can add a program.");
            } else {
                showAddOrUpdateDialog(null);
            }
        });
        
        update.setOnAction(e -> {
            Program selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showAddOrUpdateDialog(selected);
        });

        delete.setOnAction(e -> {
            Program selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showDeleteConfirmation(selected);
        });

        table.setRowFactory(tv -> {
            TableRow<Program> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    showProgramInfoDialog(row.getItem());
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

    private void loadData() {
        ObservableList<Program> programs = FXCollections.observableArrayList();
        List<String[]> rawData = CsvHandler.readCSV(filePath);
        
        for (String[] row : rawData) {
            if(row.length >= 3 && !row[0].equalsIgnoreCase("code")) {
                String code = row[0].trim();
                
                String college = row[row.length - 1].trim(); 
                
                StringBuilder nameBuilder = new StringBuilder(row[1].trim());
                //stitch the whole name together when separated by commas
                for (int i = 2; i < row.length - 1; i++) {
                    nameBuilder.append(", ").append(row[i].trim());
                }
                
                Program p = new Program(code, nameBuilder.toString(), college);
                programs.add(p);
            }
        }
        table.setItems(programs);
    }

    private void refreshCSV() {
        List<String> lines = new ArrayList<>();
        lines.add("code,name,college"); 
        for (Program p : table.getItems()) {
            lines.add(p.toCSV());
        }
        CsvHandler.overwriteCSV(filePath, lines);
    }

    /*overlays */
    private void showAddOrUpdateDialog(Program programToUpdate) {
        boolean isUpdate = (programToUpdate != null);
        
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.getStyleClass().add("modal-overlay");
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

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

        VBox maroonBox = new VBox(15);
        maroonBox.getStyleClass().add("modal-content");
        maroonBox.setAlignment(Pos.CENTER);

        Label title = new Label(isUpdate ? "Update Program" : "Add Program");
        title.getStyleClass().add("modal-title");
        maroonBox.getChildren().add(title);

        //program code
        TextField codeField = createLabeledField(maroonBox, "Program Code (e.g. BSCS)", isUpdate ? programToUpdate.getCode() : "");

        /*updates */
        String defaultDegree = "Bachelor of Science";
        String defaultMajor = "";

        if (isUpdate) {
            String existingName = programToUpdate.getName();
            
            if (existingName.startsWith("Bachelor of Science in ")) {
                defaultDegree = "Bachelor of Science";
                defaultMajor = existingName.substring(22).trim(); 
            } else if (existingName.startsWith("Bachelor of Science")) {
                defaultDegree = "Bachelor of Science";
                defaultMajor = existingName.substring(19).trim();
            } else if (existingName.startsWith("Bachelor of Arts in ")) {
                defaultDegree = "Bachelor of Arts";
                defaultMajor = existingName.substring(19).trim();
            } else if (existingName.startsWith("Bachelor of Arts")) {
                defaultDegree = "Bachelor of Arts";
                defaultMajor = existingName.substring(16).trim();
            } else if (existingName.startsWith("Bachelor of Elementary Education")) {
                defaultDegree = "Bachelor of Elementary Education";
                defaultMajor = existingName.substring(32).trim();
            } else if (existingName.startsWith("Bachelor of Secondary Education")) {
                defaultDegree = "Bachelor of Secondary Education";
                defaultMajor = existingName.substring(31).trim();
            } else {
                defaultMajor = existingName;
            }
        }

        //degree dropdown
        VBox degreeContainer = new VBox(5);
        degreeContainer.setAlignment(Pos.CENTER_LEFT);
        Label degreeLabel = new Label("Degree Type");
        degreeLabel.getStyleClass().add("modal-label");

        ComboBox<String> degreeBox = new ComboBox<>();
        degreeBox.getItems().addAll(
            "Bachelor of Science",
            "Bachelor of Arts",
            "Bachelor of Elementary Education",
            "Bachelor of Secondary Education"
        );
        degreeBox.setValue(defaultDegree);
        degreeBox.setStyle("-fx-background-color: #F1ECE4; -fx-background-radius: 15; -fx-pref-width: 300px; -fx-font-size: 14px; -fx-padding: 2; -fx-cursor: hand;");
        
        degreeContainer.getChildren().addAll(degreeLabel, degreeBox);
        maroonBox.getChildren().add(degreeContainer);

        //major name
        TextField majorField = createLabeledField(maroonBox, "Major (e.g. Computer Science)", defaultMajor);

        //college code
        VBox collegeContainer = new VBox(5);
        collegeContainer.setAlignment(Pos.CENTER_LEFT);
        Label collegeLabel = new Label("College Code");
        collegeLabel.getStyleClass().add("modal-label");

        ComboBox<String> collegeBox = new ComboBox<>();
        collegeBox.setVisibleRowCount(5);

        List <String[]> collegesList = CsvHandler.readCSV("src/data/college_data.csv");
        for (String[] row : collegesList) {
            if (row.length > 0 && !row[0].equalsIgnoreCase("code") && !row[0].equalsIgnoreCase("collegeCode")) {
                collegeBox.getItems().add(row[0].toUpperCase());
            }
        }

        if (isUpdate) {
            String currentCollege = programToUpdate.getCollege().toUpperCase();
            if (!collegeBox.getItems().contains(currentCollege)) {
                collegeBox.getItems().add(currentCollege);
            }
            collegeBox.setValue(currentCollege);
        } else if (!collegeBox.getItems().isEmpty()) {
            collegeBox.setValue(collegeBox.getItems().get(0));
        } else {
            collegeBox.setValue("NO COLLEGES FOUND");
        }
        
        collegeBox.setStyle("-fx-background-color: #F1ECE4; -fx-background-radius: 15; -fx-pref-width: 300px; -fx-font-size: 14px; -fx-padding: 2; -fx-cursor: hand;");
        collegeContainer.getChildren().addAll(collegeLabel, collegeBox);
        maroonBox.getChildren().add(collegeContainer);

        //action button
        Button actionBtn = new Button(isUpdate ? "Update" : "Add");
        actionBtn.getStyleClass().add("modal-button");
        VBox.setMargin(actionBtn, new Insets(20, 0, 0, 0));

        actionBtn.setOnAction(e -> {
            String code = codeField.getText().trim().toUpperCase();
            String degree = degreeBox.getValue();
            String major = majorField.getText().trim();
            String college = collegeBox.getValue();

            if (major.matches(".*\\d.*") || code.matches(".*\\d.*")) {
                showWarningDialog("Invalid Name.\nExtended Name and Code cannot contain numbers.");
                return;
            }

            //combine extended name
            String combinedName = degree;
            if (!major.isEmpty()) {
                combinedName = degree + " in " + capitalizeWords(major);
            }

            //no duplicate
            for (Program p : table.getItems()) {
                if (isUpdate && p == programToUpdate) continue;

                if (p.getCode().equalsIgnoreCase(code)) {
                    showWarningDialog("Duplicate Entry.\nA Program with the code '" + code + "' already exists.");
                    return;
                }
                if (p.getName().equalsIgnoreCase(combinedName)) {
                    showWarningDialog("Duplicate Entry.\nThe Program '" + combinedName + "' already exists.");
                    return;
                }
            }

            //update
            // Update
            if (isUpdate) {
                String oldCode = programToUpdate.getCode();
                
                programToUpdate.setCode(code);
                programToUpdate.setName(combinedName);
                programToUpdate.setCollege(college);
                table.refresh();
                
                if (!oldCode.equalsIgnoreCase(code)) {
                    List<String[]> students = CsvHandler.readCSV("src/data/student_data.csv");
                    List<String> updatedStudentLines = new ArrayList<>();
                    updatedStudentLines.add("id,firstName,lastName,programCode,year,gender");
                    boolean changed = false;
                    
                    for (String[] row : students) {
                        if (row.length >= 6 && !row[0].equalsIgnoreCase("id")) {
                            if (row[3].equalsIgnoreCase(oldCode)) {
                                row[3] = code;
                                changed = true;
                            }
                            updatedStudentLines.add(String.join(",", row));
                        }
                    }
                    // Save the updated students to the CSV
                    if (changed) {
                        CsvHandler.overwriteCSV("src/data/student_data.csv", updatedStudentLines);
                    }
                }
                
            } else {
                Program newProgram = new Program(code, combinedName, college);
                table.getItems().add(newProgram);
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

    private void showProgramInfoDialog(Program program) {
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

        Label nameTitle = new Label(program.getCode());
        nameTitle.getStyleClass().add("modal-title");
        nameTitle.setStyle("-fx-font-size: 32px; -fx-text-alignment: center;");

        box.getChildren().addAll(profilePic, nameTitle);

        createLabeledField(box, "Extended Name", program.getName()).setEditable(false);
        createLabeledField(box, "College", program.getCollege()).setEditable(false);

        root.getChildren().addAll(topHeader, box);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private void showDeleteConfirmation(Program program) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(StageStyle.TRANSPARENT);

        VBox box = new VBox(20);
        box.getStyleClass().add("modal-content");
        box.setAlignment(Pos.CENTER);

        Label prompt = new Label("Delete Program: " + program.getCode() + "?");
        prompt.getStyleClass().add("modal-label");
        prompt.setStyle("-fx-font-size: 18px; -fx-text-alignment: center;");

        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);
        
        Button yesBtn = new Button("Delete");
        yesBtn.getStyleClass().add("modal-button");
        yesBtn.setOnAction(e -> {
            table.getItems().remove(program);
            refreshCSV();
            
            //set null if there are students in program 
            List<String[]> students = CsvHandler.readCSV("src/data/student_data.csv");
            List<String> updatedStudentLines = new ArrayList<>();
            updatedStudentLines.add("id,firstName,lastName,programCode,year,gender");
            boolean changed = false;
            
            for (String[] row : students) {
                if (row.length >= 6 && !row[0].equalsIgnoreCase("id")) {
                    if (row[3].equalsIgnoreCase(program.getCode())) {
                        row[3] = "N/A";
                        changed = true;
                    }
                    updatedStudentLines.add(String.join(",", row));
                }
            }
            if (changed) {
                CsvHandler.overwriteCSV("src/data/student_data.csv", updatedStudentLines);
            }
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
        box.setPrefWidth(350); 

        Label title = new Label("Notice");
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
                if (word.equalsIgnoreCase("in") || word.equalsIgnoreCase("of") || word.equalsIgnoreCase("and")) {
                    capitalized.append(word.toLowerCase()).append(" ");
                } else if (word.startsWith("(")) {
                    //allow parenthesis
                    capitalized.append("(");
                    if (word.length() > 1) {
                        capitalized.append(Character.toUpperCase(word.charAt(1)))
                            .append(word.substring(2).toLowerCase());
                    }
                    capitalized.append(" ");
                } else {
                    //normal capitalization
                    capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase()).append(" ");
                }
            }
        }
        return capitalized.toString().trim();
    }
}