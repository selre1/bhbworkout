package com.bhbworkout.event;

import com.bhbworkout.domain.Account;
import com.bhbworkout.domain.Enrollment;
import com.bhbworkout.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);
}
