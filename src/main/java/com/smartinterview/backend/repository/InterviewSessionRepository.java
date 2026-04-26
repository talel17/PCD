package com.smartinterview.backend.repository;

import com.smartinterview.backend.entity.InterviewSession;
import com.smartinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByUserOrderByCreatedAtDesc(User user);
}