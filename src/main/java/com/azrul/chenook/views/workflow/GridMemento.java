/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.common.components.SearchPanel;
import com.vaadin.flow.component.grid.Grid;

/**
 *
 * @author azrulhasnimadisa
 */
public class GridMemento<T> {
    private Class<T> clazz = null;
    private Grid<T> grid = null;
    private PageNav pageNav = null;
    private SearchPanel searchPanel = null;
    
    public static <T> GridMemento<T> build(Class<T> clazz, SearchPanel searchPanel,PageNav pageNav,Grid<T> grid){
        GridMemento<T> memento = new GridMemento<>();
        memento.setClazz(clazz);
        memento.setGrid(grid);
        memento.setPageNav(pageNav);
        memento.setSearchPanel(searchPanel);
        return memento;
    }

    /**
     * @return the grid
     */
    public Grid<T> getGrid() {
        return grid;
    }

    /**
     * @param grid the grid to set
     */
    public void setGrid(Grid<T> grid) {
        this.grid = grid;
    }

    /**
     * @return the pageNav
     */
    public PageNav getPageNav() {
        return pageNav;
    }

    /**
     * @param pageNav the pageNav to set
     */
    public void setPageNav(PageNav pageNav) {
        this.pageNav = pageNav;
    }

    /**
     * @return the searchPanel
     */
    public SearchPanel getSearchPanel() {
        return searchPanel;
    }

    /**
     * @param searchPanel the searchPanel to set
     */
    public void setSearchPanel(SearchPanel searchPanel) {
        this.searchPanel = searchPanel;
    }

    /**
     * @return the clazz
     */
    public Class<T> getClazz() {
        return clazz;
    }

    /**
     * @param clazz the clazz to set
     */
    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }
    
    
}
