package com.azrul.chenook.repository;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface ReferenceRepository<R>  extends JpaRepository<R, Long>, JpaSpecificationExecutor<R> { 
  

    /*@Query("SELECT r FROM Reference r WHERE :dependencyId NOT MEMBER OF r.dependencies")
    public Page<R> findAllReferencesExcludeSelected(Long dependencyId,  Pageable pageable);

    @Query("SELECT count(r) FROM Reference r WHERE :dependencyId NOT MEMBER OF r.dependencies")
    public Long countAllReferencesExcludeSelected(Long dependencyId);*/
}