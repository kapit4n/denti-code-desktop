package com.denticode.desktop.domain.service;

import com.denticode.desktop.domain.model.Doctor;
import com.denticode.desktop.infra.repo.DoctorRepository;

import java.util.List;
import java.util.Optional;

public final class DoctorService {

    private final DoctorRepository doctors;

    public DoctorService(DoctorRepository doctors) {
        this.doctors = doctors;
    }

    public List<Doctor> active() {
        return doctors.activeDoctors();
    }

    public List<Doctor> all() {
        return doctors.findAll();
    }

    public Optional<Doctor> find(Long id) {
        return doctors.findById(id);
    }

    public Optional<Doctor> findByUserId(Long userId) {
        return doctors.findByUserId(userId);
    }

    public long count() {
        return doctors.count();
    }
}
