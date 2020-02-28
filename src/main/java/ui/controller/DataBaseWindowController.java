package ui.controller;

import db.Product;
import db.ProductDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.MainApp;

import java.io.IOException;

public class DataBaseWindowController {
    private MainApp mainApp;
    private ProductDAO dao;
    private ObservableList<Product> products = FXCollections.observableArrayList();

    @FXML
    private TableView<Product> tableView;

    @FXML
    private TableColumn<Product, Integer> idColumn;

    @FXML
    private TableColumn<Product, String> prodIdColumn;

    @FXML
    private TableColumn<Product, String> titleColumn;

    @FXML
    private TableColumn<Product, Integer> priceColumn;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private TextField priceFromField;

    @FXML
    private TextField priceToField;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        prodIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProdId()));
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        priceColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCost()).asObject());

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        tableView.setItems(products);
        tableView.getSelectionModel().selectedItemProperty().addListener(((observableValue, book, newValue) -> {
            if (newValue != null) {
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        }));
    }

    @FXML
    private void handleUpdatePrice() {
        Product product = tableView.getSelectionModel().getSelectedItem();
        while (true) {
            if (showUpdatePriceDialog(product)) {
                try {
                    dao.updatePrice(product.getTitle(), product.getCost());
                    products.setAll(dao.list());
                    return;
                } catch (IllegalArgumentException e) {
                    showError(e.getMessage());
                }
            } else {
                return;
            }
        }
    }

    @FXML
    private void handleAddProduct() {
        Product product = new Product();
        while (true) {
            if (showNewProductDialog(product)) {
                try {
                    dao.add(product);
                    products.setAll(dao.list());
                    return;
                } catch (IllegalArgumentException e) {
                    showError(e.getMessage());
                }
            } else {
                return;
            }
        }
    }

    @FXML
    private void handleDeleteProduct() {
        dao.delete(tableView.getSelectionModel().getSelectedItem().getTitle());
        products.setAll(dao.list());
    }

    @FXML
    private void handleFilterByPrice() {
        int priceFrom;
        if (priceFromField.getText() == null || priceFromField.getText().isEmpty()) {
            priceFrom = 0;
        } else try {
            priceFrom = Integer.parseInt(priceFromField.getText());
        } catch (NumberFormatException e) {
            showError("Wrong from price format");
            return;
        }
        if (priceFrom < 0) {
            showError("Price can't be negative");
            return;
        }

        int priceTo;
        if (priceToField.getText() == null || priceToField.getText().isEmpty()) {
            priceTo = Integer.MAX_VALUE;
        } else try {
            priceTo = Integer.parseInt(priceToField.getText());
        } catch (NumberFormatException e) {
            showError("Wrong to price format");
            return;
        }
        if (priceTo < priceFrom) {
            showError("From must be lower then to");
            return;
        }

        try {
            products.setAll(dao.listFromPriceRange(priceFrom, priceTo));
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        products.setAll(dao.list());
    }

    @FXML
    private void handleExit() {
        mainApp.openLoginWindow();
    }

    private boolean showUpdatePriceDialog(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(mainApp.getClass().getResource("/ui/view/UpdatePriceWindow.fxml"));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Update price");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainApp.getPrimaryStage());
            dialogStage.setScene(new Scene(loader.load()));

            UpdatePriceWindowController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setProduct(product);

            dialogStage.showAndWait();

            return controller.isOKClicked();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean showNewProductDialog(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(mainApp.getClass().getResource("/ui/view/NewProductWindow.fxml"));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("New product");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainApp.getPrimaryStage());
            dialogStage.setScene(new Scene(loader.load()));

            NewProductWindowController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setProduct(product);

            dialogStage.showAndWait();

            return controller.isOKClicked();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(mainApp.getPrimaryStage());
        alert.setTitle("Invalid fields");
        alert.setHeaderText("Please correct invalid fields");
        alert.setContentText(errorMessage);

        alert.showAndWait();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setDao(ProductDAO dao) {
        this.dao = dao;
        products.setAll(dao.list());
    }
}
