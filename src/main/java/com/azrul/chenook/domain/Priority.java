/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

/**
 *
 * @author azrul
 */
public enum Priority {
    NONE("None"),
    LOWEST("Lowest"),
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    HIGHEST("Highest");
    
    private String value;
    
    Priority(String value){
        this.value=value;
    }

    @Override
    public String toString(){
        return value;
    }
    
    
    
}
