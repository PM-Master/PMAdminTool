package gov.nist.csd.pm.admintool.app;

// import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
// import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
// import com.vaadin.flow.shared.Registration;

@Tag("cytoscape-element")
@NpmPackage(value = "@polymer/polymer", version = "3.4.1")
// @NpmPackage(value = "@webcomponents/webcomponentsjs", version = "^2.2.10")
@NpmPackage(value = "cytoscape", version = "3.18.2")
@NpmPackage(value = "jquery", version = "3.2.1")
@JsModule("./src/cytoscape-element.js")
@CssImport("./styles/csd_pm_style.css")

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

    public void loadGraph2() {
        getElement().callJsFunction("loadGraph2");
    }

}
