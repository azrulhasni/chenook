/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.search.repository; //Must be outside of @EnableJpaRepositories(basePackages =...). If not Spring Data will expect certain methods

import com.azrul.chenook.search.repository.WorkItemSearchRepository;
import com.azrul.smefinancing.domain.FinApplication;

/**
 *
 * @author azrul
 */

public interface FinApplicationSearchRepository extends WorkItemSearchRepository<FinApplication>{
   
}
