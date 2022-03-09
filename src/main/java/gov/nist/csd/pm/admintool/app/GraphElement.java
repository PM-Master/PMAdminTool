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
// import com.vaadin.flow.shared.Registration;

@Tag("cytoscape-element")
@NpmPackage(value = "@polymer/polymer", version = "3.4.1")
// @NpmPackage(value = "@webcomponents/webcomponentsjs", version = "^2.2.10")
@NpmPackage(value = "cytoscape", version = "3.18.2")
@NpmPackage(value = "jquery", version = "3.2.1")
@JsModule("./src/cytoscape-element.js")
@CssImport("./styles/csd_pm_style.css")

public class GraphElement extends Div {
    private ClickCallback clickCallback;

    public GraphElement(String elementID, String graph) {
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
