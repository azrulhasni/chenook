/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

import com.azrul.chenook.domain.Message;
import com.azrul.chenook.domain.MessageStatus;
import com.azrul.chenook.repository.MessageRepository;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 */
@Service
public class MessageService {
    private final MessageRepository msgRepo;

    
    public MessageService(
            @Autowired MessageRepository msgRepo
    ){
        this.msgRepo=msgRepo;
    }

    @Transactional
    public Message save(Message message){
        return this.msgRepo.save(message);
    }
    
    @Transactional
    public void remove(Message message){
        this.msgRepo.delete(message); 
    }
    
    public Long countMessagesByParentIdStatusAndContext(Long parentId,  MessageStatus status, String context){
        return msgRepo.countByParentIdAndStatusAndContext(parentId,status, context);
    }
    
    public CallbackDataProvider.FetchCallback<Message, Void> findMessagesByParentAndContext(Long parentId, String context) {
        return query -> {
            var vaadinSortOrders = query.getSortOrders();
            var springSortOrders = new ArrayList<Sort.Order>();
            for (QuerySortOrder so : vaadinSortOrders) {
                String colKey = so.getSorted();
                if (so.getDirection() == SortDirection.ASCENDING) {
                    springSortOrders.add(Sort.Order.asc(colKey));
                }
            }
            return this.msgRepo.findByParentIdAndContext(
                    parentId,
                    context,
                    PageRequest.of(
                            query.getPage(),
                            query.getPageSize(),
                            Sort.by(springSortOrders)
                    )).stream();
        };
    }
    
    
}
