/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.service;

import com.azrul.chenook.views.common.PageNav;
import com.azrul.smefinancing.domain.Applicant;
import com.azrul.smefinancing.domain.FinApplication;
import com.azrul.smefinancing.repository.FinApplicationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider.FetchCallback;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.azrul.smefinancing.repository.ApplicantRepository;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import jakarta.persistence.criteria.Join;
import java.util.stream.Stream;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 */
@Service
public class FinApplicationService {

    private final FinApplicationRepository finAppRepo;
    private final ApplicantRepository contantPersonRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public FinApplicationService(
            @Autowired FinApplicationRepository finAppRepo,
            @Autowired ApplicantRepository contantPersonRepo
    ) {
        this.finAppRepo = finAppRepo;
        this.contantPersonRepo = contantPersonRepo;
    }

    public FetchCallback<FinApplication, Void> getApplicationsByUsernameOrEmail(String username, String email) {
        return query -> {
            var vaadinSortOrders = query.getSortOrders();
            var springSortOrders = new ArrayList<Sort.Order>();
            for (QuerySortOrder so : vaadinSortOrders) {
                String colKey = so.getSorted();
                if (so.getDirection() == SortDirection.ASCENDING) {
                    springSortOrders.add(Sort.Order.asc(colKey));
                }
            }
            return finAppRepo.findAll(
                    //whereUsernameEquals(username),
                    whereUsernameEqualsOrApplicantEmailEquals(username,email),
                    PageRequest.of(
                            query.getPage(),
                            query.getPageSize(),
                            Sort.by(springSortOrders)
                    )
            ).stream();
        };
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
    
    public DataProvider buildFinApplicationsDataProvider(PageNav pageNav) {
        //build data provider
        var dp = new AbstractBackEndDataProvider<FinApplication, Void>() {
            @Override
            protected Stream<FinApplication> fetchFromBackEnd(Query<FinApplication, Void> query) {
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

                Page<FinApplication> finapps = getFinApplications(
                        pageNav.getPage() - 1,
                        pageNav.getMaxCountPerPage(),
                        Sort.by(sort, sorted)
                );
                return finapps.stream();
            }

            @Override
            protected int sizeInBackEnd(Query<FinApplication, Void> query) {

                return pageNav.getDataCountPerPage();
            }

            @Override
            public String getId(FinApplication item) {

                return item.getId().toString();
            }

        };
        return dp;
    }

    @Transactional
    public FinApplication save(FinApplication app, String username) {
            if (app.getUsername()==null){
                app.setUsername(username);
            }
            for (Applicant a:app.getApplicants()){
                a.setFinApplication(app);
            }
            return finAppRepo.save(app);
    }
    
   

    
    @Transactional
    public void save(FinApplication app) {
            finAppRepo.save(app);
    }
    
    @Transactional
    public void remove(FinApplication app) {
            finAppRepo.delete(app);
    }
    
    public FinApplication refresh(FinApplication app) {
            return finAppRepo.getReferenceById(app.getId());
    }

    private static Specification<FinApplication> whereUsernameEquals(String username) {
        return (finApp, cq, cb) -> {
            return cb.equal(finApp.get("username"), username);
        };
    }
    
    private static Specification<FinApplication> whereUsernameEqualsOrApplicantEmailEquals(String username, String applicantEmail) {
        return (finApp, cq, cb) -> {
            Join<Applicant, FinApplication> applicants = finApp.join("applicants");
            return cb.or(
                    cb.equal(finApp.get("username"), username),
                    cb.equal(applicants.get("email"),applicantEmail)
            );
        };
    }
}
