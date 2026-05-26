package com.example.academy.enrollment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.academy.enrollment.domain.Enrollment;

public interface JpaEnrollmentRepository extends JpaRepository<Enrollment, Long> {
}
