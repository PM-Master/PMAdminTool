package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.nist.csd.pm.admintool.app.testingApps.POSTester;
import gov.nist.csd.pm.admintool.app.testingApps.UnitTester;

@Tag("tester")
public class Tester extends VerticalLayout {
    private HorizontalLayout layout;
    private POSTester posTester;
    private UnitTester unitTester;
    private Details pos, obligations, unitTests;

    public Tester() {
        layout = new HorizontalLayout();
        layout.setFlexGrow(1.0);

        pos = new Details("POS Tester", null);
        obligations = new Details("Obligations Tester", new Span("obligatoins tester"));
        unitTests = new Details("Unit Tests", null);

        add(new Paragraph("\n"));

        add(layout);
        setUpLayout();
    }

    private void setUpLayout() {
        setSizeFull();
        setPadding(true);

        posTester = new POSTester();
        posTester.setWidth("100%");
        pos.setContent(posTester);
        pos.getElement().getStyle()
                .set("background", "lightblue");
        pos.addThemeVariants(DetailsVariant.FILLED);
        pos.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                posTester.setUserSelect();
            }
        });
        add(pos);

        obligations.getElement().getStyle()
                .set("background", "lightcoral");
        obligations.addThemeVariants(DetailsVariant.FILLED);
        add(obligations);

        unitTester = new UnitTester();
        unitTester.setWidth("100%");
        unitTests.setContent(unitTester);
        unitTests.getElement().getStyle()
                .set("background", "#a0ffa0");
        unitTests.addThemeVariants(DetailsVariant.FILLED);
        add(unitTests);
    }

    public void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
    }

}
