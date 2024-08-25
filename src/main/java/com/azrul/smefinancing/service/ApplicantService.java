/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.service;

import com.azrul.chenook.domain.Attachment;
import com.azrul.smefinancing.domain.Applicant;
import com.azrul.smefinancing.domain.FinApplication;
import com.azrul.smefinancing.repository.ApplicantRepository;
import com.azrul.smefinancing.repository.FinApplicationRepository;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 */
@Service
public class ApplicantService {
    
    private final ApplicantRepository applicantRepo;
    private final FinApplicationRepository finappRepo;
    
    public ApplicantService(
            @Autowired FinApplicationRepository finappRepo,
            @Autowired ApplicantRepository applicantRepo
    ){
        this.applicantRepo=applicantRepo;
        this.finappRepo=finappRepo;
    }
    
    @Transactional
    public Applicant save(Applicant applicant, FinApplication finapp){
        FinApplication fa = finappRepo.getReferenceById(finapp.getId());
        applicant.setFinApplication(fa);
        Applicant a =  this.applicantRepo.save(applicant);
        return a;
    }
    
     public void remove(Applicant applicant){
        this.applicantRepo.delete(applicant);
    }
     
    public CallbackDataProvider.FetchCallback<Applicant, Void> getApplicantsByApplication(FinApplication finapp) {
        return query -> {
            var vaadinSortOrders = query.getSortOrders();
            var springSortOrders = new ArrayList<Sort.Order>();
            for (QuerySortOrder so : vaadinSortOrders) {
                String colKey = so.getSorted();
                if (so.getDirection() == SortDirection.ASCENDING) {
                    springSortOrders.add(Sort.Order.asc(colKey));
                }
            }
            Page<Applicant> applicants =  this.applicantRepo.findAll(
                    whereApplicationEquals(finapp),
                    PageRequest.of(
                            query.getPage(),
                            query.getPageSize(),
                            Sort.by(springSortOrders)
                    )
            );
            return applicants.stream();
        };
    }
    
    public Set<String> getApplicantsEmail(FinApplication finapp){
         List<Applicant> applicants =  this.applicantRepo.findAll(whereApplicationEquals(finapp));
         return applicants.stream().map(a->a.getEmail()).collect(Collectors.toSet());
    }
    
    public Set<Applicant> getApplicants(FinApplication finapp){
         List<Applicant> applicants =  this.applicantRepo.findAll(whereApplicationEquals(finapp));
         return applicants.stream().collect(Collectors.toSet());
    }
    
    public Long countApplicants(FinApplication finapp){
         return this.applicantRepo.count(whereApplicationEquals(finapp));
    }
    
     private static Specification<Applicant> whereApplicationEquals(FinApplication application) {
        return (applicant, cq, cb) -> {
            return cb.equal(applicant.get("finApplication"), application)
            ;
        };
    }
     
     
}
