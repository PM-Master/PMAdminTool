package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
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
import gov.nist.csd.pm.admintool.app.testingApps.POSTester;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the main page of the whole tool.
 * It contains:
 *  - The navigation bar on the left side of the screen,
 *  - Instances of all the actual pages,
 *  - Test results visualization, and
 *  - Various global helper functions.
 */
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
        admintool.getStyle().set("user-select", "none");
        navbar.add(admintool);

        Tab  tab1 = new Tab("Graph Editor");
        VerticalLayout page1 = new GraphEditor();
        page1.setSizeFull();

        Tab tab2 = new Tab("POS");
        VerticalLayout page2 = new POSTester();
        page2.setSizeFull();
        page2.setVisible(false);

        Tab tab3 = new Tab("Import/Export");
        VerticalLayout page3 = new ImportExport();
        page3.setSizeFull();
        page3.setVisible(false);

        Tab tab4 = new Tab("Smart Policy Tool");
        VerticalLayout page4 = new SPTEditor();
        page4.setSizeFull();
        page4.setVisible(false);

        Tab tab5 = new Tab("Prohibition Editor");
        VerticalLayout page5 = new ProhibitionEditor();
        page5.setSizeFull();
        page5.setVisible(false);

        Tab tab6 = new Tab("Obligation Editor");
        VerticalLayout page6 = new ObligationEditor();
        page6.setSizeFull();
        page6.setVisible(false);
        //page6.setEnabled(false);

        Tab tab7 = new Tab("Access Rights Editor");
        VerticalLayout page7 = new OperationsEditor();
        page7.setSizeFull();
        page7.setVisible(false);

        Tab tab8 = new Tab("Tester");
        VerticalLayout page8 = new Tester();
        page8.setSizeFull();
        page8.setVisible(false);

        Tab tab9 = new Tab("Policies");
        VerticalLayout page9 = new Policies();
        page9.setSizeFull();
        page9.setVisible(false);

        Tab tab10 = new Tab("GraphVis");
        VerticalLayout page10 = new GraphVisTempPage();
        page10.setSizeFull();
        page10.setVisible(false);

        Tab tab11 = new Tab("Settings");
        VerticalLayout page11 = new PolicyClassEditor();
        page11.add(new Settings());
        page11.setSizeFull();
        page11.setVisible(false);

        Tabs tabs = new Tabs(tab1, tab2, tab3, tab4, tab5, tab6, tab7, tab8, tab9, tab10, tab11);
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
        tabsToPages.put(tab9, page9);
        tabsToPages.put(tab10, page10);
        tabsToPages.put(tab11, page11);

        pages = new Div(page1, page2, page3, page4, page5, page6, page7, page8, page9, page10, page11);
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

    public static void notify(String message, NotificationType type) {
        Paragraph text = new Paragraph(message);
        switch(type) {
            case SUCCESS:
                text.getStyle().set("color", "#009933"); //success
                break;
            case ERROR:
                text.getStyle().set("color", "#990033"); //error
                break;
            case DEFAULT:
            default:
                break;
        }
        Notification notif = new Notification(text);
        notif.setDuration(3000);
        notif.open();
    }

    public static void notify(String message) {
        notify(message, NotificationType.DEFAULT);
    }

    public enum NotificationType {
        SUCCESS, ERROR, DEFAULT
    }
}
