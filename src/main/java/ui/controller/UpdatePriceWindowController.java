package ui.controller;

import db.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UpdatePriceWindowController {
    private Stage dialogStage;
    private Product product;
    private boolean isOKClicked = false;

    @FXML
    private TextField priceField;

    @FXML
    private Label titleLabel;

    @FXML
    private void handleOKClicked() {
        if (isInputValid()) {
            product.setCost(Integer.parseInt(priceField.getText()));
            isOKClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancelClicked() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";
        if (priceField.getText() == null || priceField.getText().isEmpty()) {
            errorMessage = "No price provided\n";
        } else {
            try {
                int price = Integer.parseInt(priceField.getText());
                if (price < 0) {
                    errorMessage = "Price can't be negative\n";
                }
            } catch (NumberFormatException e) {
                errorMessage = "No valid price. must be integer!\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

    public void setProduct(Product product) {
        this.product = product;
        titleLabel.setText(product.getTitle());
        priceField.setText(Integer.toString(product.getCost()));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOKClicked() {
        return isOKClicked;
    }
}
