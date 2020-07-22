package gov.nist.csd.pm.admintool.app.blips;

import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.html.Paragraph;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;

import java.util.Set;

public class AssociationBlip extends Blip {
    private Set<String> props;

    public AssociationBlip(Node node, boolean outgoing, OperationSet props) {
        super(node, outgoing);
        this.props = props;
        setDetailSummaryText();
        setDetailsContent();
    }

    @Override
    public void setDetailsContent() {
        Paragraph propertiesText = new Paragraph("Operations: ");
        propertiesText.getStyle().set("font-weight", "bold");
        addContent(propertiesText);

        OrderedList propertiesOL = new OrderedList();
        props.forEach(prop -> {
            ListItem propertyItem = new ListItem(prop);
            propertiesOL.add(propertyItem);
        });
        addContent(propertiesOL);
    }
}
