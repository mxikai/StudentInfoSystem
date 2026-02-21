import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import gui.Student_Panel;

public class App extends Application {

    private BorderPane mainLayout;

    @Override
    public void start(Stage primaryStage) {
        mainLayout = new BorderPane();

        /*sidebar*/
        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(280);

        Label title = new Label("Simple\nStudent\nInformation\nSystem");
        title.getStyleClass().add("header-title-sidebar");

        Button students = new Button("🎓 Students");
        students.setMaxWidth(Double.MAX_VALUE);
        students.getStyleClass().add("nav-button-active");

        Button programs = createNavButton("📄 Programs");
        Button colleges = createNavButton("🏢 Colleges");

        students.setOnAction(e -> mainLayout.setCenter(new Student_Panel().getView()));
        programs.setOnAction(e -> System.out.println("Programs clicked.")); //placeholder
        colleges.setOnAction(e -> System.out.println("Colleges clicked.")); //placeholder

        sidebar.getChildren().addAll(title, students, programs, colleges);
        mainLayout.setLeft(sidebar);

        mainLayout.setCenter(new Student_Panel().getView());

        Scene scene = new Scene (mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Student Information System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createNavButton (String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}