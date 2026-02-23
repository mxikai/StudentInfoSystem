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
import model.College;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class College_Panel {
    private String filePath = "src/data/college_data.csv";
    private TableView<College> table;

    public Parent getView() {
        VBox layout = new VBox(25);
        layout.setPadding(new Insets(30));

        /*header */
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("top-header-bar");

        Label title = new Label("Colleges");
        title.getStyleClass().add("top-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search colleges...");
        search.getStyleClass().add("search-bar");
        search.setPrefWidth(350);

        header.getChildren().addAll(title, spacer, search);

        /*action buttons */
        HBox toolbar = new HBox(15);
        Button add = new Button("+ Add a college");
        Button update = new Button("✎ Update college");
        Button delete = new Button("🗑 Delete college");

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
            "College Code (Asc)", "College Code (Desc)", 
            "College Name (Asc)", "College Name (Desc)"
        );
        sortFilter.setValue("Sort filter");
        sortFilter.getStyleClass().add("sort-combo-box");
        sortBox.getChildren().add(sortFilter);

        /*table */
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<College, String> codeCol = new TableColumn<>("College Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("collegeCode"));

        TableColumn<College, String> nameCol = new TableColumn<>("College Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("collegeName")); 

        table.getColumns().addAll(codeCol, nameCol);
        table.getColumns().forEach(col -> {
            col.setSortable(false);
            col.setReorderable(false); 
        });

        loadData();

        /*action listenres */
        search.setOnAction(e -> {
            String query = search.getText().trim().toLowerCase();
            if (query.isEmpty()) {
                table.getSelectionModel().clearSelection();
                return;
            }

            boolean foundMatch = false;
            for (College c : table.getItems()) {
                if (c.getCollegeCode().toLowerCase().contains(query) || 
                    c.getCollegeName().toLowerCase().contains(query)) {
                    
                    table.getSelectionModel().select(c);
                    table.scrollTo(c);
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                showWarningDialog("No college found matching: " + search.getText());
                search.clear();
            }
        });

        sortFilter.setOnAction(e -> {
            String selected = sortFilter.getValue();
            if (selected == null || selected.equals("Sort filter")) return;

            ObservableList<College> items = table.getItems();
            if (selected.equals("College Code (Asc)")) {
                items.sort(Comparator.comparing(College::getCollegeCode));
            } else if (selected.equals("College Code (Desc)")) {
                items.sort(Comparator.comparing(College::getCollegeCode).reversed());
            } else if (selected.equals("College Name (Asc)")) {
                items.sort(Comparator.comparing(College::getCollegeName));
            } else if (selected.equals("College Name (Desc)")) {
                items.sort(Comparator.comparing(College::getCollegeName).reversed());
            }
        });

        add.setOnAction(e -> showAddOrUpdateDialog(null));
        
        update.setOnAction(e -> {
            College selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showAddOrUpdateDialog(selected);
        });

        delete.setOnAction(e -> {
            College selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showDeleteConfirmation(selected);
        });

        table.setRowFactory(tv -> {
            TableRow<College> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    showCollegeInfoDialog(row.getItem());
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
        ObservableList<College> colleges = FXCollections.observableArrayList();
        List<String[]> rawData = CsvHandler.readCSV(filePath);
        for (String[] row : rawData) {
            if(row.length >= 2 && !row[0].equalsIgnoreCase("code") && !row[0].equalsIgnoreCase("collegeCode")) {
                College c = new College(row[0].trim(), row[1].trim());
                colleges.add(c);
            }
        }
        table.setItems(colleges);
    }

    private void refreshCSV() {
        List<String> lines = new ArrayList<>();
        lines.add("collegeCode,collegeName");
        for (College c : table.getItems()) {
            lines.add(c.toCSV());
        }
        CsvHandler.overwriteCSV(filePath, lines);
    }

    /*overlay */
    private void showAddOrUpdateDialog(College collegeToUpdate) {
        boolean isUpdate = (collegeToUpdate != null);
        
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

        Label title = new Label(isUpdate ? "Update College" : "Add College");
        title.getStyleClass().add("modal-title");
        maroonBox.getChildren().add(title);

        TextField codeField = createLabeledField(maroonBox, "College Code (e.g. CCS)", isUpdate ? collegeToUpdate.getCollegeCode() : "");
        TextField nameField = createLabeledField(maroonBox, "College Name (e.g. College of Computer Studies)", isUpdate ? collegeToUpdate.getCollegeName() : "");

        Button actionBtn = new Button(isUpdate ? "Update" : "Add");
        actionBtn.getStyleClass().add("modal-button");
        VBox.setMargin(actionBtn, new Insets(20, 0, 0, 0));

        actionBtn.setOnAction(e -> {
            String code = codeField.getText().trim().toUpperCase();
            String name = capitalizeWords(nameField.getText().trim());

            if (code.isEmpty() || name.isEmpty()) {
                showWarningDialog("Both College Code and College Name are required.");
                return;
            }

            if (isUpdate) {
                collegeToUpdate.setCollegeCode(code);
                collegeToUpdate.setCollegeName(name);
                table.refresh();
            } else {
                College newCollege = new College(code, name);
                table.getItems().add(newCollege);
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

    private void showCollegeInfoDialog(College college) {
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

        Label nameTitle = new Label(college.getCollegeCode());
        nameTitle.getStyleClass().add("modal-title");
        nameTitle.setStyle("-fx-font-size: 32px; -fx-text-alignment: center;");

        box.getChildren().addAll(profilePic, nameTitle);

        createLabeledField(box, "College Name", college.getCollegeName()).setEditable(false);

        root.getChildren().addAll(topHeader, box);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private void showDeleteConfirmation(College college) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(StageStyle.TRANSPARENT);

        VBox box = new VBox(20);
        box.getStyleClass().add("modal-content");
        box.setAlignment(Pos.CENTER);

        Label prompt = new Label("Are you sure you want to delete\nCollege: " + college.getCollegeCode() + "?");
        prompt.getStyleClass().add("modal-label");
        prompt.setStyle("-fx-font-size: 18px; -fx-text-alignment: center;");

        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);
        
        Button yesBtn = new Button("Delete");
        yesBtn.getStyleClass().add("modal-button");
        yesBtn.setOnAction(e -> {
            table.getItems().remove(college);
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