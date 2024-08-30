/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.service;

import com.azrul.chenook.domain.Status;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import org.springframework.stereotype.Service;
import org.vaadin.addons.badge.Badge;

/**
 *
 * @author azrul
 */
@Service
public class BadgeUtils{
     public Span createStatusBadge(Status status){
        Span badge = new Span(status.getHumanReadableText());
        badge.getElement().getThemeList().add("badge primary");
        badge.getStyle().set("background-color", status.getBackgroundColor());
        badge.getStyle().set("color", status.getColor());
        return badge;
    }
     
    public ComponentRenderer<Span,Status> createStatusBadgeRenderer(){
        ComponentRenderer<Span,Status> renderer = new ComponentRenderer<Span,Status>(st->{
            return createStatusBadge(st);
         });
        return renderer;
        
    }
}
