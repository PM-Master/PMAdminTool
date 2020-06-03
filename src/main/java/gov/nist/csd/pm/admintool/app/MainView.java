package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import gov.nist.csd.pm.admintool.actions.Action;
import gov.nist.csd.pm.admintool.actions.SingletonActiveActions;
import gov.nist.csd.pm.admintool.actions.events.Event;
import gov.nist.csd.pm.admintool.actions.tests.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route
public class MainView extends HorizontalLayout {
    private Div testResults;
    private Div pages;
    private SingletonActiveActions actions;
    private VerticalLayout navbar;

    public MainView() {
        testResults = new Div();
        actions = SingletonActiveActions.getInstance();

        navbar = new VerticalLayout();
        navbar.setWidth("16%");
        navbar.setJustifyContentMode(JustifyContentMode.START);
        navbar.getStyle()
                .set("border-right", "1px solid #FFF3D3")
                .set("height", "100vh")
                .set("background", "#FFF3D3");

        H3 admintool = new H3("Admin Tool");
        navbar.add(admintool);

        Tab tab1 = new Tab("Graph Editor");
        VerticalLayout page1 = new GraphEditor();
        page1.setSizeFull();

        Tab tab2 = new Tab("PC Editor");
        VerticalLayout page2 = new PolicyClassEditor();
        page2.setSizeFull();
        page2.setVisible(false);

        Tab tab3 = new Tab("Obligation Editor");
        VerticalLayout page3 = new ObligationEditor();
        page3.setSizeFull();
        page3.setVisible(false);

        Tab tab4 = new Tab("Smart Policy Tool");
        VerticalLayout page4 = new SPTEditor();
        page4.setSizeFull();
        page4.setVisible(false);

        Tab tab5 = new Tab("Import/Export");
        VerticalLayout page5 = new ImportExport();
        page5.setSizeFull();
        page5.setVisible(false);

        Tab tab6 = new Tab("Tester");
        VerticalLayout page6 = new Tester();
        page6.setSizeFull();
        page6.setVisible(false);


        Tabs tabs = new Tabs(tab1, tab2, tab3, tab4, tab5, tab6);
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.setFlexGrowForEnclosedTabs(1);
        navbar.add(tabs);

        Map<Tab, Component> tabsToPages = new HashMap<>();
        tabsToPages.put(tab1, page1);
        tabsToPages.put(tab2, page2);
        tabsToPages.put(tab3, page3);
        tabsToPages.put(tab4, page4);
        tabsToPages.put(tab5, page5);
        tabsToPages.put(tab6, page6);

        pages = new Div(page1, page2, page3, page4, page5, page6);
        pages.setSizeFull();

        Set<Component> pagesShown = Stream.of(page1)
                .collect(Collectors.toSet());
        tabs.addSelectedChangeListener(event -> {
            pagesShown.forEach(page -> page.setVisible(false));
            pagesShown.clear();
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
            pagesShown.add(selectedPage);
            if (tabs.getSelectedTab().equals(tab1)) {
                UI.getCurrent().getPage().reload();
            }
        });

        testResults.getStyle()
                .set("overflow-y", "scroll")
                .set("position", "absolute")
                .set("bottom", "0")
                .set("left", "0");
        testResults.setWidth("16%");
        testResults.setHeight("10%");
        refreshTestResults();
        navbar.add(testResults);

        add(navbar);
        add(pages);
    }

    public void refreshTestResults() {
        testResults.removeAll();
        for (Action action: actions) {
            Icon line = new Icon(VaadinIcon.LINE_V);
            if (action instanceof Test) {
                if (action.run()) {
                    line.setColor("green");
                } else {
                    line.setColor("red");
                }
            } else if (action instanceof Event) {
                if (action.run()) {
                    line.setColor("blue");
                } else {
                    line.setColor("black");
                }
            }
            testResults.add(line);
        }
    }

}
