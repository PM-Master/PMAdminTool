package gov.nist.csd.pm.admintool.app.blips;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Style;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;

import java.util.Set;

public class AssociationBlip extends HorizontalLayout {
    private Long id;
    private String name;
    private NodeType type;
    private boolean outgoing;
    private Set<String> props;

//    private HorizontalLayout box;

    public AssociationBlip(long id, String name, NodeType type, boolean outgoing, OperationSet props) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.outgoing = outgoing;
        this.props = props;

//        getStyle().set("margin" , "0");
        setUpBox();
    }

    private void setUpBox() {
        Style style = getStyle();
        style.set("border-radius", "3px")
                .set("border", "1px grey")
                .set("padding", "2px")
                .set("margin-top", "1px");

        setHeight("20px");
        setWidth("99%");

//        if (outgoing) {
//            style.set("background", "#E39A7E");
//        } else {
//            style.set("background", "#ADCC9F");
//        }
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

        Paragraph propsText = new Paragraph(props.toString());
        if (outgoing) {
            propsText.setText(propsText.getText() + ">");
        } else {
            propsText.setText("<" + propsText.getText());
        }
//        Paragraph propsText = new Paragraph("");
//        for(String prop: props)
//            propsText.setText(propsText.getText() + prop + " ");
//        if (outgoing) {
//            propsText.setText("--{ " + propsText.getText() + "}->");
//        } else {
//            propsText.setText("<-{ " + propsText.getText() + "}--");
//        }
        add(propsText);

        Paragraph typeText = new Paragraph(type.toString());
        typeText.getStyle().set("font-weight", "bold");
        add(typeText);


        Paragraph nameText = new Paragraph(name);
        nameText.getStyle().set("margin-left", "2px");
        add(nameText);

    }
}
