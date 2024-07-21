package com.azrul.smefinancing.views.smefinancing;

import com.azrul.smefinancing.components.avataritem.AvatarItem;
import com.azrul.smefinancing.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import java.util.List;

@PageTitle("SME Financing")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class SMEFinancingView extends Composite<VerticalLayout> {

    public SMEFinancingView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        Avatar avatar = new Avatar();
        MenuBar menuBar = new MenuBar();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        MultiSelectListBox avatarItems = new MultiSelectListBox();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        avatar.setName("Firstname Lastname");
        menuBar.setWidth("min-content");
        setMenuBarSampleData(menuBar);
        layoutRow2.addClassName(Gap.MEDIUM);
        layoutRow2.setWidth("100%");
        layoutRow2.getStyle().set("flex-grow", "1");
        layoutColumn2.setWidth("100%");
        layoutColumn2.getStyle().set("flex-grow", "1");
        avatarItems.setWidth("min-content");
        setAvatarItemsSampleData(avatarItems);
        getContent().add(layoutRow);
        layoutRow.add(avatar);
        layoutRow.add(menuBar);
        getContent().add(layoutRow2);
        layoutRow2.add(layoutColumn2);
        layoutColumn2.add(avatarItems);
    }

    private void setMenuBarSampleData(MenuBar menuBar) {
        menuBar.addItem("View");
        menuBar.addItem("Edit");
        menuBar.addItem("Share");
        menuBar.addItem("Move");
    }

    private void setAvatarItemsSampleData(MultiSelectListBox multiSelectListBox) {
        record Person(String name, String profession) {
        }
        ;
        List<Person> data = List.of(new Person("Aria Bailey", "Endocrinologist"), new Person("Aaliyah Butler", "Nephrologist"), new Person("Eleanor Price", "Ophthalmologist"), new Person("Allison Torres", "Allergist"), new Person("Madeline Lewis", "Gastroenterologist"));
        multiSelectListBox.setItems(data);
        multiSelectListBox.setRenderer(new ComponentRenderer(item -> {
            AvatarItem avatarItem = new AvatarItem();
            avatarItem.setHeading(((Person) item).name);
            avatarItem.setDescription(((Person) item).profession);
            avatarItem.setAvatar(new Avatar(((Person) item).name));
            return avatarItem;
        }));
    }
}
