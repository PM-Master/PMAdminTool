package gov.nist.csd.pm.admintool.app.blips;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Style;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;

public class NodeDataBlip extends HorizontalLayout {
    private String name;
    private NodeType type;

    private HorizontalLayout box;

    public NodeDataBlip(String name, NodeType type) {
        this.name = name;
        this.type = type;
        box = new HorizontalLayout();
        add(box);
        getStyle().set("margin" , "0");
        setUpBox();
    }

    private void setUpBox() {
        Style style = box.getStyle();
        style.set("border-radius", "3px")
                .set("border", "1px grey")
                .set("padding", "2px")
                .set("margin-top", "0px");

        switch (type) {
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

        box.setHeight("20px");
        box.setMinWidth("50px");
        box.setMaxWidth("100px");

        Paragraph typeText = new Paragraph(type.toString());
        typeText.getStyle().set("font-weight", "bold");
        box.add(typeText);

        Paragraph nameText = new Paragraph(name);
        nameText.getStyle().set("margin-left", "2px");
        box.add(nameText);
    }
}
