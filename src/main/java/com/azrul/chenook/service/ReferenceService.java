package com.azrul.chenook.service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.azrul.chenook.domain.Reference;
import com.azrul.chenook.domain.ReferenceStatus;
import com.azrul.chenook.domain.ReferenceWork;
import com.azrul.chenook.domain.Status;
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
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.order.AuditOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

//import jakarta.transaction.Transactional;
public abstract class ReferenceService<R extends Reference> {

    protected abstract ReferenceRepository<R> getRefRepo();

    protected abstract ReferenceSearchRepository<R> getRefSearchRepo();

    public abstract R copy(R ref, ReferenceStatus newStatus, Long replacementOf);

    //setter injection
    private EntityManagerFactory emFactory;
    
     //setter injection
    private EntityManager entityManager;

    @Transactional
    public R save(R entity) {
        R e = getRefRepo().save(entity);
        getRefSearchRepo().save(entity);
        return e;
    }

    @Transactional
    public Set<R> saveAll(Set<R> entities) {
        List<R> refs = getRefRepo().saveAll(entities);
        getRefSearchRepo().saveAll(entities);
        return refs.stream().collect(Collectors.toSet());
    }

    @Transactional
    public void remove(R entity) {
        getRefRepo().delete(entity);
        getRefSearchRepo().delete(entity);
    }

    @Transactional
    public void retire(Set<R> entities) {
        for (R r : entities) {
            r.setStatus(ReferenceStatus.RETIRED);
        }
        getRefRepo().saveAll(entities);
        getRefSearchRepo().saveAll(entities);
    }

