/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.domain;

/**
 *
 * @author azrul
 */
public enum ApplicantType {
    DIRECTOR("Director"),
    SHAREHOLDER("Shareholder"),
    OWNER("Owner"),
    OTHER("Other");
    
    private String text;
    
    ApplicantType(String text){
        this.text=text;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }
    
}
