package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;


public class GraphVisTempPage extends VerticalLayout {

    private SingletonGraph g;
    private HorizontalLayout layout;
    private GraphViewer leftGraphViewer;
    private GraphViewer rightGraphViewer;

    public GraphVisTempPage() {
        g = SingletonGraph.getInstance();
        layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setFlexGrow(1.0);
        add(layout);
        setUpLayout();
    }

    private void setUpLayout(){
        setSizeFull();
        setPadding(false);

        leftGraphViewer = new GraphViewer(true);
        leftGraphViewer.setWidth("50%");
        leftGraphViewer.getStyle().set("height","100vh");
        layout.add(leftGraphViewer);

        rightGraphViewer = new GraphViewer(false);
        rightGraphViewer.setWidth("50%");
        rightGraphViewer.getStyle().set("height","100vh");
        layout.add(rightGraphViewer);
    }

    public class GraphViewer extends VerticalLayout {
        private boolean leftGraph;

        public GraphViewer(boolean leftGraph) {
            this.leftGraph = leftGraph;

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

        }


    }
}
