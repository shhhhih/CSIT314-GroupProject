package com.team.idp.repository;

import com.team.idp.entity.Company;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByUserID(Long userID);

}