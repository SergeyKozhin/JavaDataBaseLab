package ui.controller;

import db.ProductDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ui.MainApp;

public class LoginWindowController {
    private MainApp mainApp;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        try {
            ProductDAO dao = new ProductDAO(loginField.getText(), passwordField.getText());
            mainApp.openDataBaseWindow(dao);
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("Error while connecting");
            alert.setHeaderText("Please check credentials and db status");
            alert.setContentText(e.getCause().getMessage());

            alert.showAndWait();
        }
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
