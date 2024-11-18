/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

/**
 *
 * @author azrul
 */
public enum ReferenceStatus {
    CONFIRMED,
    DEPRECATED,
    RETIRED,
    DRAFT,
    
}
/*
Create
------
        Admin (Maker): create R in DRAFT mode. Link to R to RefWork
        Admin (Checker): review R through RefWork -> move to CONFIRMED
        
Update
------
        Admin (Maker): create R' in DRAFT mode. Put R in DEPRECATED mode. Link R', R to Link to RefWork
        Admin (Checker): review R,R' through RefWork -> move R' to CONFIRMED and R to RETIRED
        
Retire
------
        Admin (Maker) move R to DEPRECATED mode
        Admin (Checker): review R  -> move R to RETIRED
        

*/
