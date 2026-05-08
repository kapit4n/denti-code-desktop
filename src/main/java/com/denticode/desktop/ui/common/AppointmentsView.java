package com.denticode.desktop.ui.common;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.core.EventBus;
import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.Doctor;
import com.denticode.desktop.domain.model.Patient;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Lists appointments. Filter is a function applied to the master list:
 *   - admin / global: null  (all appointments)
 *   - doctor:         a -> a.primaryDoctor.id == doctorId
 *   - patient:        a -> a.patient.id == patientId
 */
public final class AppointmentsView {

    private final AppContext app;
    private final java.util.function.Predicate<Appointment> filter;
    private final VBox root = new VBox();
    private final TableView<Appointment> table = new TableView<>();
    private final ObservableList<Appointment> rows = FXCollections.observableArrayList();
    private Consumer<Appointment> onOpen;

    public AppointmentsView(AppContext app, java.util.function.Predicate<Appointment> filter) {
        this.app = app;
        this.filter = filter;
        root.getStyleClass().add("page");

        Label title = new Label();
        title.textProperty().bind(app.i18n().binding("appointments.title"));
        title.getStyleClass().add("page-title");

        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);

        Button create = new Button();
        create.textProperty().bind(app.i18n().binding("appointments.new"));
        create.getStyleClass().add("accent");
        create.setOnAction(e -> openCreate());

        Button refresh = new Button();
        refresh.textProperty().bind(app.i18n().binding("common.refresh"));
        refresh.setOnAction(e -> reload());

        HBox toolbar = new HBox(10, grow, refresh, create);
        toolbar.setPadding(new Insets(0, 0, 6, 0));

        buildColumns();
        table.setItems(rows);
        table.setPlaceholder(new Label(app.i18n().t("common.empty")));
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Appointment> r = new javafx.scene.control.TableRow<>();
            r.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !r.isEmpty() && onOpen != null) {
                    onOpen.accept(r.getItem());
                }
            });
            return r;
        });

        root.getChildren().addAll(title, toolbar, table);
        reload();

        app.eventBus().<Object>subscribe(EventBus.APPOINTMENT_CHANGED,
                ignored -> Platform.runLater(this::reload));
    }

    public void setOnOpen(Consumer<Appointment> handler) {
        this.onOpen = handler;
    }

    public Node getRoot() {
        return root;
    }

    private void openCreate() {
        var dialog = new AppointmentCreateDialog(app, defaultPatient(), defaultDoctor());
        dialog.showAndWait().ifPresent(a -> reload());
    }

    private Patient defaultPatient() { return null; }
    private Doctor defaultDoctor() {
        var u = app.session().getUser();
        if (u != null) {
            return app.doctorService().findByUserId(u.getId()).orElse(null);
        }
        return null;
    }

    private void buildColumns() {
        TableColumn<Appointment, String> idCol = new TableColumn<>();
        idCol.textProperty().bind(app.i18n().binding("appointments.col.id"));
        idCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        idCol.setMinWidth(50);

        TableColumn<Appointment, String> whenCol = new TableColumn<>();
        whenCol.textProperty().bind(app.i18n().binding("appointments.col.when"));
        whenCol.setCellValueFactory(c -> new SimpleStringProperty(
                Formatters.dateTime(c.getValue().getScheduledAt(), app.i18n().getLocale())));
        whenCol.setMinWidth(160);

        TableColumn<Appointment, String> patCol = new TableColumn<>();
        patCol.textProperty().bind(app.i18n().binding("appointments.col.patient"));
        patCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPatient().getFullName()));
        patCol.setMinWidth(160);

        TableColumn<Appointment, String> docCol = new TableColumn<>();
        docCol.textProperty().bind(app.i18n().binding("appointments.col.doctor"));
        docCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrimaryDoctor().getFullName()));
        docCol.setMinWidth(160);

        TableColumn<Appointment, String> purposeCol = new TableColumn<>();
        purposeCol.textProperty().bind(app.i18n().binding("appointments.col.purpose"));
        purposeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPurpose() == null ? "" : c.getValue().getPurpose()));
        purposeCol.setMinWidth(160);

        TableColumn<Appointment, Appointment> statusCol = new TableColumn<>();
        statusCol.textProperty().bind(app.i18n().binding("appointments.col.status"));
        statusCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Appointment item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : StatusTag.of(item.getStatus(), app.i18n()));
                setText(null);
            }
        });
        statusCol.setMinWidth(120);

        table.getColumns().setAll(idCol, whenCol, patCol, docCol, purposeCol, statusCol);
    }

    public void reload() {
        var data = app.appointmentService().all();
        if (filter != null) {
            data = data.stream().filter(filter).toList();
        }
        rows.setAll(data);
    }
}
