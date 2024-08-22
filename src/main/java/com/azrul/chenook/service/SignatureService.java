/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

import com.azrul.chenook.domain.Signature;
import com.azrul.chenook.repository.SignatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 */
@Service
public class SignatureService {
    private  SignatureRepository signRepo;
    
    public SignatureService(
           @Autowired SignatureRepository signRepo
    ){
        this.signRepo=signRepo;
    }
    
    @Transactional
    public Signature save(Signature sign){
        return signRepo.save(sign);
    }
    
    @Transactional
    public void remove(Signature sign){
        signRepo.delete(sign);
    }
    
    @Transactional
    public Signature findSignatureByParentAndContext(Long parentId, String context){
        return this.signRepo.findOneByParentIdAndContext(
                    parentId,
                    context);
    }
}
