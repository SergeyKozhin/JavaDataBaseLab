package ui.controller;

import db.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewProductWindowController {
    private Stage dialogStage;
    private Product product;
    private boolean isOKClicked = false;

    @FXML
    private TextField priceField;

    @FXML
    private TextField titleField;

    @FXML
    private void handleOKClicked() {
        if (isInputValid()) {
            product.setTitle(titleField.getText());
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
        StringBuilder errorMessage = new StringBuilder();
        if (titleField.getText() == null || titleField.getText().isEmpty()) {
            errorMessage.append("No title provided\n");
        }
        if (priceField.getText() == null || priceField.getText().isEmpty()) {
            errorMessage.append("No price provided\n");
        } else {
            try {
                int price = Integer.parseInt(priceField.getText());
                if (price < 0) {
                    errorMessage.append("Price can't be negative\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("No valid price. must be integer!\n");
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage.toString());

            alert.showAndWait();

            return false;
        }
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            titleField.setText(product.getTitle());
            priceField.setText(Integer.toString(product.getCost()));
        } else {
            titleField.setText("");
            priceField.setText("");
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOKClicked() {
        return isOKClicked;
    }
}
