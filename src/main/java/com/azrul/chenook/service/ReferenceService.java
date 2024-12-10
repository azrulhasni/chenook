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
import com.azrul.chenook.domain.ReferenceStatus;
//import com.azrul.chenook.domain.ReferenceMap;
//import com.azrul.chenook.repository.ReferenceMapRepository;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

//import jakarta.transaction.Transactional;

public abstract class ReferenceService<R extends Reference> {

    protected abstract ReferenceRepository<R> getRefRepo();

    protected abstract ReferenceSearchRepository<R> getRefSearchRepo();

    @Transactional
    public void save(R entity) {
        getRefRepo().save(entity);
        getRefSearchRepo().save(entity);
    }
    
    @Transactional
    public void remove(R entity) {
        getRefRepo().delete(entity);
        getRefSearchRepo().delete(entity);
    }
    
    @Transactional
     public void retire(Set<R> entities) {
        for (R r:entities){
            r.setStatus(ReferenceStatus.RETIRED);
        }
        getRefRepo().saveAll(entities);
        getRefSearchRepo().saveAll(entities);
    }
     
    @Transactional
     public void deprecate(Set<R> entities) {
        for (R r:entities){
            r.setStatus(ReferenceStatus.DEPRECATED);
        }
        getRefRepo().saveAll(entities);
        getRefSearchRepo().saveAll(entities);
    }
    
