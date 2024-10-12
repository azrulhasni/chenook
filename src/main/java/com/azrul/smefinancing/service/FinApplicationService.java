/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.service;

import com.azrul.chenook.repository.WorkItemRepository;
import com.azrul.chenook.search.repository.WorkItemSearchRepository;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.smefinancing.domain.Applicant;
import com.azrul.smefinancing.domain.FinApplication;
import com.azrul.smefinancing.repository.FinApplicationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.azrul.smefinancing.repository.ApplicantRepository;
import com.azrul.smefinancing.search.repository.FinApplicationSearchRepository;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 */
@Service
@Transactional
public class FinApplicationService extends WorkflowService<FinApplication> {

    private final FinApplicationRepository finAppRepo;
    private final ApplicantRepository contantPersonRepo;
    private final ObjectMapper mapper = new ObjectMapper();
    private final FinApplicationSearchRepository finAppSearchRepo;

    public FinApplicationService(
            @Autowired FinApplicationRepository finAppRepo,
            @Autowired FinApplicationSearchRepository finAppSearchRepo,
            @Autowired ApplicantRepository contantPersonRepo
    ) {
        this.finAppRepo = finAppRepo;
        this.contantPersonRepo = contantPersonRepo;
        this.finAppSearchRepo=finAppSearchRepo;
    }
    
    public FinApplication getById(Long id){
        Optional<FinApplication> ofinapp = finAppRepo.findById(id);
        return ofinapp.orElse(null);
    }

    private Page<FinApplication> getFinApplications(
            Integer page,
            Integer countPerPage,
            Sort sort){
        Pageable pageable = PageRequest.of(page,countPerPage, sort);
        return finAppRepo.findAll(pageable);
    }
    
    public Long countFinApplications(){
        return finAppRepo.count();
    }

    @Override
    public FinApplication save(FinApplication finapp) {
            for (Applicant a:finapp.getApplicants()){
                a.setFinApplication(finapp);
            }
            finAppSearchRepo.save(finapp);
            return finAppRepo.save(finapp);
            
    }
    
    public void remove(FinApplication app) {
            finAppSearchRepo.delete(app);
            finAppRepo.delete(app);
    }
    
    public FinApplication refresh(FinApplication app) {
            return finAppRepo.getReferenceById(app.getId());
    }

    @Override
    public WorkItemRepository<FinApplication> getWorkItemRepo() {
        return finAppRepo;
    }

     @Override
    public WorkItemSearchRepository<FinApplication> getWorkItemSearchRepo() {
        return finAppSearchRepo;
    }
    
}
