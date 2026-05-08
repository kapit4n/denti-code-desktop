package com.denticode.desktop.ui.common;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.domain.model.Doctor;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Read-only directory of doctors. Equivalent to {@code /admin/doctors}.
 */
public final class DoctorsView {

    private final AppContext app;
    private final VBox root = new VBox();
    private final TableView<Doctor> table = new TableView<>();
    private final ObservableList<Doctor> rows = FXCollections.observableArrayList();

    public DoctorsView(AppContext app) {
        this.app = app;
        root.getStyleClass().add("page");

        Label title = new Label();
        title.textProperty().bind(app.i18n().binding("doctors.title"));
        title.getStyleClass().add("page-title");

        buildColumns();
        rows.setAll(app.doctorService().all());
        table.setItems(rows);
        table.setPlaceholder(new Label(app.i18n().t("common.empty")));
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(title, table);
    }

    public Node getRoot() { return root; }

    private void buildColumns() {
        TableColumn<Doctor, String> name = new TableColumn<>();
        name.textProperty().bind(app.i18n().binding("doctors.col.name"));
        name.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        name.setMinWidth(180);

        TableColumn<Doctor, String> email = new TableColumn<>();
        email.textProperty().bind(app.i18n().binding("doctors.col.email"));
        email.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        email.setMinWidth(180);

        TableColumn<Doctor, String> phone = new TableColumn<>();
        phone.textProperty().bind(app.i18n().binding("doctors.col.phone"));
        phone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getContactPhone()));
        phone.setMinWidth(140);

        TableColumn<Doctor, String> license = new TableColumn<>();
        license.textProperty().bind(app.i18n().binding("doctors.col.license"));
        license.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLicenseNumber()));
        license.setMinWidth(120);

        TableColumn<Doctor, String> room = new TableColumn<>();
        room.textProperty().bind(app.i18n().binding("doctors.col.room"));
        room.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOfficeRoom()));
        room.setMinWidth(80);

        TableColumn<Doctor, String> active = new TableColumn<>();
        active.textProperty().bind(app.i18n().binding("doctors.col.active"));
        active.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "\u2713" : "\u2715"));
        active.setMinWidth(70);

        table.getColumns().setAll(name, email, phone, license, room, active);
    }
}
