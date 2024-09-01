/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.repository.SignatureRepository;
import com.azrul.chenook.repository.WorkItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 */
@Service
public class WorkItemService {
    private  WorkItemRepository workItemRepo;
    
    public WorkItemService(
           @Autowired WorkItemRepository workItemRepo
    ){
        this.workItemRepo=workItemRepo;
    }
    
    @Transactional
    public WorkItem save(WorkItem work){
        return workItemRepo.save(work);
    }
    
    @Transactional
    public WorkItem createApprovalAndSave(WorkItem work){
        
        work.getHistoricalApprovals().addAll(work.getApprovals());
        work.getApprovals().clear();
        WorkItem w = workItemRepo.save(work);
        return w;
    }
    
    @Transactional
    public WorkItem archiveApprovalsAndSave(WorkItem work){
        work.getHistoricalApprovals().addAll(work.getApprovals());
        work.getApprovals().clear();
        WorkItem w = workItemRepo.save(work);
        return w;
    }
    
    @Transactional
    public WorkItem findOneByParentIdAndContext(Long parentId,String context){
        return workItemRepo.findOneByParentIdAndContext(parentId, context);
    }
}
