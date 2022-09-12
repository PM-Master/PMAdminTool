package gov.nist.csd.pm.admintool.app.blips;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import gov.nist.csd.pm.policy.model.graph.nodes.Node;

public class NodeDataBlip extends Blip {
    public NodeDataBlip(Node node, boolean outgoing) {
        super(node, outgoing);
        setDetailSummaryText();
        setDetailsContent();
    }

    @Override
    public void setDetailsContent() {
        HorizontalLayout line1 = new HorizontalLayout();
        Paragraph nameText = new Paragraph("Name: ");
        nameText.getStyle().set("font-weight", "bold");
        line1.add(nameText, new Paragraph(node.getName()));

        HorizontalLayout line2 = new HorizontalLayout();
        Paragraph typeText = new Paragraph("Type: ");
        typeText.getStyle().set("font-weight", "bold");
        line2.add(typeText, new Paragraph(node.getType().toString()));

        addContent(line1, line2);
    }
}
