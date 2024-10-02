/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.binder.Binder;
import java.util.function.Consumer;

/**
 *
 * @author azrul
 */
public class WorkflowAwareButton extends Button {
    private WorkflowAwareGroup group;
    
    private WorkflowAwareButton(WorkflowAwareGroup group) {
        this.group = group;
    }
    
     public void applyGroup(){
        if (group!=null){
            this.setEnabled(group.calculateEnable());
            this.setVisible(group.calculateVisible());
        }
    }
    
    public static <T, C> WorkflowAwareButton create(WorkflowAwareGroup group) {
         WorkflowAwareButton button = new WorkflowAwareButton(group);
         button.applyGroup();
         return button;
     }
     
     public static <T, C> WorkflowAwareButton create() {
         WorkflowAwareButton button = new WorkflowAwareButton(null);
         return button;
     }
}
