package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.nist.csd.pm.admintool.app.testingApps.ACLTester;
import gov.nist.csd.pm.admintool.app.testingApps.POSTester;
import gov.nist.csd.pm.admintool.app.testingApps.UnitTester;

@Tag("tester")
public class Tester extends VerticalLayout {
    private HorizontalLayout layout;
    private UnitTester unitTester;
    private ACLTester aclTester;
    private Details obligations, unitTests, acl;

    public Tester() {
        layout = new HorizontalLayout();
        layout.setFlexGrow(1.0);

        add(new H2("All Tests:"));

        obligations = new Details("Obligations Tester", new Span("obligations tester"));
        unitTests = new Details("Unit Tests", null);
        acl = new Details("ACL Generator", null);

        add(new Paragraph("\n"));

        add(layout);
        setUpLayout();
    }

    private void setUpLayout() {
        setSizeFull();
        setPadding(true);

        // Obligation Tester
        obligations.getElement().getStyle()
                .set("background", "lightcoral");
        obligations.addThemeVariants(DetailsVariant.FILLED);
        add(obligations);

        // Unit Tester
        unitTester = new UnitTester();
        unitTester.setWidth("100%");
        unitTests.setContent(unitTester);
        unitTests.getElement().getStyle()
                .set("background", "#DADADA"); //#A0FFA0
        unitTests.addThemeVariants(DetailsVariant.FILLED);
        unitTests.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                unitTester.refreshComponent();
            }
        });
        add(unitTests);

        // ACL tester
        aclTester = new ACLTester();
        aclTester.setWidth("100%");
        acl.setContent(aclTester);
        acl.getElement().getStyle()
                .set("background", "lightblue");
        acl.addThemeVariants(DetailsVariant.FILLED);
        acl.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                aclTester.setAttrSelect();
            }
        });
        add(acl);
    }
}
