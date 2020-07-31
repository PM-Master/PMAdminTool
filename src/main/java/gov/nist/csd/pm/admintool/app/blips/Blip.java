package gov.nist.csd.pm.admintool.app.blips;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.dom.Style;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;

public abstract class Blip extends Details {
    public Node node;
    public boolean outgoing;

    public Blip (boolean outgoing) {
        Node tempNode = new Node();
        tempNode.setType(NodeType.PC);
        this.node = tempNode;
        this.outgoing = outgoing;
        setUpBox();
    }

    public Blip (Node node, boolean outgoing) {
        this.node = node;
        this.outgoing = outgoing;
        setUpBox();
    }

    public void setUpBox() {
        Style style = getElement().getStyle();

        style.set("border-radius", "3px")
                .set("border", "1px grey")
                .set("padding-left", "10px")
                .set("padding-right", "10px")
                .set("margin", "1px")
                .set("text-align", "center")
                .set("user-select", "none");



        addThemeVariants(DetailsVariant.SMALL);
        if (!outgoing) {
            addThemeVariants(DetailsVariant.REVERSE);
        }

        switch (node.getType()) {
            case PC:
                style.set("background", "#C1BFB5");
                break;
            case UA:
                style.set("background", "#EB4511");
                break;
            case U:
                style.set("background", "#F07751");
                break;
            case OA:
                style.set("background", "#8EB1C7");
                break;
            case O:
                style.set("background", "#ACC6D6");
                break;
            case OS:
                style.set("background", "#C1EDCC");
                break;
        }
    }

    public void setDetailSummaryText() {
        setSummaryText(node.getName() + " (" + node.getType() + ")");
    }

    abstract void setDetailsContent();
}
