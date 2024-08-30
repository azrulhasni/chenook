/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

import com.azrul.chenook.domain.Approval;
import com.azrul.chenook.repository.ApprovalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author azrul
 */
@Service
public class ApprovalService {
    private final ApprovalRepository approvalRepo;
    
    
    public ApprovalService(
        @Autowired ApprovalRepository approvalRepo    
    ){
        this.approvalRepo=approvalRepo;
    }
    
    public Approval save(Approval approval){
       return this.approvalRepo.save(approval);
    }
}
