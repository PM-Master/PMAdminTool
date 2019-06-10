package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route
public class MainView extends HorizontalLayout {
    private SingletonGraph g;
    public MainView() {
        g = SingletonGraph.getInstance();

        VerticalLayout navbar = new VerticalLayout();
        navbar.setWidth("15%");
        navbar.setJustifyContentMode(JustifyContentMode.START);
        navbar.getStyle()
                .set("border-right", "1px solid lightgray")
                .set("height", "100vh")
                .set("background", "lightgray");

        H3 admintool = new H3("Admin Tool");
        navbar.add(admintool);

        Tab tab1 = new Tab("Graph Editor");
        VerticalLayout page1 = new GraphEditor();
        page1.setSizeFull();

        Tab tab2 = new Tab("Smart Policy Tool");
        Component page2 = new GraphEditor();
        page2.setVisible(false);
        tab2.setEnabled(false);

        Tab tab3 = new Tab("Import/Export");
        VerticalLayout page3 = new ImportExport();
        page3.setSizeFull();
        page3.setVisible(false);


        Tabs tabs = new Tabs(tab1, tab2, tab3);
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.setFlexGrowForEnclosedTabs(1);
        navbar.add(tabs);

        Map<Tab, Component> tabsToPages = new HashMap<>();
        tabsToPages.put(tab1, page1);
        tabsToPages.put(tab2, page2);
        tabsToPages.put(tab3, page3);

        Div pages = new Div(page1, page2, page3);
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

        add(navbar);
        add(pages);
    }

}
