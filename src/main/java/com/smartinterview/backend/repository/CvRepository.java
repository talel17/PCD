package com.smartinterview.backend.repository;

import com.smartinterview.backend.entity.Cv;
import com.smartinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CvRepository extends JpaRepository<Cv, Long> {
    List<Cv> findByUserOrderByUploadedAtDesc(User user);
}