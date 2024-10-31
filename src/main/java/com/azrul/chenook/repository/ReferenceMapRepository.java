package com.azrul.chenook.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.azrul.chenook.domain.Reference;
import com.azrul.chenook.domain.ReferenceMap;

public interface ReferenceMapRepository<R extends Reference> extends JpaRepository<ReferenceMap<R>, Long>, JpaSpecificationExecutor<ReferenceMap> {
    @Query("SELECT r FROM ReferenceMap r WHERE r.type=:type AND :parentId = r.parentId")
    public ReferenceMap<R> findReferencesByParent(Long parentId, String type);

    @Query("SELECT refs FROM ReferenceMap r left join r.references refs WHERE r.type=:type AND :parentId = r.parentId")
    public Page<R> findReferencesByParent(Long parentId, String type, Pageable pageable);

    @Query("SELECT count(refs) FROM ReferenceMap r left join r.references refs WHERE r.type=:type AND :parentId = r.parentId")
    public Long countReferencesByParent(Long parentId, String type);
}