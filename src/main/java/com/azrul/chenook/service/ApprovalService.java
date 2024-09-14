/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

import com.azrul.chenook.domain.Approval;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.repository.ApprovalRepository;
import com.azrul.chenook.views.common.PageNav;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 *
 * @author azrul
 */
@Service
public class ApprovalService {
    private final ApprovalRepository approvalRepo;
    
    
    public ApprovalService(
        @Autowired ApprovalRepository approvalRepo    
    ){
        this.approvalRepo=approvalRepo;
    }
    
    public Approval save(Approval approval){
       return this.approvalRepo.save(approval);
    }
    
    
    public DataProvider getApprovalsByWork(WorkItem work, PageNav pageNav) {
        //build data provider
        var dp = new AbstractBackEndDataProvider<Approval, Void>() {
            @Override
            protected Stream<Approval> fetchFromBackEnd(Query<Approval, Void> query) {
                QuerySortOrder so = query.getSortOrders().isEmpty() ? null : query.getSortOrders().get(0);
                
                Sort.Direction sort =   so==null
                                        ?Sort.Direction.DESC
                                        :(
                                            SortDirection.ASCENDING.equals(so.getDirection())
                                            ?Sort.Direction.ASC
                                            :Sort.Direction.DESC
                                        );
                String sorted = so==null
                                ?"id"
                                :so.getSorted();
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
   
}
