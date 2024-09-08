/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.domain.Attachment;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.smefinancing.domain.FinApplication;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author azrul
 */
public class WorklistPanel extends VerticalLayout{
    @Autowired
    private WorkflowService workflowService;
    private Grid<FinApplication> grid;
    
    public WorklistPanel(
            final String userrname,
            final Consumer<Attachment> onPostSave,
            final Consumer<Attachment> onPostRemove
    ) {
        grid = new Grid<>(FinApplication.class, false);
        grid.getStyle().set("max-width","285px");
        grid.setAllRowsVisible(true);
    }
    
    
}
