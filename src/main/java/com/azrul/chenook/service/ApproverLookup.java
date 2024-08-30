/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

import com.azrul.chenook.domain.BizUser;
import java.util.Optional;

/**
 *
 * @author azrul
 */
public interface ApproverLookup<T> {
    Optional<BizUser> lookupApprover(T work, String username);
}

