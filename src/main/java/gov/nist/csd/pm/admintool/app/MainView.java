package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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
public class MainView extends HorizontalLayout{
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

        Tab  tab1 = new Tab("Graph Editor");
        VerticalLayout page1 = new GraphEditor();
        page1.setSizeFull();

        Tab tab2 = new Tab("Import/Export");
        VerticalLayout page2 = new ImportExport();
        page2.setSizeFull();
        page2.setVisible(false);

        Tab tab3 = new Tab("Smart Policy Tool");
        VerticalLayout page3 = new SPTEditor();
        page3.setSizeFull();
        page3.setVisible(false);

        Tab tab4 = new Tab("Prohibition Editor");
        VerticalLayout page4 = new ProhibitionEditor();
        page4.setSizeFull();
        page4.setVisible(false);
        page4.setEnabled(false);

        Tab tab5 = new Tab("Obligation Editor");
        VerticalLayout page5 = new ObligationEditor();
        page5.setSizeFull();
        page5.setVisible(false);
        page5.setEnabled(false);

        Tab tab6 = new Tab("Operations Editor");
        VerticalLayout page6 = new OperationsEditor();
        page6.setSizeFull();
        page6.setVisible(false);

        Tab tab7 = new Tab("Tester");
        VerticalLayout page7 = new Tester();
        page7.setSizeFull();
        page7.setVisible(false);

        Tab tab8 = new Tab("Settings");
        VerticalLayout page8 = new PolicyClassEditor();
        page8.add(new Settings());
        page8.setSizeFull();
        page8.setVisible(false);

        Tabs tabs = new Tabs(tab1, tab2, tab3, tab4, tab5, tab6, tab7, tab8);
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
        tabsToPages.put(tab7, page7);
        tabsToPages.put(tab8, page8);

        pages = new Div(page1, page2, page3, page4, page5, page6, page7, page8);
        pages.setSizeFull();

        Set<Component> pagesShown = Stream.of(page1)
                .collect(Collectors.toSet());
        tabs.addSelectedChangeListener(event -> {
            pagesShown.forEach(page -> page.setVisible(false));
            pagesShown.clear();
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
            pagesShown.add(selectedPage);

        });

        testResults.getStyle()
                .set("overflow-y", "hidden")
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

    public static void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
    }

}
