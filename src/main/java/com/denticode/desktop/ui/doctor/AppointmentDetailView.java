package com.denticode.desktop.ui.doctor;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.AppointmentStatus;
import com.denticode.desktop.domain.model.Doctor;
import com.denticode.desktop.domain.model.PerformedAction;
import com.denticode.desktop.domain.model.ProcedureType;
import com.denticode.desktop.ui.common.Formatters;
import com.denticode.desktop.ui.common.Notifier;
import com.denticode.desktop.ui.common.StatusTag;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;

/**
 * Doctor view for one appointment: status update, performed actions table,
 * and a small form to add a treatment line. Equivalent to
 * {@code /doctor/appointments/[appointmentId]}.
 */
public final class AppointmentDetailView {

    private final AppContext app;
    private final Doctor performingDoctor;
    private Appointment appointment;
    private final Runnable onChanged;
    private final VBox root = new VBox();
    private final TableView<PerformedAction> actionsTable = new TableView<>();
    private final ObservableList<PerformedAction> rows = FXCollections.observableArrayList();

    public AppointmentDetailView(AppContext app, Appointment appointment,
                                 Doctor performingDoctor, Runnable onChanged) {
        this.app = app;
        this.appointment = appointment;
        this.performingDoctor = performingDoctor;
        this.onChanged = onChanged;
        root.getStyleClass().add("page");
        rebuild();
    }

    public Node getRoot() {
        return root;
    }

