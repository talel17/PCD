package com.smartinterview.backend.repository;

import com.smartinterview.backend.entity.InterviewQuestion;
import com.smartinterview.backend.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    List<InterviewQuestion> findBySessionOrderByOrdreAsc(InterviewSession session);
}