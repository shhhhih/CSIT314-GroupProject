
package com.team.idp.repository;

import com.team.idp.entity.IDP;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IDPRepository extends JpaRepository<IDP, Long> {

    // Retrieve all projects belonging to a company
    List<IDP> findByCompanyID(Long companyID);

    // Retrieve projects belonging to a company, sorted
    List<IDP> findByCompanyID(Long companyID, Sort sort);

    // Retrieve a specific project belonging to a company
    Optional<IDP> findByIdpIDAndCompanyID(
            Long idpID, Long companyID);
}

