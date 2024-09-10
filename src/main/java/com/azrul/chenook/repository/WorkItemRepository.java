/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.repository;

import com.azrul.chenook.domain.Signature;
import com.azrul.chenook.domain.WorkItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 *
 * @author azrul
 */
@NoRepositoryBean
public interface WorkItemRepository<T extends WorkItem> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    public T findOneByParentIdAndContext(Long parentId,String context);
}
