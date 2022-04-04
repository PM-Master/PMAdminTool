package gov.nist.csd.pm.admintool.app;

// import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
// import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
// import com.vaadin.flow.shared.Registration;

@Tag("cytoscape-element")
@NpmPackage(value = "@polymer/polymer", version = "3.4.1")
// @NpmPackage(value = "@webcomponents/webcomponentsjs", version = "^2.2.10")
@NpmPackage(value = "cytoscape", version = "3.18.2")
@NpmPackage(value = "cytoscape-popper", version = "2.0.0")
@NpmPackage(value = "tippy.js", version = "6.3.7")
@NpmPackage(value = "jquery", version = "3.2.1")
@JsModule("./src/cytoscape-element.js")
@CssImport("./styles/csd_pm_style.css")

public class GraphElement extends Div {
    private ClickCallback clickCallback;
    private SingletonGraph g;

    public GraphElement(String elementID, String graph) {
        g = SingletonGraph.getInstance();

        this.setId(elementID);
        getElement().setProperty("cyName", elementID);
        getElement().setProperty("graphFromVaadin", graph);

        getElement().addEventListener("click", (mouseEvent) -> {
            getSelectedElements().then((jsonValue) -> {
                if (clickCallback != null)
                    clickCallback.onClick(jsonValue.toJson() != null ? jsonValue.asString() : null);
            });
        });

        getElement().addEventListener("dblclick", (mouseEvent) -> getElement().callJsFunction("fit"));

        ContextMenu menu = new ContextMenu();
        menu.setTarget(this);
        menu.addItem("Download JPEG", menuItemClickEvent -> getElement().callJsFunction("download"));
        menu.addItem("Highlight Path from Selected", menuItemClickEvent -> {
            getSelectedElements().then((jsonValue) -> {
                if (jsonValue.toJson() != null) {
                    getElement().callJsFunction("highlight", jsonValue.asString());
                } else {
                    MainView.notify("No Node Selected! Please select node before highlighting");
                }
            });
        });
        menu.addItem("Highlight Parents from Selected", menuItemClickEvent -> {
            getSelectedElements().then((jsonValue) -> {
                if (jsonValue.toJson() != null) {
                    try {
                        g.getParents(jsonValue.asString()).forEach(this::highlightNode);
                    } catch (PMException e) {
                        e.printStackTrace();
                    }
                } else {
                    MainView.notify("No Node Selected! Please select node before highlighting");
                }
            });

        });
        menu.addItem("Highlight Children from Selected", menuItemClickEvent -> {
            getSelectedElements().then((jsonValue) -> {
                if (jsonValue.toJson() != null) {
                    try {
                        g.getChildren(jsonValue.asString()).forEach(this::highlightNode);
                    } catch (PMException e) {
                        e.printStackTrace();
                    }
                } else {
                    MainView.notify("No Node Selected! Please select node before highlighting");
                }
            });

        });
    }

    public void reset(String graph) {
        getElement().setProperty("graphFromVaadin", graph);
        getElement().callJsFunction("reset", graph);
    }

    private void highlightNode(String node_name) {
        getElement().callJsFunction("highlightNode", node_name);
    }

    private PendingJavaScriptResult getSelectedElements() {
        return getElement().callJsFunction("getSelected");
    }

    public void setClickListener(ClickCallback clickCallback) {
        this.clickCallback = clickCallback;
    }

    public PendingJavaScriptResult getZoomAndPan() {
        return getElement().callJsFunction("getZoomAndPan");
    }

    public void setViewport(String viewport) {
        getElement().callJsFunction("setViewport", viewport);
    }

    public void loadGraph1() {
        getElement().callJsFunction("loadGraph1");
    }

    public void loadGraph2() {
        getElement().callJsFunction("loadGraph2");
    }

    public interface ClickCallback {
        void onClick(String node_id);
    }

}
