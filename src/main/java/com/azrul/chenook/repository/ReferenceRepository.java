package com.azrul.chenook.repository;
import com.azrul.chenook.domain.ReferenceStatus;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ReferenceRepository<R>  extends JpaRepository<R, Long>, JpaSpecificationExecutor<R> { 
  
    
    /*@Query("SELECT r FROM Reference r WHERE :dependencyId NOT MEMBER OF r.dependencies")
    public Page<R> findAllReferencesExcludeSelected(Long dependencyId,  Pageable pageable);

    @Query("SELECT count(r) FROM Reference r WHERE :dependencyId NOT MEMBER OF r.dependencies")
    public Long countAllReferencesExcludeSelected(Long dependencyId);*/
    
   
    public Page<R> findAllByRefWorkId(Long refWorkId,  Pageable pageable);
   
    public Long countAllByRefWorkId(Long refWorkId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update #{#entityName} r set r.status = ?1 where r.refWorkId=?2")
    public void updateRefStatusGivenRefWork(ReferenceStatus status, Long refWorkId);
}