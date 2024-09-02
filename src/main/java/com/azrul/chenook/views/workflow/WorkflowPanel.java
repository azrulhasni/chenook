/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.domain.Attachment;
import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.Priority;
import com.azrul.chenook.domain.Status;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.domain.WorkflowInfo;
import com.azrul.chenook.service.AttachmentService;
import com.azrul.chenook.service.BizUserService;
import com.azrul.chenook.service.WorkItemService;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.chenook.value.WorkflowMemento;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.smefinancing.service.BadgeUtils;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class WorkflowPanel<T> extends VerticalLayout {
    
    //private Long parentId;
    private final WorkflowMemento memento;
    

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private WorkItemService workItemService;

    @Autowired
    private BadgeUtils badgeUtils;
    
    @Autowired
    private BizUserService bizUserService;

    private static final String STATUS_LABEL = "Status";

    public WorkflowPanel(
            final WorkflowMemento memento, 
            final Boolean editable,
            final Consumer<Attachment> onPostSave,
            final Consumer<Attachment> onPostRemove
    ) {
        ApplicationContextHolder.autowireBean(this);
        this.memento=memento;
        
        WorkItem work = workItemService.findOneByParentIdAndContext(
                memento.getParentId(), 
                memento.getContext());
        if (work != null) {
            if (work.getWorkflowInfo().getOwners().contains(memento.getOidcUser().getPreferredUsername())) {
                Select<Status> cbStatus = createSelect(STATUS_LABEL, editable);
                cbStatus.setItems(Status.values());
                cbStatus.setRenderer(badgeUtils.createStatusBadgeRenderer());
                cbStatus.setValue(work.getStatus());
                this.add(cbStatus);
            } else {
                //show nothing
            }
        } else {
            WorkItem newwork = new WorkItem();
            newwork.setContext(memento.getContext());
            newwork.setCreator(memento.getOidcUser().getPreferredUsername());
            newwork.setParentId(memento.getParentId());
            newwork.setPriority(Priority.NONE);
            newwork.setStatus(Status.NEWLY_CREATED);
            WorkflowInfo wfInfo = new WorkflowInfo();
            Set<String> owners = new HashSet<>();
            owners.add(memento.getOidcUser().getPreferredUsername());
            wfInfo.setOwners(owners);
            wfInfo.setStartEventId(memento.getBizProcess().getStartEvents().iterator().next().getId());
            wfInfo.setStartEventDescription(memento.getBizProcess().getStartEvents().iterator().next().getDescription());
            wfInfo.setWorklist(memento.getBizProcess().getStartEvents().iterator().next().getId());
            wfInfo.setWorklistUpdateTime(LocalDateTime.now());
            newwork.setWorkflowInfo(wfInfo);
            newwork = workItemService.save(newwork);
            Select<Status> cbStatus = createSelect(STATUS_LABEL, editable);
            cbStatus.setItems(Status.values());
            cbStatus.setRenderer(badgeUtils.createStatusBadgeRenderer());
            cbStatus.setValue(newwork.getStatus());
            this.add(cbStatus);
        }
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
