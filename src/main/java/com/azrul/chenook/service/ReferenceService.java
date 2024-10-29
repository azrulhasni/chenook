package com.azrul.chenook.service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.azrul.chenook.domain.Reference;
import com.azrul.chenook.repository.ReferenceRepository;
import com.azrul.chenook.search.repository.ReferenceSearchRepository;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.workflow.SearchTermProvider;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;


public abstract class ReferenceService<R extends Reference> {
     
    protected abstract ReferenceRepository<R> getRefRepo();

    protected abstract ReferenceSearchRepository<R> getRefSearchRepo();

    public void save(R entity) {
        getRefRepo().save(entity);
    }
    
    public Integer countReferenceData(
            Class<R> referenceClass, 
            Long dependencyId
    ) {

            Long count = getRefRepo()
                    .countReferencesByDependency(dependencyId);
            return count.intValue();
        
    }
    
    public DataProvider getReferenceData( Class<R> referenceClass,PageNav pageNav, Long dependencyId){
         //build data provider
         var dp = new AbstractBackEndDataProvider<R, Void>() {
            @Override
            protected Stream<R> fetchFromBackEnd(Query<R, Void> query) {
                Sort.Direction sort = pageNav.getAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
                String sorted = pageNav.getSortField();
                query.getPage();
                    Page<R> finapps = getRefRepo().findReferencesByDependency(dependencyId,
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, sorted))
                    );
                    return finapps.stream();
                
            }

            @Override
            protected int sizeInBackEnd(Query<R, Void> query) {
                return pageNav.getDataCountPerPage();
            }

            @Override
            public String getId(R item) {
                return item.getId().toString();
            }

        };
        return dp;
    }

    public Integer countAllReferenceData(
            Class<R> referenceClass, 
            SearchTermProvider searchTermProvider

    ) {

        if (searchTermProvider==null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
            Long count = getRefRepo()
                    .count();
            return count.intValue();
        } else {
             Long count = getRefSearchRepo()
                     .count(
                             searchTermProvider.getSearchTerm()
                     );
            return count.intValue();
        }
    }

    public DataProvider getAllReferenceData(
            Class<R> referenceClass,
            SearchTermProvider searchTermProvider, 
            PageNav pageNav
    ) {
        //build data provider
        var dp = new AbstractBackEndDataProvider<R, Void>() {
            @Override
            protected Stream<R> fetchFromBackEnd(Query<R, Void> query) {
                Sort.Direction sort = pageNav.getAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
                String sorted = pageNav.getSortField();
                query.getPage();
                if (searchTermProvider==null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
                    Page<R> finapps = getRefRepo().findAll(
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, sorted))
                    );

                    return finapps.stream();
                } else {
                    Page<R> finapps = getRefSearchRepo().find(
                            searchTermProvider.getSearchTerm(),
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, modifySortFieldForSearch(sorted,referenceClass)))
                    );
                    return finapps.stream();
                }
            }

            @Override
            protected int sizeInBackEnd(Query<R, Void> query) {
                
                return pageNav.getDataCountPerPage();
            }

            @Override
            public String getId(R item) {
                return item.getId().toString();
            }

        };
        return dp;
    }

     private String modifySortFieldForSearch(String sortField,Class<R> workItemClass ){
        Field field = WorkflowUtils.getField(workItemClass, sortField);
//        if (String.class.equals(field.getType())){
//            return sortField+".keyword";
//        }else  if (Status.class.equals(field.getType())){
//            return sortField+".keyword";
//        }else if (Priority.class.equals(field.getType())){
//            return sortField+".keyword";
        if (Number.class.isAssignableFrom(field.getType()) || 
            LocalDateTime.class.isAssignableFrom(field.getType())||
            LocalDate.class.isAssignableFrom(field.getType()) ||
            Date.class.isAssignableFrom(field.getType())){
           return sortField; 
        }else {
            
            return sortField+".keyword";
        }
    }
}