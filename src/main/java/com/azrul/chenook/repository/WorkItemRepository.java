/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.repository;

import com.azrul.chenook.domain.Approval;
import com.azrul.chenook.domain.ReferenceStatus;
import com.azrul.chenook.domain.WorkItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 */
@NoRepositoryBean
public interface WorkItemRepository<T extends WorkItem> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    //public T findOneByParentIdAndContext(Long parentId,String context);
//    @Query(value = "SELECT * FROM work_item WHERE :username = ANY(work_item.owners)", nativeQuery = true)    
//    public Page<T> findByOwner(String username,  Pageable pageable);
//    
//    @Query(value = "SELECT COUNT(*) FROM work_item WHERE :username = ANY(work_item.owners)", nativeQuery = true)    
//    public Long countByOwner(String username);
//    
//    @Query(value = "SELECT * FROM work_item WHERE owners = '{}' AND worklist = :worklist", nativeQuery = true)    
//    public Page<T> findByWorklistAndNoOwner(String worklist,  Pageable pageable);
//    
//     @Query(value = "SELECT COUNT(*) FROM work_item WHERE owners = '{}' AND worklist = :worklist", nativeQuery = true)    
//    public Long countByWorklistAndNoOwner(String worklist);
    
  @Query(value="""
               select
                       count(distinct fa1_0.id)
                   from
                       work_item fa1_0 
                   left join
                       biz_user o1_0 
                           on fa1_0.id=o1_0.work_id 
                   left join
                       approval a1_0 
                           on fa1_0.id=a1_0.work_id 
                   where
                       (
                           o1_0.username=:username
                           or (a1_0.username=:username 
                           and a1_0.approved is null)
                       ) 
                       and fa1_0.item_type='FIN_APP'
               """, nativeQuery = true)
    public Long countWhereOwnersOrUndecidedApprovalsContains(String username);
    
    @Query(value="""
               select
                        wi1_0.id,
                               wi1_0.item_type,
                               wi1_0.context,
                               wi1_0.creator,
                               wi1_0.priority,
                               wi1_0.start_event_description,
                               wi1_0.start_event_id,
                               wi1_0.status,
                               wi1_0.supervisor_approval_level,
                               wi1_0.supervisor_approval_seeker,
                               wi1_0.tenant,
                               wi1_0.worklist,
                               wi1_0.worklist_update_time,
                               wi1_0.address,
                               wi1_0.application_date,
                               wi1_0.created_by,
                               wi1_0.creation_date,
                               wi1_0.financing_amount,
                               wi1_0.financing_currency,
                               wi1_0.last_modified_by,
                               wi1_0.last_modified_date,
                               wi1_0.main_business_activity,
                               wi1_0.name,
                               wi1_0.postal_code,
                               wi1_0.reason_for_financing,
                               wi1_0.ssm_registration_number,
                               wi1_0.state,
                               wi1_0.version 
                   from
                       work_item wi1_0 
                   left join
                       biz_user o1_0 
                           on wi1_0.id=o1_0.work_id 
                   left join
                       approval a1_0 
                           on wi1_0.id=a1_0.work_id 
                   where
                       (
                           o1_0.username=:username
                           or (a1_0.username=:username 
                           and a1_0.approved is null)
                       ) 
                       and wi1_0.item_type='FIN_APP'
               """, nativeQuery = true)
    public Page<T> findAllWhereOwnersOrUndecidedApprovalsContains(String username,Pageable pageable);
    
   
}
