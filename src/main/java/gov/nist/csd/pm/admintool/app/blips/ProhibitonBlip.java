package gov.nist.csd.pm.admintool.app.blips;

import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import gov.nist.csd.pm.pip.prohibitions.model.Prohibition;

public class ProhibitonBlip extends Blip {
    Prohibition prohibition;


    public ProhibitonBlip(Prohibition prohibition) {
        super(true);
        this.prohibition = prohibition;
        setDetailSummaryText();
        setDetailsContent();
    }

    @Override
    public void setDetailSummaryText() {
        setSummaryText(prohibition.getName());
    }

    @Override
    public void setDetailsContent() {
        // subject (String)
        HorizontalLayout subjectLayout = new HorizontalLayout();
        Paragraph subjectText = new Paragraph("Subject: ");
        subjectText.getStyle().set("font-weight", "bold");
        subjectLayout.add(subjectText, new Paragraph(prohibition.getSubject()));
        addContent(subjectLayout);


        // operations (OperationSet)
        Paragraph operationsText = new Paragraph("Operations: ");
        operationsText.getStyle().set("font-weight", "bold");
        addContent(operationsText);

        UnorderedList operationsUL = new UnorderedList();
        prohibition.getOperations().forEach(op -> {
            ListItem propertyItem = new ListItem(op);
            operationsUL.add(propertyItem);
        });
        addContent(operationsUL);

        // containers
        Paragraph containersText = new Paragraph("Containers: (Target [Complement]) ");
        containersText.getStyle().set("font-weight", "bold");
        addContent(containersText);

        UnorderedList containersUL = new UnorderedList();
        prohibition.getContainers().forEach((target, complement) -> {
            ListItem propertyItem = new ListItem(target + " [" + complement.toString() + "]");
            containersUL.add(propertyItem);
        });
        addContent(containersUL);

        // intersection
        HorizontalLayout intersectionLayout = new HorizontalLayout();
        Paragraph intersectionText = new Paragraph("Intersection: ");
        intersectionText.getStyle().set("font-weight", "bold");
        intersectionLayout.add(intersectionText, new Paragraph(new Boolean(prohibition.isIntersection()).toString()));
        addContent(intersectionLayout);
    }
}
