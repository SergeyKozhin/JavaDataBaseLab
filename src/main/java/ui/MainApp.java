package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.controller.MainWindowController;

import java.io.IOException;

public class MainApp extends Application {
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/ui/view/MainWindow.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Data Base Client");

            MainWindowController controller = loader.getController();
            controller.setMainApp(this);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
