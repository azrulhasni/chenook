/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

import com.azrul.chenook.domain.Approval;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.repository.ApprovalRepository;
import com.azrul.chenook.views.common.components.PageNav;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import jakarta.persistence.EntityManagerFactory;
import java.util.stream.Stream;
import org.hibernate.SessionFactory;
import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 *
 * @author azrul
 */
@Service
public class ApprovalService {
    private final ApprovalRepository approvalRepo;
    private final EntityManagerFactory emFactory;
    
    
    public ApprovalService(
        @Autowired ApprovalRepository approvalRepo,
        @Autowired EntityManagerFactory emFactory
    ){
        this.approvalRepo=approvalRepo;
        this.emFactory=emFactory;
    }
    
    public Approval save(Approval approval){
       return this.approvalRepo.save(approval);
    }
    
    public Integer countApprovalsByWork(WorkItem work){
        return approvalRepo.countByWork(work.getId()).intValue();
    }
    
    public Integer countHistoricalApprovalsByWork(WorkItem work){
        return approvalRepo.countHistoricalByWork(work.getId()).intValue();
    }
    
    public DataProvider getApprovalsByWork(WorkItem work, PageNav pageNav) {
        //build data provider
        var dp = new AbstractBackEndDataProvider<Approval, Void>() {
            @Override
            protected Stream<Approval> fetchFromBackEnd(Query<Approval, Void> query) {

                Sort.Direction sort = pageNav.getAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
                String sortedField = pageNav.getSortField();
                SessionFactory sessionFactory = emFactory.unwrap(SessionFactory.class);
                AbstractEntityPersister persister = ((AbstractEntityPersister) ((MappingMetamodel) sessionFactory.getMetamodel()).getEntityDescriptor(Approval.class));
                String sorted = persister.getPropertyColumnNames(sortedField)[0];
    
                query.getPage();
                Page<Approval> finapps = approvalRepo.findByWork(work.getId(),
                    PageRequest.of(
                            pageNav.getPage() - 1,
                            pageNav.getMaxCountPerPage(),
                            Sort.by(sort, sorted))
                );
                return finapps.stream();
            }

            @Override
            protected int sizeInBackEnd(Query<Approval, Void> query) {
                return pageNav.getDataCountPerPage();
            }

            @Override
            public String getId(Approval item) {
                return item.getId().toString();
            }

        };
        return dp;
    }
    
    public DataProvider<Approval,Void> getHistoricalApprovalsByWork(WorkItem work, PageNav pageNav) {
        //build data provider
        var dp = new AbstractBackEndDataProvider<Approval, Void>() {
            @Override
            protected Stream<Approval> fetchFromBackEnd(Query<Approval, Void> query) {

                Sort.Direction sort = pageNav.getAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
                String sortedField = pageNav.getSortField();
                SessionFactory sessionFactory = emFactory.unwrap(SessionFactory.class);
                AbstractEntityPersister persister = ((AbstractEntityPersister) ((MappingMetamodel) sessionFactory.getMetamodel()).getEntityDescriptor(Approval.class));
                String sorted = persister.getPropertyColumnNames(sortedField)[0];
    
                query.getPage();
                Page<Approval> finapps = approvalRepo.findHistoricalByWork(work.getId(),
                    PageRequest.of(
                            pageNav.getPage() - 1,
                            pageNav.getMaxCountPerPage(),
                            Sort.by(sort, sorted))
                );
                return finapps.stream();
            }

            @Override
            protected int sizeInBackEnd(Query<Approval, Void> query) {
                return pageNav.getDataCountPerPage();
            }

            @Override
            public String getId(Approval item) {
                return item.getId().toString();
            }

        };
        return dp;
    }
    
     
   
}
