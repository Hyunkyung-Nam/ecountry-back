package com.growup.ecountry.repository;

import com.growup.ecountry.entity.Countries;
import com.growup.ecountry.entity.Students;
import com.growup.ecountry.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Students, Long> {
    List<Students> findAllByCountryId(Long countryId);
    @Query("select s from Students s where s.name = :name and s.pw = :pw")
    Optional<Students> findByNameANDPw(@Param("name")String name,@Param("pw")String pw);
    @Query("select s from Students s where s.id = :id and s.countries.id = :countryId")
    Optional<Students> findByIdANDCountryId(@Param("id") Long id, @Param("countryId") Long countryId);
}
