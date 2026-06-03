package com.denticode.desktop.ui.doctor;

import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.core.EventBus;
import com.denticode.desktop.domain.model.Appointment;
import com.denticode.desktop.domain.model.AppointmentStatus;
import com.denticode.desktop.domain.model.Doctor;
import com.denticode.desktop.domain.model.PerformedAction;
import com.denticode.desktop.ui.common.Formatters;
import com.denticode.desktop.ui.components.VisitCardController;
import com.denticode.desktop.ui.shell.PortalShell;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.fxml.FXMLLoader;

public final class DoctorDashboardController implements Initializable {

    private final AppContext app;
    private final Doctor self;
    private final PortalShell portal;
    private final Consumer<Appointment> openAppointment;

    @FXML
    private VBox dashboardRoot;
    @FXML
    private Label welcomeTitle;
    @FXML
    private Label welcomeSubtitle;
    @FXML
    private GridPane statGrid;
    @FXML
    private Label statApptTitle;
    @FXML
    private Label statApptValue;
    @FXML
    private Label statApptBadge;
    @FXML
    private Label statApptGrowth;
    @FXML
    private Label statDoneTitle;
    @FXML
    private Label statDoneValue;
    @FXML
    private Label statDoneBadge;
    @FXML
    private Label statDoneGrowth;
    @FXML
    private Label statPatTitle;
    @FXML
    private Label statPatValue;
    @FXML
    private Label statPatBadge;
    @FXML
    private Label statPatGrowth;
    @FXML
    private Label statRevTitle;
    @FXML
    private Label statRevValue;
    @FXML
    private Label statRevBadge;
    @FXML
    private Label statRevGrowth;
    @FXML
    private Label todayVisitsTitle;
    @FXML
    private VBox todayVisitsList;
    @FXML
    private Label upcomingTitle;
    @FXML
    private VBox upcomingList;
    @FXML
    private Label quickTitle;
    @FXML
    private VBox quickActions;
    @FXML
    private Label chartTitle;
    @FXML
    private BarChart<String, Number> weekChart;

    public DoctorDashboardController(AppContext app, Doctor self, PortalShell portal,
                                     Consumer<Appointment> openAppointment) {
        this.app = app;
        this.self = self;
        this.portal = portal;
        this.openAppointment = openAppointment;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        welcomeTitle.textProperty().bind(Bindings.createStringBinding(() -> {
            if (self == null) {
                return app.i18n().t("dashboard.welcome.anon");
            }
            return app.i18n().t("dashboard.welcome", self.getFullName());
        }, app.i18n().localeProperty()));
        welcomeSubtitle.textProperty().bind(app.i18n().binding("dashboard.welcome.subtitle"));

        statApptTitle.textProperty().bind(app.i18n().binding("dashboard.stat.appointments"));
        statDoneTitle.textProperty().bind(app.i18n().binding("dashboard.stat.completed"));
        statPatTitle.textProperty().bind(app.i18n().binding("dashboard.stat.patients"));
        statRevTitle.textProperty().bind(app.i18n().binding("dashboard.stat.revenue"));

        statApptBadge.textProperty().bind(app.i18n().binding("dashboard.stat.badge.upcoming"));
        statDoneBadge.textProperty().bind(app.i18n().binding("dashboard.stat.badge.period"));
        statPatBadge.textProperty().bind(app.i18n().binding("dashboard.stat.badge.unique"));
        statRevBadge.textProperty().bind(app.i18n().binding("dashboard.stat.badge.actions"));

        todayVisitsTitle.textProperty().bind(app.i18n().binding("dashboard.todaysVisits"));
        upcomingTitle.textProperty().bind(app.i18n().binding("dashboard.upcoming"));
        quickTitle.textProperty().bind(app.i18n().binding("dashboard.quickActions"));
        chartTitle.textProperty().bind(app.i18n().binding("dashboard.chart.week"));

        statApptGrowth.setText("+8%");
        statDoneGrowth.setText("+12%");
        statPatGrowth.setText("+5%");
        statRevGrowth.setText("+15%");

        wireQuickActions();
        attachStatHoverEffects();
        setupChartAxes();

        refresh();
        Runnable r = () -> Platform.runLater(this::refresh);
        app.eventBus().<Object>subscribe(EventBus.APPOINTMENT_CHANGED, ignored -> r.run());
        app.eventBus().<Object>subscribe(EventBus.PERFORMED_ACTION_CHANGED, ignored -> r.run());
    }

    private void wireQuickActions() {
        quickActions.getChildren().clear();
        quickActions.getChildren().add(quickBtn("dashboard.quick.newAppointment", FontAwesomeSolid.CALENDAR,
                () -> portal.navigateToView("appointments")));
        quickActions.getChildren().add(quickBtn("dashboard.quick.addPatient", FontAwesomeSolid.USER_PLUS,
                () -> portal.navigateToView("patients")));
        quickActions.getChildren().add(quickBtn("dashboard.quick.records", FontAwesomeSolid.FOLDER_OPEN,
                () -> portal.navigateToView("patients")));
        quickActions.getChildren().add(quickBtn("dashboard.quick.reports", FontAwesomeSolid.CHART_BAR,
                () -> portal.navigateToView("appointments")));
    }

