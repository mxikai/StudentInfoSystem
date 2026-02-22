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

        header.getChildren().addAll(title, spacer, search);

        /*action buttons */
        HBox toolbar = new HBox(15);
        Button add = new Button("+ Add a program");
        Button update = new Button("✎ Update program");
        Button delete = new Button("🗑 Delete program");

        add.getStyleClass().add("action-button");
        update.getStyleClass().add("action-button");
        delete.getStyleClass().add("action-button");

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
        table.getColumns().forEach(col -> col.setSortable(false));

        loadData();

        /*action logic */        
        //search logic
        search.setOnAction(e -> {
            String query = search.getText().trim().toLowerCase();
            if (query.isEmpty()) {
                table.getSelectionModel().clearSelection();
                return;
            }
            boolean foundMatch = false;
            for (Program p : table.getItems()) {
                if (p.getCode().toLowerCase().contains(query) || p.getName().toLowerCase().contains(query)) {
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
            if (selected == null || selected.equals("Sort filter ˅")) return;

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
            
            while (target != null) {
                if (target instanceof TableRow) {
                    clickedOnRow = true;
                    break;
                }
                target = target.getParent();
            }
            
            if (!clickedOnRow) {
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
                Program p = new Program(row[0].trim(), row[1].trim(), row[2].trim());
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

            if (code.isEmpty() || college.isEmpty()) {
                showWarningDialog("Program Code and College Code are required.");
                return;
            }

            String combinedName = degree;
            if (!major.isEmpty()) {
                combinedName = degree + " in " + capitalizeWords(major);
            }

            if (isUpdate) {
                programToUpdate.setCode(code);
                programToUpdate.setName(combinedName);
                programToUpdate.setCollege(college);
                table.refresh();
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
                } else {
                    capitalized.append(Character.toUpperCase(word.charAt(0)))
                               .append(word.substring(1).toLowerCase()).append(" ");
                }
            }
        }
        return capitalized.toString().trim();
    }
}