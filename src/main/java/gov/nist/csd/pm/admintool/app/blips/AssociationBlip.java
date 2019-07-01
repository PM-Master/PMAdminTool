package gov.nist.csd.pm.admintool.app.blips;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Style;
import gov.nist.csd.pm.graph.model.nodes.NodeType;

import java.util.Set;

public class AssociationBlip extends HorizontalLayout {
    private Long id;
    private String name;
    private NodeType type;
    private boolean outgoing;
    private Set<String> props;

    private HorizontalLayout box;

    public AssociationBlip(long id, String name, NodeType type, boolean outgoing, Set<String> props) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.outgoing = outgoing;
        this.props = props;
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

        Paragraph idText = new Paragraph(id.toString());
        idText.getStyle().set("font-weight", "bold");

        Paragraph nameText = new Paragraph(name);
        nameText.getStyle().set("margin-left", "2px");

        Paragraph propsText = new Paragraph("");
        for(String prop: props)
            propsText.setText(propsText.getText() + prop + " ");

        if (outgoing) {
            style.set("background", "#E39A7E");
            propsText.setText("- " + propsText.getText() + '>');
//            box.add(new Icon(VaadinIcon.MINUS));
//            box.add(propsText);
//            box.add(new Icon(VaadinIcon.CHEVRON_RIGHT_SMALL));
        } else {
            style.set("background", "#ADCC9F");
            propsText.setText("< " + propsText.getText() + '-');
//            box.add(new Icon(VaadinIcon.CHEVRON_LEFT_SMALL));
//            box.add(propsText);
//            box.add(new Icon(VaadinIcon.MINUS));
        }

        box.setHeight("20px");
        box.setMinWidth("100px");
        box.setMaxWidth("200px");

        box.add(propsText);
        box.add(idText);
        box.add(nameText);

    }
}
