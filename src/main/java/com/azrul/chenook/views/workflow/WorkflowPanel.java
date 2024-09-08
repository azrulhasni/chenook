/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.domain.Attachment;
import com.azrul.chenook.domain.Status;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.BizUserService;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.smefinancing.service.BadgeUtils;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author azrul
 */
public class WorkflowPanel<T> extends VerticalLayout {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private BadgeUtils badgeUtils;

    @Autowired
    private BizUserService bizUserService;

    private static final String STATUS_LABEL = "Status";

    public WorkflowPanel(
            final WorkItem work,
            final Boolean editable,
            final Consumer<Attachment> onPostSave,
            final Consumer<Attachment> onPostRemove
    ) {
        ApplicationContextHolder.autowireBean(this);

        Select<Status> cbStatus = createSelect(STATUS_LABEL, editable);
        cbStatus.setItems(Status.values());
        cbStatus.setRenderer(badgeUtils.createStatusBadgeRenderer());
        if (work!=null){
            cbStatus.setValue(work.getStatus());
        }else{
            cbStatus.setValue(Status.NEWLY_CREATED);
        }
        this.add(cbStatus);

        //this.parentId = parentId;
    }

    private <T> Select<T> createSelect(
            final String label,
            final boolean editable
    ) {
        Select<T> select = new Select<>();
        select.setLabel(label);
        select.setReadOnly(!editable);
        return select;
    }

//    public void moveWork(){
//         WorkItem work = workItemService.findOneByParentIdAndContext(parentId, context);
//         BizUser bizUser = bizUserService.getUser(oidcUser.getPreferredUsername());
//         workflowService.run(parent, work, bizUser, false, bizProcess);
//    }
}
