package com.azrul.chenook.service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.azrul.chenook.domain.Reference;
import com.azrul.chenook.domain.ReferenceMap;
import com.azrul.chenook.repository.ReferenceMapRepository;
import com.azrul.chenook.repository.ReferenceRepository;
import com.azrul.chenook.search.repository.ReferenceSearchRepository;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.workflow.SearchTermProvider;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

import jakarta.transaction.Transactional;

public abstract class ReferenceService<R extends Reference> {

    protected abstract ReferenceRepository<R> getRefRepo();

    protected abstract ReferenceSearchRepository<R> getRefSearchRepo();

    protected ReferenceMapRepository<R> refMapRepo;

    public void save(R entity) {
        getRefRepo().save(entity);
        getRefSearchRepo().save(entity);
    }

    @Transactional
    public void saveMap(Long parentId, Class<R> referenceClass, Set<R> references, String context) {
        ReferenceMap<R> refMap = refMapRepo.findReferencesByParent(parentId, referenceClass.getCanonicalName());
        if (refMap == null) {
            refMap = new ReferenceMap<>(parentId, referenceClass.getCanonicalName(), context);
            refMap.setReferences(references);
            refMapRepo.save(refMap);
        } else {
            refMap.getReferences().clear();
            refMap.getReferences().addAll(references);
            refMapRepo.save(refMap);
        }
    }

    @Transactional
    public ReferenceMap<R> getMap(Long parentId, Class<R> referenceClass) {
        return refMapRepo.findReferencesByParent(parentId, referenceClass.getCanonicalName());
    }

    public Integer countReferenceData(
            Class<R> referenceClass, 
            Long parentId
    ) {
        Long count = refMapRepo.countReferencesByParent(parentId, referenceClass.getCanonicalName());                    
        return count.intValue();
    }

    public DataProvider<R, Void> getReferenceData(Class<R> referenceClass, PageNav pageNav, Long parentId) {
        // build data provider
        var dp = new AbstractBackEndDataProvider<R, Void>() {
            @Override
            protected Stream<R> fetchFromBackEnd(Query<R, Void> query) {
                // Sort.Direction sort = pageNav.getAsc() ? Sort.Direction.ASC :
                // Sort.Direction.DESC;
                // String sorted = pageNav.getSortField();
                QuerySortOrder so = query.getSortOrders().isEmpty() ? null : query.getSortOrders().get(0);
                Sort.Direction sort = so == null ? Sort.Direction.DESC
                        : (so.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC);
                String sorted = so == null ? "id" : so.getSorted();
                query.getPage();
                Page<R> finapps = refMapRepo.findReferencesByParent(parentId, referenceClass.getCanonicalName(),
                        PageRequest.of(
                                pageNav.getPage() - 1,
                                pageNav.getMaxCountPerPage(),
                                Sort.by(sort, sorted)));
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

        if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
            Long count = getRefRepo()
                    .count();
            return count.intValue();
        } else {
            Long count = getRefSearchRepo()
                    .count(
                            searchTermProvider.getSearchTerm());
            return count.intValue();
        }
    }

    public DataProvider<R, Void> getAllReferenceData(
            Class<R> referenceClass,
            SearchTermProvider searchTermProvider,
            PageNav pageNav) {
        // build data provider
        var dp = new AbstractBackEndDataProvider<R, Void>() {
            @Override
            protected Stream<R> fetchFromBackEnd(Query<R, Void> query) {
                // Sort.Direction sort = pageNav.getAsc() ? Sort.Direction.ASC :
                // Sort.Direction.DESC;
                // String sorted = pageNav.getSortField();
                QuerySortOrder so = query.getSortOrders().isEmpty() ? null : query.getSortOrders().get(0);
                Sort.Direction sort = so == null ? Sort.Direction.DESC
                        : (so.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC);
                String sorted = so == null ? "id" : so.getSorted();

                query.getPage();
                if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
                    Page<R> finapps = getRefRepo().findAll(
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, sorted)));

                    return finapps.stream();
                } else {
                    Page<R> finapps = getRefSearchRepo().find(
                            searchTermProvider.getSearchTerm(),
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, modifySortFieldForSearch(sorted, referenceClass))));
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

    private String modifySortFieldForSearch(String sortField, Class<R> workItemClass) {
        Field field = WorkflowUtils.getField(workItemClass, sortField);
        // if (String.class.equals(field.getType())){
        // return sortField+".keyword";
        // }else if (Status.class.equals(field.getType())){
        // return sortField+".keyword";
        // }else if (Priority.class.equals(field.getType())){
        // return sortField+".keyword";
        if (Number.class.isAssignableFrom(field.getType()) ||
                LocalDateTime.class.isAssignableFrom(field.getType()) ||
                LocalDate.class.isAssignableFrom(field.getType()) ||
                Date.class.isAssignableFrom(field.getType())) {
            return sortField;
        } else {

            return sortField + ".keyword";
        }
    }

    @Autowired
    public void setRefMapRepo(ReferenceMapRepository refMapRepo) {
        this.refMapRepo = refMapRepo;
    }
}