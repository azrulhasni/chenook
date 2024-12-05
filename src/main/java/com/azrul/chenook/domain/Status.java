/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;


import java.util.HashMap;
import java.util.Map;

public enum Status {
    DRAFT("#FFFFFF", "#808080", "Draft"),               
    NEWLY_CREATED("#FFFFFF", "#0000FF", "Newly Created"),
    IN_PROGRESS("#000000", "#FFFF00", "In Progress"),   
    IN_REVIEW("#000000", "#FFA500", "In Review"),       
    NEED_MORE_INFO("#FFFFFF", "#FF4500", "Need More Info"),
    DONE("#FFFFFF", "#008000", "Disbursed"),      
    DENIED("#FFFFFF", "#FF0000", "Denied"),             
    CANCELLED("#FFFFFF", "#A52A2A", "Cancelled");       

    private final String color;
    private final String backgroundColor;
    private final String humanReadableText;

    Status(String color, String backgroundColor, String humanReadableText) {
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