    private void rebuild() {
        root.getChildren().clear();

        Label title = new Label();
        title.textProperty().bind(app.i18n().binding("appointments.detail"));
        title.getStyleClass().add("page-title");

        GridPane summary = new GridPane();
        summary.setHgap(20);
        summary.setVgap(6);
        addRow(summary, 0, app.i18n().t("appointments.col.patient"), appointment.getPatient().getFullName());
        addRow(summary, 1, app.i18n().t("appointments.col.doctor"), appointment.getPrimaryDoctor().getFullName());
        addRow(summary, 2, app.i18n().t("appointments.col.when"),
                Formatters.dateTime(appointment.getScheduledAt(), app.i18n().getLocale()));
        addRow(summary, 3, app.i18n().t("appointments.col.purpose"),
                appointment.getPurpose() == null ? "" : appointment.getPurpose());

        ChoiceBox<AppointmentStatus> statusBox = new ChoiceBox<>();
        statusBox.getItems().setAll(AppointmentStatus.values());
        statusBox.setValue(appointment.getStatus());
        statusBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(AppointmentStatus s) { return s == null ? "" : app.i18n().t("status." + s.name()); }
            @Override public AppointmentStatus fromString(String s) { return null; }
        });
        Button save = new Button();
        save.textProperty().bind(app.i18n().binding("appointments.status.update"));
        save.setOnAction(e -> {
            try {
                appointment = app.appointmentService().updateStatus(appointment.getId(), statusBox.getValue());
                if (onChanged != null) onChanged.run();
                rebuild();
            } catch (RuntimeException ex) {
                Notifier.error("", ex.getMessage());
            }
        });
        HBox statusRow = new HBox(10, StatusTag.of(appointment.getStatus(), app.i18n()), statusBox, save);
        statusRow.getStyleClass().add("kpi-card");

        buildActionsColumns();
        rows.setAll(app.performedActionService().byAppointment(appointment.getId()));
        actionsTable.setItems(rows);
        actionsTable.setPlaceholder(new Label(app.i18n().t("common.empty")));
        VBox.setVgrow(actionsTable, Priority.SOMETIMES);
        actionsTable.setPrefHeight(220);

        Label actionsTitle = new Label();
        actionsTitle.textProperty().bind(app.i18n().binding("actions.title"));
        actionsTitle.getStyleClass().add("section-title");

        Node addForm = buildAddForm();

        root.getChildren().addAll(title, summary, statusRow, actionsTitle, actionsTable, addForm);
    }

    private Node buildAddForm() {
        ChoiceBox<ProcedureType> procedure = new ChoiceBox<>();
        procedure.getItems().setAll(app.procedureService().activeProcedures());

        TextField tooth = new TextField();
        TextField surfaces = new TextField();
        TextField anesthesia = new TextField();
        TextField notes = new TextField();
        Spinner<Integer> qty = new Spinner<>(1, 99, 1);
        qty.setEditable(true);
        TextField unitPrice = new TextField("0.00");

        procedure.valueProperty().addListener((obs, was, now) -> {
            if (now != null && now.getStandardPrice() != null) {
                unitPrice.setText(now.getStandardPrice().toPlainString());
            }
        });

        Button add = new Button();
        add.textProperty().bind(app.i18n().binding("actions.add"));
        add.getStyleClass().add("accent");
        add.setOnAction(e -> {
            try {
                if (procedure.getValue() == null) throw new IllegalArgumentException("Select a procedure");
                BigDecimal price = new BigDecimal(unitPrice.getText().trim());
                Doctor doctor = performingDoctor != null ? performingDoctor : appointment.getPrimaryDoctor();
                app.performedActionService().add(
                        appointment.getId(),
                        procedure.getValue().getId(),
                        doctor,
                        qty.getValue(),
                        price,
                        tooth.getText(),
                        surfaces.getText(),
                        anesthesia.getText(),
                        List.of(),
                        notes.getText());
                rebuild();
            } catch (RuntimeException ex) {
                Notifier.error(app.i18n().t("actions.add"), ex.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        bindRow(form, 0, "actions.field.procedure", procedure);
        bindRow(form, 1, "actions.field.tooth", tooth);
        bindRow(form, 2, "actions.field.surfaces", surfaces);
        bindRow(form, 3, "actions.field.anesthesia", anesthesia);
        bindRow(form, 4, "actions.field.notes", notes);
        bindRow(form, 5, "actions.field.qty", qty);
        bindRow(form, 6, "actions.field.unit", unitPrice);
        form.add(add, 1, 7);
        form.getStyleClass().add("kpi-card");
        return form;
    }

    private void bindRow(GridPane g, int row, String key, javafx.scene.Node ctl) {
        Label l = new Label();
        l.textProperty().bind(app.i18n().binding(key));
        g.add(l, 0, row);
        g.add(ctl, 1, row);
        GridPane.setHgrow(ctl, Priority.ALWAYS);
    }

    private static void addRow(GridPane g, int row, String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-font-weight: 600; -fx-text-fill: -color-fg-muted;");
        Label v = new Label(value);
        g.add(l, 0, row);
        g.add(v, 1, row);
    }

    private void buildActionsColumns() {
        TableColumn<PerformedAction, String> proc = new TableColumn<>();
        proc.textProperty().bind(app.i18n().binding("actions.col.procedure"));
        proc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProcedureType().getName()));
        proc.setMinWidth(180);

        TableColumn<PerformedAction, String> tooth = new TableColumn<>();
        tooth.textProperty().bind(app.i18n().binding("actions.col.tooth"));
        tooth.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getToothInvolved() == null ? "" : c.getValue().getToothInvolved()));
        tooth.setMinWidth(80);

        TableColumn<PerformedAction, String> qty = new TableColumn<>();
        qty.textProperty().bind(app.i18n().binding("actions.col.qty"));
        qty.setCellValueFactory(c -> new SimpleStringProperty(Integer.toString(c.getValue().getQuantity())));
        qty.setMinWidth(60);

        TableColumn<PerformedAction, String> unit = new TableColumn<>();
        unit.textProperty().bind(app.i18n().binding("actions.col.unit"));
        unit.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getUnitPrice() == null ? "" : c.getValue().getUnitPrice().toPlainString()));
        unit.setMinWidth(100);

        TableColumn<PerformedAction, String> total = new TableColumn<>();
        total.textProperty().bind(app.i18n().binding("actions.col.total"));
        total.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTotalPrice() == null ? "" : c.getValue().getTotalPrice().toPlainString()));
        total.setMinWidth(100);

        TableColumn<PerformedAction, PerformedAction> remove = new TableColumn<>();
        remove.textProperty().bind(app.i18n().binding("common.actions"));
        remove.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(c.getValue()));
        remove.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            { btn.textProperty().bind(app.i18n().binding("common.delete"));
              btn.setOnAction(e -> {
                  PerformedAction a = getItem();
                  if (a == null) return;
                  app.performedActionService().delete(a.getId());
                  rebuild();
              });
            }
            @Override
            protected void updateItem(PerformedAction item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : btn);
            }
        });

        actionsTable.getColumns().setAll(proc, tooth, qty, unit, total, remove);
    }
}
