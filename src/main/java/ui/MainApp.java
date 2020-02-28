package ui;

import db.ProductDAO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.controller.DataBaseWindowController;
import ui.controller.LoginWindowController;

import java.io.IOException;

public class MainApp extends Application {
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        openLoginWindow();
        stage.show();
    }

    public void openLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/ui/view/LoginWindow.fxml"));
            primaryStage.setScene(new Scene(loader.load()));
            primaryStage.setTitle("DataBase login");

            LoginWindowController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openDataBaseWindow(ProductDAO dao) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/ui/view/DataBaseWindow.fxml"));
            primaryStage.setScene(new Scene(loader.load()));
            primaryStage.setTitle("Data Base Client");

            DataBaseWindowController controller = loader.getController();
            controller.setMainApp(this);
            controller.setDao(dao);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
