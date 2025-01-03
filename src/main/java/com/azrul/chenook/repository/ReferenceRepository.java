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
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ReferenceRepository<R>  extends JpaRepository<R, Long>, JpaSpecificationExecutor<R>, RevisionRepository<R, Long, Long> { 
  
    
    /*@Query("SELECT r FROM Reference r WHERE :dependencyId NOT MEMBER OF r.dependencies")
    public Page<R> findAllReferencesExcludeSelected(Long dependencyId,  Pageable pageable);

    @Query("SELECT count(r) FROM Reference r WHERE :dependencyId NOT MEMBER OF r.dependencies")
    public Long countAllReferencesExcludeSelected(Long dependencyId);*/
    
   
    public Page<R> findAllByRefWorkId(Long refWorkId,  Pageable pageable);
   
    public Long countAllByRefWorkId(Long refWorkId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update #{#entityName} r set r.status = ?1 where r.status=?2 and r.refWorkId=?3")
    public void updateRefStatsByRefWork(ReferenceStatus targetStatus, ReferenceStatus conditionStatus, Long refWorkId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update #{#entityName} r set r.status = ?1, r.replacementOf=?4 where r.status=?2 and r.id=?3")
    public void updateRefStatsById(ReferenceStatus targetStatus, ReferenceStatus conditionStatus, Long refId, Long replacementOf);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update #{#entityName} r set r.status = ?1 where r.status=?2 and r.id=?3")
    public void updateRefStatsById(ReferenceStatus targetStatus, ReferenceStatus conditionStatus, Long refId);
          
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from #{#entityName} r where r.id=?1")
    public void removeById(Long refId);
}