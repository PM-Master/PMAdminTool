package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
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
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;

import java.util.*;
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

    private SingletonGraph g;

    public MainView() {
        g = SingletonGraph.getInstance();

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
        admintool.getStyle().set("user-select", "none")
                .set("margin-bottom", "0");
        navbar.add(admintool);

        Paragraph currentUser = new Paragraph("Current User: " + g.getCurrentContext());
        currentUser.getStyle()
                .set("color", "#a1a1a1")
                .set("cursor", "pointer")
                .set("margin-top", "0");
        currentUser.addClickListener(component -> switchUserContextDialog());
        navbar.add(currentUser);

        // tabs
        Tab  tab1 = new Tab("Graph Editor");
        VerticalLayout page1 = new GraphEditor();
        page1.setSizeFull();

        Tab tab2 = new Tab("Import/Export");
        VerticalLayout page2 = null;
        try {
            page2 = new ImportExport();
            page2.setSizeFull();
            page2.setVisible(false);
        } catch (PMException e) {
            MainView.notify(e.getMessage(), NotificationType.ERROR, 10 * 1000);
            tab2.setEnabled(false);
            tab2.getElement().setProperty("title", e.getMessage())
                    .setProperty("user-select", "auto");
        }

        Tab tab3 = new Tab("Smart Policy Tool");
        VerticalLayout page3 = new SPTEditor();
        page3.setSizeFull();
        page3.setVisible(false);

        Tab tab4 = new Tab("Prohibition Editor");
        VerticalLayout page4 = new ProhibitionEditor();
        page4.setSizeFull();
        page4.setVisible(false);

        Tab tab5 = new Tab("Obligation Editor");
        VerticalLayout page5 = null;
        try {
            page5 = new ObligationEditor();
            page5.setSizeFull();
            page5.setVisible(false);
        } catch (PMException e) {
            MainView.notify(e.getMessage(), NotificationType.ERROR, 10 * 1000);
            tab5.setEnabled(false);
            tab5.getElement().setProperty("title", e.getMessage());
        }

        Tab tab6 = new Tab("Access Rights Editor");
        VerticalLayout page6 = new OperationsEditor();
        page6.setSizeFull();
        page6.setVisible(false);

        Tab tab7 = new Tab("Tester");
        VerticalLayout page7 = new Tester();
        page7.setSizeFull();
        page7.setVisible(false);

        Tab tab8 = new Tab("Policies");
        VerticalLayout page8 = new Policies();
        page8.setSizeFull();
        page8.setVisible(false);

        Tab tab9 = new Tab("Settings");
        VerticalLayout page9 = new PolicyClassEditor();
        page9.add(new Settings());
        page9.setSizeFull();
        page9.setVisible(false);

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

        Tabs tabs = new Tabs(tab1, tab2, tab3, tab4, tab5, tab6, tab7, tab8, tab9);
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.setFlexGrowForEnclosedTabs(1);
        navbar.add(tabs);

        pages = new Div();
        for (Component page: tabsToPages.values()) {
            if (page != null) pages.add(page);
        }
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

    private void switchUserContextDialog() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();

        // get all users that are not the current user
        Collection<Node> nodeCollection;
        try {
            nodeCollection = new HashSet<>(g.getActiveNodes());
            nodeCollection.removeIf(curr -> !(curr.getType() == NodeType.U) || curr.getName().equals(g.getCurrentContext()));
        } catch (PMException e) {
            nodeCollection = new HashSet<>();
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }
        List<String> nodes = nodeCollection.stream().map(Node::getName).collect(Collectors.toList());

        // user select filed
        ComboBox<String> userSelect = new ComboBox<>();
        userSelect.setRequiredIndicatorVisible(true);
        userSelect.setLabel("New User");
        userSelect.setPlaceholder("Select User...");
        userSelect.setWidthFull();

        // allow for custom values
        userSelect.setAllowCustomValue(true);
        userSelect.addCustomValueSetListener(e -> {
            String customValue = e.getDetail();
            if (nodes.contains(customValue)) return;

            nodes.add(customValue);
            userSelect.setItems(nodes);
            userSelect.setValue(customValue);
        });

        userSelect.setItems(nodes);
        form.add(userSelect);

        // ----- Title Section -----
        Button button = new Button("Submit", event -> {
            String user = userSelect.getValue();

            if (user == null) {
                userSelect.focus();
                MainView.notify("User selection is Required", MainView.NotificationType.DEFAULT);
            } else {
                g.setUserContext(user);
                dialog.close();
                UI.getCurrent().getPage().reload();
            }
        });
        HorizontalLayout titleLayout = TitleFactory.generate("Login", "Current User: " + g.getCurrentContext(), button);

        dialog.add(titleLayout, new Hr(), form);
        dialog.setMinWidth("25%");
        dialog.open();
        userSelect.focus();
    }

    public static void notify(String message, NotificationType type) {
        notify(message, type, 3000);
    }

    public static void notify(String message, NotificationType type, int duration) {
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
        notif.setDuration(duration);
        notif.open();
    }

    public static void notify(String message) {
        notify(message, NotificationType.DEFAULT);
    }

    public enum NotificationType {
        SUCCESS, ERROR, DEFAULT
    }
}
