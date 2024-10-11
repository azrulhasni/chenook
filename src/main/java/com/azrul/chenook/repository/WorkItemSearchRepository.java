/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.repository;

import com.azrul.chenook.domain.WorkItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author azrul
 */
@Repository
public interface WorkItemSearchRepository  extends ElasticsearchRepository<WorkItem, Long> {
   
}
