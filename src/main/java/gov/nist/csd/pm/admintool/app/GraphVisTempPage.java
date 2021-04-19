package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;

import gov.nist.csd.pm.admintool.graph.SingletonGraph;

public class GraphVisTempPage extends VerticalLayout {

    private SingletonGraph g;
    private SplitLayout layout;
    private GraphViewer leftGraphViewer;
    private GraphViewer rightGraphViewer;

    public GraphVisTempPage() {
        g = SingletonGraph.getInstance();
        layout = new SplitLayout();
        layout.setOrientation(Orientation.HORIZONTAL);
        layout.setSplitterPosition(50);
        layout.setWidthFull();
        // layout.setFlexGrow(1.0);
        add(layout);
        setUpLayout();
    }

    private void setUpLayout() {
        setSizeFull();
        setPadding(false);

        leftGraphViewer = new GraphViewer(true, "cy1");
        leftGraphViewer.getStyle().set("height", "100vh");
        layout.addToPrimary(leftGraphViewer);

        rightGraphViewer = new GraphViewer(false, "cy2");
        // rightGraphViewer.setWidth("50%");
        rightGraphViewer.getStyle().set("height", "100vh");
        layout.addToSecondary(rightGraphViewer);
    }

    public class GraphViewer extends VerticalLayout {
        private boolean leftGraph;
        private String graphId;

        public GraphViewer(boolean leftGraph, String graphId) {
            this.leftGraph = leftGraph;
            this.graphId = graphId;

            HorizontalLayout title = new HorizontalLayout();
            title.setAlignItems(Alignment.BASELINE);
            title.setWidthFull();
            title.setJustifyContentMode(JustifyContentMode.START);
            add(title);

            if (leftGraph) {
                title.add(new H2("Left Graph Visualization:"));
                getStyle().set("background", "lightblue");
            } else {
                title.add(new H2("Right Graph Visualization:"));
                getStyle().set("background", "lightcoral");
            }

            GraphElement cy = new GraphElement(graphId);
            cy.addClassName("cy");
            cy.setHeight("100%");
            cy.setWidth("100%");
            add(cy);
        }
    }
}
