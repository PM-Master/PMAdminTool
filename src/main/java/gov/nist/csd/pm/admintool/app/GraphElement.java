package gov.nist.csd.pm.admintool.app;

// import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
// import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
// import com.vaadin.flow.shared.Registration;

@Tag("cytoscape-element")
@NpmPackage(value = "@polymer/polymer", version = "3.2.0")
@NpmPackage(value = "@webcomponents/webcomponentsjs", version = "^2.2.10")
@NpmPackage(value = "cytoscape", version = "3.18.2")
@NpmPackage(value = "dagre", version = "0.7.4")
@NpmPackage(value = "cytoscape-dagre", version = "2.3.2")
@NpmPackage(value = "cytoscape-expand-collapse", version = "4.0.0")
@JsModule("./src/cytoscape-element.js")

public class GraphElement extends Div {
    public GraphElement(String elementID) {
        getElement().setProperty("cyName", elementID);
        this.setId(elementID);
    }

    public PendingJavaScriptResult getSelectedElements() {
        return getElement().callJsFunction("selectedElements", ":selected");
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

}
