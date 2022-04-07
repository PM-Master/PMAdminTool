package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;

@Tag("cytoscape-element")
@NpmPackage(value = "@polymer/polymer", version = "3.4.1")
@NpmPackage(value = "cytoscape", version = "3.18.2")
@NpmPackage(value = "cytoscape-popper", version = "2.0.0")
@NpmPackage(value = "tippy.js", version = "6.3.7")
@NpmPackage(value = "jquery", version = "3.2.1")
@JsModule("./src/cytoscape-element.js")
@CssImport("./styles/csd_pm_style.css")

public class CytoscapeElement extends Div {
    private ClickCallback clickCallback;
    private SingletonGraph g;

    public CytoscapeElement(String elementID) throws PMException {
        // get singleton instance
        g = SingletonGraph.getInstance();

        // html meta info
        this.setId(elementID);
        getElement().setProperty("cyName", elementID);

        // pass graph json to cytoscape
        getElement().setProperty("graphFromVaadin", ImportExport.toFullJson(g));

        // add interactive functionality
        addListeners();
        addContextMenu();
    }

    // constructor helpers
    private void addListeners() {
        getElement().addEventListener("click", (mouseEvent) -> {
            getSelectedElements().then((jsonValue) -> {
                if (clickCallback != null)
                    clickCallback.onClick(jsonValue.toJson() != null ? jsonValue.asString() : null);
            });
        });

        getElement().addEventListener("dblclick", (mouseEvent) -> getElement().callJsFunction("fit"));
    }

    private void addContextMenu() {
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


    // public methods
    public void setClickListener(ClickCallback clickCallback) {
        this.clickCallback = clickCallback;
    }

    public void reset() throws PMException{
        String graph = ImportExport.toFullJson(g);
        getElement().setProperty("graphFromVaadin", graph);
        getElement().callJsFunction("reset");
    }


    // methods to interact with underlying javascript
    private void highlightNode(String node_name) {
        getElement().callJsFunction("highlightNode", node_name);
    }

    private PendingJavaScriptResult getSelectedElements() {
        return getElement().callJsFunction("getSelected");
    }


    // click callback to give custom functionality to clicking on a node
    public interface ClickCallback {
        void onClick(String node_id);
    }
}
