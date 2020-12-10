package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;

import java.util.Objects;

@Tag("policies")
public class Policies extends VerticalLayout {
    private HorizontalLayout layout;
    private RBACArea rbacArea;
    private DACArea dacArea;
    private Details dacDetails, rbacDetails;

    public Policies() {
        layout = new HorizontalLayout();
        layout.setFlexGrow(1.0);

        add(new H2("All Tests:"));

        dacDetails = new Details("DAC", null);
        rbacDetails = new Details("RBAC", null);

        add(new Paragraph("\n"));

        add(layout);
        setUpLayout();
    }

    private void setUpLayout() {
        setSizeFull();
        setPadding(true);


        // Unit Tester
        dacArea = new DACArea();
        dacArea.setWidth("100%");
        dacDetails.setContent(dacArea);
        dacDetails.getElement().getStyle()
                .set("background", "lightblue"); //#A0FFA0, #DADADA
        dacDetails.addThemeVariants(DetailsVariant.FILLED);
        dacDetails.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                dacArea.refreshComponent();
            }
        });
        add(dacDetails);

        // ACL tester
        rbacArea = new RBACArea();
        rbacArea.setWidth("100%");
        rbacDetails.setContent(rbacArea);
        rbacDetails.getElement().getStyle()
            .set("background", "lightcoral");
        rbacDetails.addThemeVariants(DetailsVariant.FILLED);
        rbacDetails.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                rbacArea.refreshComponent();
            }
        });
        add(rbacDetails);
    }

    private class DACArea extends VerticalLayout {
        SingletonGraph g;
        HorizontalLayout configureForm;

        public DACArea () {
            setPadding(false);
            setMargin(false);
            setWidthFull();
//            setAlignItems(Alignment.CENTER);
            setAlignItems(Alignment.STRETCH);
            setJustifyContentMode(JustifyContentMode.START);

            g = SingletonGraph.getInstance();

            addConfigureSection();

        }

        private void addConfigureSection() {
            configureForm = new HorizontalLayout();
            configureForm.setAlignItems(FlexComponent.Alignment.BASELINE);
            configureForm.setWidthFull();
            configureForm.setMargin(false);

            Select<SingletonGraph.PolicyClassWithActive> policySelect = new Select<>();

            policySelect.setLabel("Choose DAC PC");
            policySelect.setPlaceholder("Select an option");
            policySelect.setEmptySelectionCaption("Select an option");
            policySelect.setEmptySelectionAllowed(true);
//            policySelect.setItemEnabledProvider(Objects::nonNull);
            policySelect.addComponents(null, new Hr());
            policySelect.setItems(g.getActivePCs());
            policySelect.setTextRenderer((pc) -> pc.getName());
            configureForm.add(policySelect);

            // submit button
            Button configureButton = new Button("Configure DAC", event -> {
                // todo: Add Warning Dialog - Will reset graph
                try {
                    if (policySelect.getValue() != null) {
                        g.reset();
                        g.configureDAC(policySelect.getValue().getName());
                    } else {
                        g.configureDAC(null);
                    }

                    MainView.notify("DAC has been configured.", MainView.NotificationType.SUCCESS);

                } catch (PMException e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
            });
            configureButton.setEnabled(true);
            configureForm.add(configureButton);

            add (configureForm);
        }


        public void refreshComponent() {
        }
    }
    private class RBACArea extends VerticalLayout {
        SingletonGraph g;

        public RBACArea () {
            setPadding(false);
            setMargin(false);
            setWidthFull();
//            setAlignItems(Alignment.CENTER);
            setAlignItems(Alignment.STRETCH);
            setJustifyContentMode(JustifyContentMode.START);


            H2 importTitle = new H2("RBAC:");
            importTitle.getStyle().set("margin-bottom","0");
            add(importTitle);

            g = SingletonGraph.getInstance();
        }

        public void refreshComponent() {
        }
    }
}