    private Button quickBtn(String i18nKey, FontAwesomeSolid icon, Runnable action) {
        Button b = new Button();
        b.textProperty().bind(app.i18n().binding(i18nKey));
        FontIcon ic = new FontIcon(icon);
        ic.setIconSize(16);
        b.setGraphic(ic);
        b.getStyleClass().add("quick-action-btn");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> action.run());
        return b;
    }

    private void attachStatHoverEffects() {
        for (Node n : statGrid.getChildrenUnmodifiable()) {
            if (!(n instanceof VBox v) || !v.getStyleClass().contains("stat-card")) {
                continue;
            }
            v.setOnMouseEntered(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(120), v);
                st.setToX(1.02);
                st.setToY(1.02);
                st.play();
            });
            v.setOnMouseExited(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(120), v);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });
        }
    }

    private void setupChartAxes() {
        CategoryAxis x = (CategoryAxis) weekChart.getXAxis();
        x.setTickLabelRotation(0);
        NumberAxis y = (NumberAxis) weekChart.getYAxis();
        y.setForceZeroInRange(true);
        y.setMinorTickVisible(false);
    }

    private void refresh() {
        if (self == null) {
            clearStats();
            todayVisitsList.getChildren().setAll(new Label(app.i18n().t("common.empty")));
            upcomingList.getChildren().setAll(new Label(app.i18n().t("common.empty")));
            weekChart.getData().clear();
            return;
        }
        List<Appointment> mine = app.appointmentService().byDoctor(self.getId());
        long upcomingCount = mine.stream().filter(a -> a.getScheduledAt().isAfter(LocalDateTime.now())).count();
        long completed = mine.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long uniquePatients = mine.stream().map(a -> a.getPatient().getId()).distinct().count();
        BigDecimal revenue = revenueForDoctor(mine);

        statApptValue.setText(Long.toString(upcomingCount));
        statDoneValue.setText(Long.toString(completed));
        statPatValue.setText(Long.toString(uniquePatients));
        statRevValue.setText(Formatters.currency(revenue, app.i18n().getLocale()));

        LocalDate today = LocalDate.now();
        List<Appointment> todayVisits = mine.stream()
                .filter(a -> a.getScheduledAt().toLocalDate().equals(today))
                .sorted((x, y) -> x.getScheduledAt().compareTo(y.getScheduledAt()))
                .toList();

        todayVisitsList.getChildren().clear();
        if (todayVisits.isEmpty()) {
            Label empty = new Label(app.i18n().t("dashboard.todaysVisits.empty"));
            empty.getStyleClass().add("empty-state");
            todayVisitsList.getChildren().add(empty);
        } else {
            for (Appointment a : todayVisits) {
                todayVisitsList.getChildren().add(buildVisitCard(a));
            }
        }

        upcomingList.getChildren().clear();
        List<Appointment> upcoming = mine.stream()
                .filter(a -> a.getScheduledAt().isAfter(LocalDateTime.now()))
                .sorted((x, y) -> x.getScheduledAt().compareTo(y.getScheduledAt()))
                .limit(6)
                .toList();
        if (upcoming.isEmpty()) {
            upcomingList.getChildren().add(new Label(app.i18n().t("common.empty")));
        } else {
            for (Appointment a : upcoming) {
                Label line = new Label(shortUpcomingLine(a));
                line.getStyleClass().add("upcoming-line");
                line.setWrapText(true);
                upcomingList.getChildren().add(line);
            }
        }

        fillWeekChart(mine);
    }

    private String shortUpcomingLine(Appointment a) {
        String when = Formatters.dateTime(a.getScheduledAt(), app.i18n().getLocale());
        return when + " · " + a.getPatient().getFullName();
    }

    private BigDecimal revenueForDoctor(List<Appointment> mine) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Appointment a : mine) {
            for (PerformedAction pa : app.performedActionService().byAppointment(a.getId())) {
                if (pa.getPerformingDoctor() != null && self != null
                        && pa.getPerformingDoctor().getId().equals(self.getId())) {
                    BigDecimal t = pa.getTotalPrice();
                    sum = sum.add(t != null ? t : BigDecimal.ZERO);
                }
            }
        }
        return sum;
    }

    private void clearStats() {
        statApptValue.setText("0");
        statDoneValue.setText("0");
        statPatValue.setText("0");
        statRevValue.setText(Formatters.currency(BigDecimal.ZERO, app.i18n().getLocale()));
    }

    private Node buildVisitCard(Appointment a) {
        try {
            FXMLLoader loader = new FXMLLoader(DoctorDashboardController.class.getResource("/fxml/components/VisitCard.fxml"));
            VisitCardController c = new VisitCardController();
            loader.setController(c);
            Node n = loader.load();
            c.init(app, a, openAppointment);
            return n;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void fillWeekChart(List<Appointment> mine) {
        weekChart.getData().clear();
        Locale loc = app.i18n().getLocale();
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);
        List<String> cats = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            cats.add(d.getDayOfWeek().getDisplayName(TextStyle.SHORT, loc));
        }
        int[] counts = new int[7];
        for (Appointment a : mine) {
            LocalDate d = a.getScheduledAt().toLocalDate();
            if (!d.isBefore(start) && !d.isAfter(today)) {
                int idx = (int) java.time.temporal.ChronoUnit.DAYS.between(start, d);
                if (idx >= 0 && idx < 7) {
                    counts[idx]++;
                }
            }
        }
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(app.i18n().t("dashboard.chart.series"));
        for (int i = 0; i < 7; i++) {
            series.getData().add(new XYChart.Data<>(cats.get(i), counts[i]));
        }
        weekChart.getData().add(series);
    }
}
