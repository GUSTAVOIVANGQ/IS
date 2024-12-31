package com.web.book.version.repository;

import com.web.book.version.model.Recommendation;
import com.web.book.version.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByUser(User user);
    @Modifying
    @Query("DELETE FROM Recommendation r WHERE r.user = :user AND r.generatedDate < :date")
    void deleteByUserAndGeneratedDateBefore(@Param("user") User user, @Param("date") LocalDateTime date);

    Recommendation save(Recommendation recommendation);

    @Query("SELECT r FROM Recommendation r WHERE r.user = :user AND r.generatedDate >= :date")
    List<Recommendation> findByUserAndGeneratedDateAfter(@Param("user") User user, @Param("date") LocalDateTime date);
}
