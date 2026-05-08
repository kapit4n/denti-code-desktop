package com.denticode.desktop.ui.common;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.core.EventBus;
import com.denticode.desktop.domain.model.InventoryMovement;
import com.denticode.desktop.domain.model.MaterialInventoryLine;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * Read-only stub of the inventory dashboards (lines + recent movements).
 * Editing is wired through the service; UI for create/adjust to be added next.
 */
public final class InventoryView {

    private final AppContext app;
    private final VBox root = new VBox();
    private final TableView<MaterialInventoryLine> linesTable = new TableView<>();
    private final TableView<InventoryMovement> movementsTable = new TableView<>();
    private final ObservableList<MaterialInventoryLine> lines = FXCollections.observableArrayList();
    private final ObservableList<InventoryMovement> movements = FXCollections.observableArrayList();

    public InventoryView(AppContext app) {
        this.app = app;
        root.getStyleClass().add("page");

        Label title = new Label();
        title.textProperty().bind(app.i18n().binding("inventory.title"));
        title.getStyleClass().add("page-title");

        Label linesTitle = new Label();
        linesTitle.textProperty().bind(app.i18n().binding("inventory.lines"));
        linesTitle.getStyleClass().add("section-title");

        Label movementsTitle = new Label();
        movementsTitle.textProperty().bind(app.i18n().binding("inventory.movements"));
        movementsTitle.getStyleClass().add("section-title");

        buildLinesColumns();
        buildMovementsColumns();
        linesTable.setItems(lines);
        linesTable.setPlaceholder(new Label(app.i18n().t("common.empty")));
        movementsTable.setItems(movements);
        movementsTable.setPlaceholder(new Label(app.i18n().t("common.empty")));

        VBox.setVgrow(linesTable, Priority.ALWAYS);
        VBox.setVgrow(movementsTable, Priority.ALWAYS);

        root.getChildren().addAll(title, linesTitle, linesTable, movementsTitle, movementsTable);
        reload();
        app.eventBus().<Object>subscribe(EventBus.INVENTORY_CHANGED,
                ignored -> Platform.runLater(this::reload));
    }

    public Node getRoot() { return root; }

    private void buildLinesColumns() {
        TableColumn<MaterialInventoryLine, String> consultory = new TableColumn<>();
        consultory.textProperty().bind(app.i18n().binding("inventory.col.consultory"));
        consultory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getConsultory().getName()));
        consultory.setMinWidth(150);

        TableColumn<MaterialInventoryLine, String> material = new TableColumn<>();
        material.textProperty().bind(app.i18n().binding("inventory.col.material"));
        material.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFacility().getDisplayName()));
        material.setMinWidth(180);

        TableColumn<MaterialInventoryLine, String> category = new TableColumn<>();
        category.textProperty().bind(app.i18n().binding("inventory.col.category"));
        category.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFacility().getCategoryKey()));
        category.setMinWidth(120);

        TableColumn<MaterialInventoryLine, String> qty = new TableColumn<>();
        qty.textProperty().bind(app.i18n().binding("inventory.col.qty"));
        qty.setCellValueFactory(c -> new SimpleStringProperty(Integer.toString(c.getValue().getQuantity())));
        qty.setMinWidth(80);

        linesTable.getColumns().setAll(consultory, material, category, qty);
    }

    private void buildMovementsColumns() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        TableColumn<InventoryMovement, String> when = new TableColumn<>();
        when.textProperty().bind(app.i18n().binding("inventory.col.when"));
        when.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() == null ? "" :
                        fmt.format(c.getValue().getCreatedAt().atZone(java.time.ZoneId.systemDefault()))));
        when.setMinWidth(140);

        TableColumn<InventoryMovement, String> consultory = new TableColumn<>();
        consultory.textProperty().bind(app.i18n().binding("inventory.col.consultory"));
        consultory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getConsultory().getName()));
        consultory.setMinWidth(140);

        TableColumn<InventoryMovement, String> material = new TableColumn<>();
        material.textProperty().bind(app.i18n().binding("inventory.col.material"));
        material.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFacility().getDisplayName()));
        material.setMinWidth(160);

        TableColumn<InventoryMovement, String> type = new TableColumn<>();
        type.textProperty().bind(app.i18n().binding("inventory.col.type"));
        type.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().name()));
        type.setMinWidth(100);

        TableColumn<InventoryMovement, String> delta = new TableColumn<>();
        delta.textProperty().bind(app.i18n().binding("inventory.col.delta"));
        delta.setCellValueFactory(c -> new SimpleStringProperty(Integer.toString(c.getValue().getQuantityChange())));
        delta.setMinWidth(80);

        TableColumn<InventoryMovement, String> note = new TableColumn<>();
        note.textProperty().bind(app.i18n().binding("inventory.col.note"));
        note.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNote() == null ? "" : c.getValue().getNote()));
        note.setMinWidth(160);

        movementsTable.getColumns().setAll(when, consultory, material, type, delta, note);
    }

    private void reload() {
        lines.setAll(app.inventoryService().allLines());
        movements.setAll(app.inventoryService().recentMovements());
    }
}
