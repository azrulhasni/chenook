/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.BadgeUtils;
import com.azrul.chenook.service.BizUserService;
import com.azrul.chenook.service.MapperService;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.Card;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.StartEvent;
import com.azrul.smefinancing.domain.FinApplication;
import com.azrul.smefinancing.service.FinApplicationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.function.TriConsumer;
import org.springframework.data.util.Pair;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class WorklistPanel<T extends WorkItem> extends VerticalLayout {

    private final int COUNT_PER_PAGE = 3;
    private final List<Pair<Grid<T>, PageNav>> myWorklists = new ArrayList<>();
    private final WorkflowService<T> workflowService;
    private final OidcUser oidcUser;
    private final BadgeUtils badgeUtils;
    private final MapperService basicMapper;

    public WorklistPanel(
            final Class workItemClass,
            final OidcUser oidcUser,
            final BizProcess bizProcess,
            final WorkflowService<T> workflowService,
            final BizUserService bizUserService,
            final BadgeUtils badgeUtils,
            final MapperService basicMapper,
            final TriConsumer<WorklistPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder
    ) {

        this.oidcUser = oidcUser;
        this.workflowService = workflowService;
        this.badgeUtils = badgeUtils;
        this.basicMapper=basicMapper;
        Map<String,String> sortableFields = WorkflowUtils.getSortableFields(workItemClass);
            
        this.setWidth("-webkit-fill-available");

        Set<String> roles = oidcUser
                .getAuthorities()
                .stream()
                .map(o -> o.getAuthority())
                .filter(a->a.startsWith("ROLE"))
                .map(a -> a.replace("ROLE_", ""))
                .collect(Collectors.toSet());
        
        Map<String,String> worklists = workflowService.findWorklistsByRoles(roles, bizProcess);

        for (Map.Entry<String,String> worklist : worklists.entrySet()) {
            Pair<Grid<T>, PageNav> panel = buildDataPanel(
                    "Worklist:" + worklist.getValue(),
                    worklist.getKey(),
                    oidcUser,
                    bizProcess,
                    bizUserService,
                    showUpdateDialog,
                    cardBuilder,
                    sortableFields,
                    (w) -> workflowService.countWorkByWorklist(w),
                    (w, nav) -> workflowService.getWorkByWorklist(workItemClass,w, nav)
            );
            myWorklists.add(panel);
            addPair(panel);
        }

    }

    private void addPair(Pair<Grid<T>, PageNav> pair) {
        this.add(pair.getSecond());
        this.add(pair.getFirst());
    }

    private Pair<Grid<T>, PageNav> buildDataPanel(
            final String title,
            final String w,
            final OidcUser oidcUser1,
            final BizProcess bizProcess,
            final BizUserService bizUserService,
            final TriConsumer<WorklistPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder,
            final Map<String, String> sortableFields1,
            final Function<String, Integer> counter,
            final BiFunction<String, PageNav, DataProvider> dataProviderCreator) {
        PageNav nav = new PageNav();
        Integer count = counter.apply(w);//finappService1.countWorkByCreator(oidcUser1.getPreferredUsername());
        DataProvider dataProvider = dataProviderCreator.apply(w, nav);//finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(), nav);
        Grid<T> grid = createGrid(title, oidcUser1, bizProcess, bizUserService, dataProvider, showUpdateDialog, cardBuilder);
        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields1, false);
        Pair<Grid<T>, PageNav> pair = Pair.of(grid, nav);
        return pair;
    }

    public void refresh() {
        for (var pair : myWorklists) {
            pair.getFirst().getDataProvider().refreshAll();
            Integer countWorkByCreator = workflowService.countWorkByCreator(oidcUser.getPreferredUsername());
            pair.getSecond().refresh(countWorkByCreator);
        }
    }

    private Grid<T> createGrid(
            final String panelTitle,
            final OidcUser oidcUser,
            final BizProcess bizProcess,
            final BizUserService bizUserService,
            final DataProvider dataProvider,
            final TriConsumer<WorklistPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder) {
        Grid<T> grid = new Grid<>();
        H3 title = new H3(panelTitle);
        this.add(title);
        grid.getStyle().set("max-width", "285px");
        grid.setAllRowsVisible(true);

        this.add(grid);
        grid.addComponentColumn(work
                -> {
            VerticalLayout content = cardBuilder.apply(work);
            Span badge = badgeUtils.createStatusBadge(work.getStatus());
            Card card = new Card(work.getTitle(), badge);
            card.add(content);
            HorizontalLayout btnPanel = new HorizontalLayout();
            Button btnBookThis = new Button("Book this work", e -> {
                work.getOwners().add(basicMapper.map(oidcUser));
                T w = workflowService.save(work);
                showUpdateDialog.accept(this, null, w);
            });
            btnBookThis.setId("btnBook"+work.getId());
            btnPanel.add(btnBookThis);
            card.add(btnPanel);
            return card;
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.setItems(dataProvider);
        return grid;
    }

}
