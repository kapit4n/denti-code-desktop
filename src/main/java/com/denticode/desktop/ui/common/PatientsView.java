package com.denticode.desktop.ui.common;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.core.EventBus;
import com.denticode.desktop.domain.model.Patient;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Master list of patients with quick search, registration and edit.
 * Used by Admin and Doctor portals (read-only details for patient self-service).
 */
public final class PatientsView {

    private final AppContext app;
    private final VBox root = new VBox();
    private final TextField search = new TextField();
    private final TableView<Patient> table = new TableView<>();
    private final ObservableList<Patient> rows = FXCollections.observableArrayList();

    public PatientsView(AppContext app) {
        this.app = app;
        root.getStyleClass().add("page");

        Label title = new Label();
        title.textProperty().bind(app.i18n().binding("patients.title"));
        title.getStyleClass().add("page-title");

        search.promptTextProperty().bind(app.i18n().binding("patients.search"));
        search.textProperty().addListener((obs, was, now) -> reload(now));

        Button refresh = new Button();
        refresh.textProperty().bind(app.i18n().binding("common.refresh"));
        refresh.setOnAction(e -> reload(search.getText()));

        Button create = new Button();
        create.textProperty().bind(app.i18n().binding("patients.new"));
        create.getStyleClass().add("accent");
        create.setOnAction(e -> {
            new PatientFormDialog(app, null).showAndWait().ifPresent(p -> reload(search.getText()));
        });

        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        HBox toolbar = new HBox(10, search, grow, refresh, create);
        toolbar.setPadding(new Insets(0, 0, 6, 0));

        buildColumns();
        table.setItems(rows);
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Patient> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    new PatientFormDialog(app, row.getItem()).showAndWait().ifPresent(p -> reload(search.getText()));
                }
            });
            return row;
        });
        table.setPlaceholder(new Label(app.i18n().t("common.empty")));
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(title, toolbar, table);
        reload(null);

        app.eventBus().<Object>subscribe(EventBus.PATIENT_CHANGED,
                ignored -> Platform.runLater(() -> reload(search.getText())));
    }

    public Node getRoot() {
        return root;
    }

    private void buildColumns() {
        TableColumn<Patient, String> idCol = new TableColumn<>();
        idCol.textProperty().bind(app.i18n().binding("patients.col.id"));
        idCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        idCol.setMinWidth(50);

        TableColumn<Patient, String> nameCol = new TableColumn<>();
        nameCol.textProperty().bind(app.i18n().binding("patients.col.name"));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        nameCol.setMinWidth(180);

        TableColumn<Patient, String> dobCol = new TableColumn<>();
        dobCol.textProperty().bind(app.i18n().binding("patients.col.dob"));
        dobCol.setCellValueFactory(c -> new SimpleStringProperty(Formatters.date(c.getValue().getDateOfBirth())));
        dobCol.setMinWidth(120);

        TableColumn<Patient, String> phoneCol = new TableColumn<>();
        phoneCol.textProperty().bind(app.i18n().binding("patients.col.phone"));
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getContactPhone()));
        phoneCol.setMinWidth(120);

        TableColumn<Patient, String> emailCol = new TableColumn<>();
        emailCol.textProperty().bind(app.i18n().binding("patients.col.email"));
        emailCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEmail() == null ? app.i18n().t("patients.noEmail") : c.getValue().getEmail()));
        emailCol.setMinWidth(180);

        TableColumn<Patient, Patient> actionsCol = new TableColumn<>();
        actionsCol.textProperty().bind(app.i18n().binding("common.actions"));
        actionsCol.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(c.getValue()));
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button edit = new Button();
            private final HBox box = new HBox(6, edit);
            {
                edit.textProperty().bind(app.i18n().binding("common.edit"));
                edit.setOnAction(e -> {
                    Patient p = getItem();
                    if (p != null) new PatientFormDialog(app, p).showAndWait().ifPresent(x -> reload(search.getText()));
                });
            }
            @Override
            protected void updateItem(Patient item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : box);
            }
        });
        actionsCol.setMinWidth(110);

        table.getColumns().setAll(idCol, nameCol, dobCol, phoneCol, emailCol, actionsCol);
    }

    private void reload(String query) {
        rows.setAll(app.patientService().search(query));
    }
}
