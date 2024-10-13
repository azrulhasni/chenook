/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.integration;

import com.azrul.chenook.domain.BizUser;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;

/**
 *
 * @author azrul
 */
public class ExperianBureauLogic {

    public Integer run(BizUser user,  Object oCurrentObject) {
       Random rand = new Random();
       return rand.nextInt(1000)+500;
        
    }
}
