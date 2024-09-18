/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common;

import com.azrul.chenook.domain.WorkItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author azrul
 */
public class WorkflowAwarePanel<T> extends VerticalLayout{
    private T workItem;
    private Predicate visibleCondition;
    private Predicate editableCondition;
    
    public WorkflowAwarePanel(T work,Predicate visibleCondition,Predicate editableCondition){
        this.workItem = work;
        this.visibleCondition = visibleCondition;
        this.editableCondition=editableCondition;
        
    }
    
    public void refresh(){
        setVisible(visibleCondition.test(workItem));
        setEnabled(editableCondition.test(workItem));
    }

    /**
     * @return the workItem
     */
    public T getWorkItem() {
        return workItem;
    }

    /**
     * @param workItem the workItem to set
     */
    public void setWorkItem(T workItem) {
        this.workItem = workItem;
    }

    /**
     * @return the visibleCondition
     */
    public Predicate getVisibleCondition() {
        return visibleCondition;
    }

    /**
     * @param visibleCondition the visibleCondition to set
     */
    public void setVisibleCondition(Predicate visibleCondition) {
        this.visibleCondition = visibleCondition;
    }

    /**
     * @return the editableCondition
     */
    public Predicate getEditableCondition() {
        return editableCondition;
    }

    /**
     * @param editableCondition the editableCondition to set
     */
    public void setEditableCondition(Predicate editableCondition) {
        this.editableCondition = editableCondition;
    }
    
}
