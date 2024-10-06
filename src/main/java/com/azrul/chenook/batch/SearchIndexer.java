/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.batch;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 */
@Component
public class SearchIndexer {
     @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void startIndexing() {
         try {
             SearchSession session = Search.session(entityManager);
             session.massIndexer().startAndWait();
         } catch (InterruptedException ex) {
             Logger.getLogger(SearchIndexer.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
}
