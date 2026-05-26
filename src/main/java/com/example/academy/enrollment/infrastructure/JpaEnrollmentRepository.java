package com.example.academy.enrollment.infrastructure;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.academy.enrollment.domain.Enrollment;
import com.example.academy.enrollment.domain.EnrollmentState;

public interface JpaEnrollmentRepository extends JpaRepository<Enrollment, Long> {

	@Query("SELECT e FROM Enrollment e JOIN FETCH e.course c WHERE e.user.id = :userId AND e.state IN :states")
	Page<Enrollment> findPageByUserIdAndStateIn(
		@Param("userId") Long userId,
		@Param("states") List<EnrollmentState> states,
		Pageable pageable
	);
}
