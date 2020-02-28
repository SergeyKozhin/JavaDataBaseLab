package ui;

import db.ProductDAO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.controller.DataBaseWindowController;

import java.io.IOException;

public class MainApp extends Application {
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        openDataBaseWindow(new ProductDAO("test", "password"));
    }

    private void openDataBaseWindow(ProductDAO dao) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/ui/view/DataBaseWindow.fxml"));
            primaryStage.setScene(new Scene(loader.load()));
            primaryStage.setTitle("Data Base Client");

            DataBaseWindowController controller = loader.getController();
            controller.setMainApp(this);
            controller.setDao(dao);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
