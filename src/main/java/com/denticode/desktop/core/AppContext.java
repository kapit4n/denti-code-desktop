package com.denticode.desktop.core;

import com.denticode.desktop.domain.service.AppointmentService;
import com.denticode.desktop.domain.service.AuthService;
import com.denticode.desktop.domain.service.DoctorService;
import com.denticode.desktop.domain.service.InventoryService;
import com.denticode.desktop.domain.service.PatientService;
import com.denticode.desktop.domain.service.PaymentService;
import com.denticode.desktop.domain.service.PerformedActionService;
import com.denticode.desktop.domain.service.ProcedureService;
import com.denticode.desktop.infra.repo.AppointmentRepository;
import com.denticode.desktop.infra.repo.ConsultoryRepository;
import com.denticode.desktop.infra.repo.DoctorRepository;
import com.denticode.desktop.infra.repo.InventoryLineRepository;
import com.denticode.desktop.infra.repo.InventoryMovementRepository;
import com.denticode.desktop.infra.repo.PatientRepository;
import com.denticode.desktop.infra.repo.PaymentRepository;
import com.denticode.desktop.infra.repo.PerformedActionRepository;
import com.denticode.desktop.infra.repo.ProcedureTypeRepository;
import com.denticode.desktop.infra.repo.TreatmentFacilityRepository;
import com.denticode.desktop.infra.repo.UserRepository;
import com.denticode.desktop.infra.security.PasswordHasher;

/**
 * Hand-rolled service locator wiring the application's singletons together.
 *
 * Deliberately avoids a DI framework so the project stays small and
 * the dependencies of every component are explicit at this single edge.
 */
public final class AppContext implements AutoCloseable {

    private final Config config;
    private final Database database;
    private final Session session;
    private final EventBus eventBus;
    private final I18n i18n;

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PerformedActionRepository performedActionRepository;
    private final ProcedureTypeRepository procedureTypeRepository;
    private final TreatmentFacilityRepository treatmentFacilityRepository;
    private final ConsultoryRepository consultoryRepository;
    private final InventoryLineRepository inventoryLineRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final PaymentRepository paymentRepository;

    private final PasswordHasher passwordHasher;

    private final AuthService authService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final PerformedActionService performedActionService;
    private final ProcedureService procedureService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;

    public AppContext() {
        this.config = new Config();
        this.database = new Database(config);
        this.session = new Session();
        this.eventBus = new EventBus();
        this.i18n = new I18n(config.defaultLocale());

        this.userRepository = new UserRepository(database);
        this.doctorRepository = new DoctorRepository(database);
        this.patientRepository = new PatientRepository(database);
        this.appointmentRepository = new AppointmentRepository(database);
        this.performedActionRepository = new PerformedActionRepository(database);
        this.procedureTypeRepository = new ProcedureTypeRepository(database);
        this.treatmentFacilityRepository = new TreatmentFacilityRepository(database);
        this.consultoryRepository = new ConsultoryRepository(database);
        this.inventoryLineRepository = new InventoryLineRepository(database);
        this.inventoryMovementRepository = new InventoryMovementRepository(database);
        this.paymentRepository = new PaymentRepository(database);

        this.passwordHasher = new PasswordHasher();

        this.authService = new AuthService(userRepository, passwordHasher);
        this.patientService = new PatientService(patientRepository, eventBus);
        this.doctorService = new DoctorService(doctorRepository);
        this.appointmentService = new AppointmentService(
                appointmentRepository, patientRepository, doctorRepository, eventBus);
        this.performedActionService = new PerformedActionService(
                performedActionRepository, appointmentRepository, procedureTypeRepository, eventBus);
        this.procedureService = new ProcedureService(procedureTypeRepository, treatmentFacilityRepository);
        this.inventoryService = new InventoryService(
                consultoryRepository, inventoryLineRepository, inventoryMovementRepository, eventBus);
        this.paymentService = new PaymentService(paymentRepository, eventBus);
    }

    public Config config() { return config; }
    public Database database() { return database; }
    public Session session() { return session; }
    public EventBus eventBus() { return eventBus; }
    public I18n i18n() { return i18n; }

    public AuthService authService() { return authService; }
    public PatientService patientService() { return patientService; }
    public DoctorService doctorService() { return doctorService; }
    public AppointmentService appointmentService() { return appointmentService; }
    public PerformedActionService performedActionService() { return performedActionService; }
    public ProcedureService procedureService() { return procedureService; }
    public InventoryService inventoryService() { return inventoryService; }
    public PaymentService paymentService() { return paymentService; }

    @Override
    public void close() {
        database.close();
    }
}
