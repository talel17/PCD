package com.smartinterview.backend.repository;

import com.smartinterview.backend.entity.Report;
import com.smartinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUserOrderByCreatedAtDesc(User user);
    Optional<Report> findBySessionId(Long sessionId);
}