      public Integer countReferenceData(
            Class<R> referenceClass,
            Long refWorkId,
            SearchTermProvider searchTermProvider

    ) {

        if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
            Long count = getRefRepo()
                    .count(whereRefWorkEquals(refWorkId));
            return count.intValue();
        } else {
            Long count = getRefSearchRepo()
                    .count(
                            searchTermProvider.getSearchTerm(),
                            refWorkId
                    );
            return count.intValue();
        }
    }
      
     public DataProvider<R, Void> getReferenceData(
            Class<R> referenceClass,
            Long refWorkId,
            SearchTermProvider searchTermProvider,
            PageNav pageNav) {
        // build data provider
        var dp = new AbstractBackEndDataProvider<R, Void>() {
            @Override
            protected Stream<R> fetchFromBackEnd(Query<R, Void> query) {
                QuerySortOrder so = query.getSortOrders().isEmpty() ? null : query.getSortOrders().get(0);
                Sort.Direction sort = so == null ? Sort.Direction.DESC
                        : (so.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC);
                String sorted = so == null ? "id" : so.getSorted();

                query.getPage();
                if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
                    Page<R> finapps = getRefRepo().findAll(
                            whereRefWorkEquals(refWorkId),
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, sorted)));

                    return finapps.stream();
                } else {
                    Page<R> finapps = getRefSearchRepo().find(
                            searchTermProvider.getSearchTerm(),
                            refWorkId,
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

    
    
    public Integer countActiveReferenceData(
            Class<R> referenceClass,
            SearchTermProvider searchTermProvider

    ) {

        if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
            Long count = getRefRepo()
                    .count(whereReferenceStatusIsActive());
            return count.intValue();
        } else {
            Long count = getRefSearchRepo()
                    .countActive(
                            searchTermProvider.getSearchTerm()
                    );
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
                    Page<R> finapps = getRefSearchRepo().findAll(
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
                    .countActive(
                            searchTermProvider.getSearchTerm()
                    );
            return count.intValue();
        }
    }

    public DataProvider<R, Void> getActiveReferenceData(
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
               Sort.Direction sort = pageNav.getAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
                String sorted = pageNav.getSortField();

                query.getPage();
                if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
                    Page<R> finapps = getRefRepo().findAll(
                            whereReferenceStatusIsActive(),
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, sorted)));

                    return finapps.stream();
                } else {
                    Page<R> finapps = getRefSearchRepo().findActive(
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
    
    public DataProvider<R, Void> getDraftReferenceData(
            Class<R> referenceClass,
            Long refWorkId,
            SearchTermProvider searchTermProvider,
            PageNav pageNav) {
        // build data provider
        var dp = new AbstractBackEndDataProvider<R, Void>() {
            @Override
            protected Stream<R> fetchFromBackEnd(Query<R, Void> query) {
                QuerySortOrder so = query.getSortOrders().isEmpty() ? null : query.getSortOrders().get(0);
                Sort.Direction sort = so == null ? Sort.Direction.DESC
                        : (so.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC);
                String sorted = so == null ? "id" : so.getSorted();

                query.getPage();
                if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
                    Page<R> finapps = getRefRepo().findAll(
                            whereRefWorkEqualsAndReferenceStatusIs(refWorkId,ReferenceStatus.DRAFT),
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, sorted)));

                    return finapps.stream();
                } else {
                    Page<R> finapps = getRefSearchRepo().findDraft(
                            searchTermProvider.getSearchTerm(),
                            refWorkId,
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
    
     public Integer countDraftReferenceData(
            Class<R> referenceClass,
            Long refWorkId,
            SearchTermProvider searchTermProvider

    ) {

        if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
            Long count = getRefRepo()
                    .count(whereRefWorkEqualsAndReferenceStatusIs(refWorkId,ReferenceStatus.DRAFT));
            return count.intValue();
        } else {
            Long count = getRefSearchRepo()
                    .countDraft(
                            searchTermProvider.getSearchTerm(),
                            refWorkId
                    );
            return count.intValue();
        }
    }
     
     public DataProvider<R, Void> getDeprecatedReferenceData(
            Class<R> referenceClass,
            Long refWorkId,
            SearchTermProvider searchTermProvider,
            PageNav pageNav) {
        // build data provider
        var dp = new AbstractBackEndDataProvider<R, Void>() {
            @Override
            protected Stream<R> fetchFromBackEnd(Query<R, Void> query) {
                QuerySortOrder so = query.getSortOrders().isEmpty() ? null : query.getSortOrders().get(0);
                Sort.Direction sort = so == null ? Sort.Direction.DESC
                        : (so.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC);
                String sorted = so == null ? "id" : so.getSorted();

                query.getPage();
                if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
                    Page<R> finapps = getRefRepo().findAll(
                            whereRefWorkEqualsAndReferenceStatusIs(refWorkId,ReferenceStatus.DEPRECATED),
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, sorted)));

                    return finapps.stream();
                } else {
                    Page<R> finapps = getRefSearchRepo().findDeprecated(
                            searchTermProvider.getSearchTerm(),
                            refWorkId,
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
    
     public Integer countDeprecatedReferenceData(
            Class<R> referenceClass,
            Long refWorkId,
            SearchTermProvider searchTermProvider

    ) {

        if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
            Long count = getRefRepo()
                    .count(whereRefWorkEqualsAndReferenceStatusIs(refWorkId, ReferenceStatus.DEPRECATED));
            return count.intValue();
        } else {
            Long count = getRefSearchRepo()
                    .countDeprecated(
                            searchTermProvider.getSearchTerm(),
                            refWorkId
                    );
            return count.intValue();
        }
    }

    private String modifySortFieldForSearch(String sortField, Class<R> workItemClass) {
        Field field = WorkflowUtils.getField(workItemClass, sortField);
        if (Number.class.isAssignableFrom(field.getType()) ||
                LocalDateTime.class.isAssignableFrom(field.getType()) ||
                LocalDate.class.isAssignableFrom(field.getType()) ||
                Date.class.isAssignableFrom(field.getType())) {
            return sortField;
        } else {

            return sortField + ".keyword";
        }
    }
    
    private Specification<R> whereReferenceStatusIsActive(){
        return (ref, cq, cb) -> {
            return cb.or(
                    cb.equal(ref.get("status"), ReferenceStatus.CONFIRMED),
                    cb.equal(ref.get("status"), ReferenceStatus.DEPRECATED)
                    );
        };
    }
    
    private Specification<R> whereRefWorkEqualsAndReferenceStatusIs(Long refWork, ReferenceStatus refStatus){
        return (ref, cq, cb) -> {
            return cb.and(
                    cb.equal(ref.get("status"), refStatus),
                    cb.equal(ref.get("refWorkId"), refWork)
            );
        };
    }
    
    private Specification<R> whereRefWorkEquals(Long refWork){
        return (ref, cq, cb) -> {
            return cb.and(
                    cb.equal(ref.get("refWorkId"), refWork)
            );
        };
    }

   
}