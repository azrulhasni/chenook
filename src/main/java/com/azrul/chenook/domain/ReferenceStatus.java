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
    CONFIRMED("#FFFFFF", "#008000", "Confirmed"),
    DEPRECATED("#FFFFFF", "#A52A2A", "Deprecated"),
    RETIRED("#FFFFFF", "#FF0000", "Retired"),
    DRAFT("#FFFFFF", "#808080", "Draft");
    
    private final String color;
    private final String backgroundColor;
    private final String humanReadableText;
    
    ReferenceStatus(String color, String backgroundColor, String humanReadableText) {
        this.color = color;
        this.backgroundColor = backgroundColor;
        this.humanReadableText = humanReadableText;
    }
    
    public String getColor() {
        return color;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getHumanReadableText() {
        return humanReadableText;
    }
    
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
