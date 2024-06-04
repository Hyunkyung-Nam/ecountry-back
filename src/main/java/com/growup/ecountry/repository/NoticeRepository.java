package com.growup.ecountry.repository;

import com.growup.ecountry.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("select n from Notice n where n.studentId = :studentId order by n.createdAt desc")
    List<Notice> findAllByStudentId(@Param("studentId") Long studentId);
}
