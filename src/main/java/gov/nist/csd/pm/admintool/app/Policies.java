package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import gov.nist.csd.pm.admintool.app.customElements.MapInput;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import gov.nist.csd.pm.policies.dac.DAC;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.*;

@Tag("policies")
public class Policies extends VerticalLayout {
    private HorizontalLayout layout;
    private ConfigurationArea configurationArea;
    private MethodsArea methodsArea;
    private POSArea posArea;
    private Details configurationDetails, methodsDetails, posDetails;

    SingletonGraph g;

    public Policies() {
        g = SingletonGraph.getInstance();

        add(new H2("Policies:"));

        add(new Paragraph("\n"));

        setUpLayout();
    }

    private void setUpLayout() {
        setSizeFull();
        setPadding(true);

        // Configuration Area
        configurationArea = new ConfigurationArea();
        configurationArea.setWidth("100%");
        configurationDetails = new Details("Policy Configuration", null);
        configurationDetails.setContent(configurationArea);
        configurationDetails.getElement().getStyle()
                .set("background", "lightblue"); //#A0FFA0, #DADADA
        configurationDetails.addThemeVariants(DetailsVariant.FILLED);
        configurationDetails.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                configurationArea.refreshComponent();
            }
        });
        add(configurationDetails);

        // Methods Area
        methodsArea = new MethodsArea();
        methodsArea.setWidth("100%");
        methodsDetails = new Details("Methods", null);
        methodsDetails.setContent(methodsArea);
        methodsDetails.getElement().getStyle()
                .set("background", "#DADADA");
        methodsDetails.addThemeVariants(DetailsVariant.FILLED);
        methodsDetails.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                methodsArea.refreshComponent();
            }
        });
        add(methodsDetails);

        // POS Area
        posArea = new POSArea();
        posArea.setWidth("100%");
        posDetails = new Details("POS", null);
        posDetails.setContent(posArea);
        posDetails.getElement().getStyle()
                .set("background", "lightcoral");
        posDetails.addThemeVariants(DetailsVariant.FILLED);
        posDetails.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                posArea.refreshComponent();
            }
        });
        add(posDetails);
    }

    private class ConfigurationArea extends VerticalLayout {
        HorizontalLayout configureForm;

        public ConfigurationArea() {
            setPadding(false);
            setMargin(false);
            setWidthFull();
//            setAlignItems(Alignment.CENTER);
            setAlignItems(Alignment.STRETCH);
            setJustifyContentMode(JustifyContentMode.START);

            addConfigureSection();

        }

        private void addConfigureSection() {
            configureForm = new HorizontalLayout();
            configureForm.setAlignItems(FlexComponent.Alignment.BASELINE);
            configureForm.setWidthFull();
            configureForm.setMargin(false);

            Select<SingletonGraph.PolicyClassWithActive> dacPolicySelect = new Select<>();
            dacPolicySelect.setLabel("Choose DAC PC");
            dacPolicySelect.setPlaceholder("Select an option");
            dacPolicySelect.setEmptySelectionCaption("Select an option");
            dacPolicySelect.setEmptySelectionAllowed(true);
//            dacPolicySelect.setItemEnabledProvider(Objects::nonNull);
            dacPolicySelect.addComponents(null, new Hr());
            dacPolicySelect.setItems(g.getActivePCs());
            dacPolicySelect.setTextRenderer((pc) -> pc.getName());
            configureForm.add(dacPolicySelect);

            Select<SingletonGraph.PolicyClassWithActive> rbacPolicySelect = new Select<>();
            rbacPolicySelect.setLabel("Choose RBAC PC");
            rbacPolicySelect.setPlaceholder("Select an option");
            rbacPolicySelect.setEmptySelectionCaption("Select an option");
            rbacPolicySelect.setEmptySelectionAllowed(true);
//            rbacPolicySelect.setItemEnabledProvider(Objects::nonNull);
            rbacPolicySelect.addComponents(null, new Hr());
            rbacPolicySelect.setItems(g.getActivePCs());
            rbacPolicySelect.setTextRenderer((pc) -> pc.getName());
            configureForm.add(rbacPolicySelect);

            // submit button
            Button configureButton = new Button("Configure Policies", event -> {
                // todo: Add Warning Dialog - Will reset graph
                try {
                    if (dacPolicySelect.getValue() != null) {
                        g.reset();
                        g.configureDAC(dacPolicySelect.getValue().getName());
                    } else {
                        g.configureDAC(null);
                    }

                    if (rbacPolicySelect.getValue() != null) {
                        g.reset();
                        g.configureRBAC(rbacPolicySelect.getValue().getName());
                    } else {
                        g.configureRBAC(null);
                    }

                    MainView.notify("Policies have been configured.", MainView.NotificationType.SUCCESS);

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
    private class MethodsArea extends HorizontalLayout {
        VerticalLayout dacMethods, rbacMethods;
        Button assignOwner;
        public MethodsArea() {
            g = SingletonGraph.getInstance();

            setPadding(false);
            setMargin(false);
            setWidthFull();
            setAlignItems(Alignment.STRETCH);
            setJustifyContentMode(JustifyContentMode.START);

            dacMethods = new VerticalLayout();
            rbacMethods = new VerticalLayout();
            add(dacMethods, rbacMethods);

            // dac things
            H2 dacTitle = new H2("DAC Methods:");
            dacTitle.getStyle().set("margin-bottom","0");
            dacMethods.add(dacTitle);

            assignOwner = new Button("Assign Owner");
            assignOwner.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            assignOwner.addClickListener((buttonClickEvent) -> {
                assignOwner();
            });

            dacMethods.add(assignOwner);




            // rbac things
            H2 rbacTitle = new H2("RBAC Methods:");
            rbacTitle.getStyle().set("margin-bottom","0");
            rbacMethods.add(rbacTitle);


        }
        public void refreshComponent() {
//            assignOwner.setEnabled(g.dacConfigured && g.rbacConfigured);
        }
    }
    private class POSArea extends VerticalLayout {

        public POSArea() {
            setPadding(false);
            setMargin(false);
            setWidthFull();
//            setAlignItems(Alignment.CENTER);
            setAlignItems(Alignment.STRETCH);
            setJustifyContentMode(JustifyContentMode.START);

            H2 importTitle = new H2("POS:");
            importTitle.getStyle().set("margin-bottom","0");
            add(importTitle);
        }

        public void refreshComponent() {
        }
    }

    private void assignOwner() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();

        // getting U and O nodes for target select
        List<Node> nodeCollection = new ArrayList<>();;
        try {
            nodeCollection.addAll(g.getActiveNodes());
        } catch (PMException e) {
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }

        // targets selector
        MultiselectComboBox<Node> targetsSelect = new MultiselectComboBox<>();
        targetsSelect.setRequiredIndicatorVisible(true);
        targetsSelect.setLabel("Targets");
        targetsSelect.setPlaceholder("Select O or U...");
        targetsSelect.setItemLabelGenerator(Node::getName);
//        targetsSelect.setEnabled(false);
//        List<Node> targetOptions = new ArrayList<>(nodeCollection);
//        targetOptions.removeIf(curr -> !(curr.getType() == NodeType.O
//                || curr.getType() == NodeType.U));
//        targetsSelect.setItems(targetOptions);

        // owner select
        Select<Node> ownerSelect = new Select<>();
        ownerSelect.setLabel("Owner");
        ownerSelect.setRequiredIndicatorVisible(true);
        ownerSelect.setPlaceholder("Select U...");
        ownerSelect.setItemLabelGenerator(Node::getName);

        List<Node> ownerOptions = new ArrayList<>(nodeCollection);
        ownerOptions.removeIf(curr -> !(curr.getType() == NodeType.U));
        ownerSelect.setItems(ownerOptions);

        // update the targets selector without things assigned to
        ownerSelect.addValueChangeListener((valueChangeEvent -> {
            List<Node> targetOptions = new ArrayList<>(nodeCollection);

            // remove everything except objects and users and self
            targetOptions.removeIf(curr -> !(curr.getType() == NodeType.O
                    || curr.getType() == NodeType.U)
                    || curr.equals(valueChangeEvent.getValue()));

            // remove all of the delegation nodes
            try {
                Set<String> ownerAssignees = g.getAssignees(valueChangeEvent.getValue().getName());
                targetOptions.removeIf((node) -> ownerAssignees.contains(node.getName()));
            } catch (PMException e) {
                e.printStackTrace();
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            }

//            targetsSelect.setEnabled(true);
            targetsSelect.setItems(targetOptions);
        }));

        // adding form elements (out of order of initialization)
        form.add(ownerSelect, targetsSelect);


        // ----- Title Section -----
        Button button = new Button("Submit", event -> {
            Node owner = ownerSelect.getValue();
            Set<Node> targets = targetsSelect.getSelectedItems();
            if (owner == null) {
                ownerSelect.focus();
                MainView.notify("Owner is Required", MainView.NotificationType.DEFAULT);
            } else if (targets == null || targets.isEmpty()) {
                //targetsSelect.focus();
                MainView.notify("Target(s) is(are) Required", MainView.NotificationType.DEFAULT);
            } else {
                try {
                    String[] targetNames = new String[targets.size()];
                    Iterator<Node> targetsIterator = targets.iterator();
                    for (int i = 0; targetsIterator.hasNext(); i++) {
                        targetNames[i] = targetsIterator.next().getName();
                    }

                    g.assignOwner(owner.getName(), targetNames);

                    MainView.notify("Targets have been assigned to " + owner.getName() + ".",
                            MainView.NotificationType.SUCCESS);

                    dialog.close();
                } catch (PMException e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
            }
        });
        if (nodeCollection.size() == 0) {
            button.setEnabled(false);
        }
        HorizontalLayout titleLayout = TitleFactory.generate("Assign Owner", button);

        dialog.add(titleLayout, new Hr(), form);
        dialog.open();
        ownerSelect.focus();
    }
}