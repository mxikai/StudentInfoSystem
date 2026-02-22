import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import gui.*;

import java.util.List;
import model.CsvHandler;

public class App extends Application {

    private BorderPane mainLayout;

    @Override
    public void start(Stage primaryStage) {
        mainLayout = new BorderPane();

        /*sidebar */
        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(280);

        Label title = new Label("Simple\nStudent\nInformation\nSystem");
        title.getStyleClass().add("header-title-sidebar");

        Button students = createNavButton("🎓 Students");
        Button programs = createNavButton("📄 Programs");
        Button colleges = createNavButton("🏢 Colleges");

        /*navigation */
        students.setOnAction(e -> {
            mainLayout.setCenter(new Student_Panel().getView());
            switchActiveStyle(students, programs, colleges);
        });

        programs.setOnAction(e -> {
            mainLayout.setCenter(new Program_Panel().getView());
            switchActiveStyle(programs, students, colleges);
        });

        colleges.setOnAction(e -> {
            mainLayout.setCenter(new College_Panel().getView()); 
            switchActiveStyle(colleges, students, programs);
        });


        sidebar.getChildren().addAll(title, students, programs, colleges);
        mainLayout.setLeft(sidebar);

        List<String[]> collegeData = CsvHandler.readCSV("src/data/college_data.csv");
        List<String[]> programData = CsvHandler.readCSV("src/data/program_data.csv");

        if (collegeData.isEmpty() || collegeData.size() <= 1) {
            mainLayout.setCenter(new College_Panel().getView());
            switchActiveStyle(colleges, students, programs);
        } else if(programData.isEmpty() || programData.size() <= 1) {
            mainLayout.setCenter(new Program_Panel().getView());
            switchActiveStyle(programs, students, colleges);
        } else {
            mainLayout.setCenter(new Student_Panel().getView());
            switchActiveStyle(students, colleges, programs);
        }

        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Student Information System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /*helper methods */
    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private void switchActiveStyle(Button activeBtn, Button... otherBtns) {
        if (!activeBtn.getStyleClass().contains("nav-button-active")) {
            activeBtn.getStyleClass().add("nav-button-active");
        }
        for (Button btn : otherBtns) {
            btn.getStyleClass().remove("nav-button-active");
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}