    @Transactional
    public void deprecate(Set<R> entities) {
        for (R r : entities) {
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

    public void updateRefStatusByRefWork(ReferenceStatus targetStatus, ReferenceStatus conditionStatus, Long refWorkId) {
        this.getRefRepo().updateRefStatsByRefWork(targetStatus, conditionStatus, refWorkId);
        Set<R> refs = this.getRefSearchRepo().findByRefWork(refWorkId);
        for (R r : refs) {
            if (r.getStatus().equals(conditionStatus)) {
                r.setStatus(targetStatus);
            }
        }
        this.getRefSearchRepo().saveAll(refs);
    }

    public void updateRefStatusByRefId(ReferenceStatus targetStatus, ReferenceStatus conditionStatus, Long refId) {
        this.getRefRepo().updateRefStatsById(targetStatus, conditionStatus, refId);
        Optional<R> or = this.getRefSearchRepo().findById(refId);
        or.ifPresent(r -> {
            if (r.getStatus().equals(conditionStatus)) {
                r.setStatus(targetStatus);
            }
            this.getRefSearchRepo().save(r);
        });
    }

    public void updateRefStatusByRefId(ReferenceStatus targetStatus, ReferenceStatus conditionStatus, Long refId, Long replacementOf) {
        this.getRefRepo().updateRefStatsById(targetStatus, conditionStatus, refId, replacementOf);
        Optional<R> or = this.getRefSearchRepo().findById(refId);
        or.ifPresent(r -> {
            if (r.getStatus().equals(conditionStatus)) {
                r.setStatus(targetStatus);
                r.setReplacementOf(null);
            }
            this.getRefSearchRepo().save(r);
        });
    }

    public void removeByRefId(Long refId) {
        this.getRefRepo().removeById(refId);
        this.getRefSearchRepo().deleteById(refId);
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

    public DataProvider<R, Void> getConfirmedReferenceData(
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
                String sorted = StringUtils.isEmpty(pageNav.getSortField()) ? "id" : pageNav.getSortField();

                query.getPage();
                if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
                    Page<R> finapps = getRefRepo().findAll(
                            whereReferenceStatusIsConfirmed(),
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, sorted)));

                    return finapps.stream();
                } else {
                    Page<R> finapps = getRefSearchRepo().findConfirmed(
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

    public Integer countConfirmedReferenceData(
            Class<R> referenceClass,
            SearchTermProvider searchTermProvider
    ) {

        if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
            Long count = getRefRepo()
                    .count(whereReferenceStatusIsConfirmed());
            return count.intValue();
        } else {
            Long count = getRefSearchRepo()
                    .countConfirmed(
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
                String sorted = StringUtils.isEmpty(pageNav.getSortField()) ? "id" : pageNav.getSortField();

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
                            whereRefWorkEqualsAndReferenceStatusIs(refWorkId, ReferenceStatus.DRAFT),
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
                    .count(whereRefWorkEqualsAndReferenceStatusIs(refWorkId, ReferenceStatus.DRAFT));
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
                            whereRefWorkEqualsAndReferenceStatusIs(refWorkId, ReferenceStatus.DEPRECATED),
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

    public DataProvider<R, Void> getUpdateCandidateReferenceData(
            Class<R> referenceClass,
            ReferenceWork refWork,
            SearchTermProvider searchTermProvider,
            PageNav pageNav) {
        var dp = new AbstractBackEndDataProvider<R, Void>() {
            @Override
            protected Stream<R> fetchFromBackEnd(Query<R, Void> query) {
                QuerySortOrder so = query.getSortOrders().isEmpty() ? null : query.getSortOrders().get(0);
                Sort.Direction sort = so == null ? Sort.Direction.DESC
                        : (so.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC);
                String sorted = so == null ? "id" : so.getSorted();

                query.getPage();
//                if (Status.DONE.equals(refWork.getStatus())) {
//                    if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
//                        Page<R> finapps = getRefRepo().findAll(
//                                whereRefWorkEqualsAndReferenceStatusIs(refWork.getId(), ReferenceStatus.CONFIRMED),
//                                PageRequest.of(
//                                        pageNav.getPage() - 1,
//                                        pageNav.getMaxCountPerPage(),
//                                        Sort.by(sort, sorted)));
//
//                        return finapps.stream();
//                    } else {
//                        Page<R> finapps = getRefSearchRepo().findConfirmed(searchTermProvider.getSearchTerm(),
//                                refWork.getId(),
//                                PageRequest.of(
//                                        pageNav.getPage() - 1,
//                                        pageNav.getMaxCountPerPage(),
//                                        Sort.by(sort, modifySortFieldForSearch(sorted, referenceClass))));
//                        return finapps.stream();
//                    }
//                } else {
                if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
                    Page<R> finapps = getRefRepo().findAll(
                            whereRefWorkEqualsAndReferenceStatusIs(refWork.getId(), ReferenceStatus.DRAFT),
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, sorted)));

                    return finapps.stream();
                } else {
                    Page<R> finapps = getRefSearchRepo().findDraft(searchTermProvider.getSearchTerm(),
                            refWork.getId(),
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, modifySortFieldForSearch(sorted, referenceClass))));
                    return finapps.stream();
                }
//                }
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

    public Integer countUpdateCandidateReferenceData(Class<R> referenceClass,
            ReferenceWork refWork,
            SearchTermProvider searchTermProvider) {
//        if (Status.DONE.equals(refWork.getStatus())) {
//             if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
//                Long count = getRefRepo().count(
//                        whereRefWorkEqualsAndReferenceStatusIs(refWork.getId(), ReferenceStatus.CONFIRMED));
//                if (count != null) {
//                    return count.intValue();
//                } else {
//                    return 0;
//                }
//            } else {
//                Long count = getRefSearchRepo().countConfirmed(searchTermProvider.getSearchTerm(),
//                        refWork.getId());
//                if (count != null) {
//                    return count.intValue();
//                } else {
//                    return 0;
//                }
//            }
//        } else {
        if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
            Long count = getRefRepo().count(
                    whereRefWorkEqualsAndReferenceStatusIs(refWork.getId(), ReferenceStatus.DRAFT));
            if (count != null) {
                return count.intValue();
            } else {
                return 0;
            }
        } else {
            Long count = getRefSearchRepo().countDraft(searchTermProvider.getSearchTerm(),
                    refWork.getId());
            if (count != null) {
                return count.intValue();
            } else {
                return 0;
            }
        }
//        }
    }

    private String modifySortFieldForSearch(String sortField, Class<R> workItemClass) {
        Field field = WorkflowUtils.getField(workItemClass, sortField);
        if (Number.class.isAssignableFrom(field.getType())
                || LocalDateTime.class.isAssignableFrom(field.getType())
                || LocalDate.class.isAssignableFrom(field.getType())
                || Date.class.isAssignableFrom(field.getType())) {
            return sortField;
        } else {

            return sortField + ".keyword";
        }
    }

    private Specification<R> whereReferenceStatusIsActive() {
        return (ref, cq, cb) -> {
            return cb.or(
                    cb.equal(ref.get("status"), ReferenceStatus.CONFIRMED),
                    cb.equal(ref.get("status"), ReferenceStatus.DEPRECATED)
            );
        };
    }

    private Specification<R> whereReferenceStatusIsConfirmed() {
        return (ref, cq, cb)
                -> cb.equal(ref.get("status"), ReferenceStatus.CONFIRMED);

    }

    private Specification<R> whereRefWorkEqualsAndReferenceStatusIs(Long refWork, ReferenceStatus refStatus) {
        return (ref, cq, cb) -> {
            return cb.and(
                    cb.equal(ref.get("status"), refStatus),
                    cb.equal(ref.get("refWorkId"), refWork)
            );
        };
    }

    private Specification<R> whereReferencesAreUpdateCandidates(
            Long refWork,
            Long replacementOf,
            ReferenceStatus refStatus) {
        return (ref, cq, cb) -> {
            return cb.and(
                    cb.equal(ref.get("status"), refStatus),
                    cb.equal(ref.get("replacementOf"), replacementOf),
                    cb.equal(ref.get("refWorkId"), refWork)
            );
        };
    }

    private Specification<R> whereRefWorkEquals(Long refWork) {
        return (ref, cq, cb) -> {
            return cb.and(
                    cb.equal(ref.get("refWorkId"), refWork)
            );
        };
    }

    /**
     * @return the emFactory
     */
    public EntityManagerFactory getEmFactory() {
        return emFactory;
    }

    /**
     * @param emFactory the emFactory to set
     */
    @Autowired
    public void setEmFactory(EntityManagerFactory emFactory) {
        this.emFactory = emFactory;
    }
    
    /**
     * @return the entityManager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * @param entityManager the entityManager to set
     */
    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Transactional
    public DataProvider<R, Void> getRevisionsByRefWork(
            Long refWorkId,
            Class<R> refClass,
            PageNav pageNav
    ) {
        var dp = new AbstractBackEndDataProvider<R, Void>() {
            @Override
            protected Stream<R> fetchFromBackEnd(Query<R, Void> query) {
                query.getPage();
                List<Object[]> auditData= AuditReaderFactory.get(entityManager)
                        .createQuery()
                        .forRevisionsOfEntityWithChanges(refClass, true)
                        .add(AuditEntity.property("refWorkId").eq(refWorkId))
                        .setFirstResult((pageNav.getPage() - 1) * pageNav.getCountPerPage())
                        .setMaxResults(pageNav.getCountPerPage())
                        .addOrder(pageNav.getAsc() == true ? AuditEntity.property(pageNav.getSortField()).asc() : AuditEntity.property(pageNav.getSortField()).desc())
                        .getResultList();
                List<R> res = new ArrayList<>();
                for (Object[] o:auditData){
                    R r = (R) o[0];
                    r.setOperation(((RevisionType)o[2]).toString());
                    res.add(r);
                }
                return res.stream();
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

    @Transactional
    public Integer countRevisionsByRefWork(
            Long refWorkId,
            Class<R> refClass
    ) {
        Long res = (Long)  AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntityWithChanges(refClass, true)
                .add(AuditEntity.property("refWorkId").eq(refWorkId))
                .addProjection(AuditEntity.id().count())
                .getSingleResult();
        return res.intValue();

    }

    

}
