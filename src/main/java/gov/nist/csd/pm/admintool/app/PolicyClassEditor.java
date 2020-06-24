package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.admintool.graph.SingletonGraph.PolicyClassWithActive;
import gov.nist.csd.pm.exceptions.PMException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PolicyClassEditor extends VerticalLayout {
    private SingletonGraph g;
//    private HorizontalLayout layout;
    private PolicyClassViewer policyClassViewer;

    public PolicyClassEditor() {
        g = SingletonGraph.getInstance();
        setFlexGrow(1.0);
        setSizeFull();
        setPadding(false);
        setUpLayout();
    }

    private void setUpLayout() {
        policyClassViewer = new PolicyClassViewer();
        policyClassViewer.setWidthFull();
        policyClassViewer.getStyle().set("height","65vh");
        add(policyClassViewer);
    }

    private class PolicyClassViewer extends VerticalLayout {
        private Grid<PolicyClassWithActive> policyClassWithActiveGrid;

        public PolicyClassViewer () {
            getStyle().set("background", "lightblue");
            setAlignItems(Alignment.STRETCH);
            setPadding(true);

            add(new H2("Policy Class Editor:"));

            policyClassWithActiveGrid = new Grid<>(PolicyClassWithActive.class);
            policyClassWithActiveGrid.getStyle()
                    .set("border-radius", "2px");
            policyClassWithActiveGrid.setColumnReorderingAllowed(true);
            policyClassWithActiveGrid.getColumns().forEach(col -> {
                col.setFlexGrow(1);
            });
            policyClassWithActiveGrid.removeAllColumns();
            policyClassWithActiveGrid.addColumn(PolicyClassWithActive::getName).setHeader("Policy Class Name").setSortable(true);
            policyClassWithActiveGrid.addColumn(PolicyClassWithActive::isActive).setHeader("Is Active?").setSortable(true);
            add(policyClassWithActiveGrid);
            createContextMenu();

            HorizontalLayout buttons = new HorizontalLayout();
            buttons.setWidthFull();
            add(buttons);

            Button choosePCButton = new Button("Choose Policy Classes");
            choosePCButton.setWidthFull();
            choosePCButton.addClickListener(event -> choosePolicyClasses());
            buttons.add(choosePCButton);

            Button addPCButton = new Button("Add Policy Class");
            addPCButton.setWidthFull();
            addPCButton.addClickListener(event -> addPolicyClass());
            buttons.add(addPCButton);

            refreshGrid();
        }

        public void refreshGrid() {
            Set<PolicyClassWithActive> currObls = SingletonGraph.getActivePCs();
            policyClassWithActiveGrid.setItems(currObls);
        }

        private void createContextMenu() {
            GridContextMenu<PolicyClassWithActive> contextMenu = new GridContextMenu<>(policyClassWithActiveGrid);

            contextMenu.addItem("Add", event -> addPolicyClass());
            contextMenu.addItem("Toggle", event -> {
                event.getItem().ifPresent(obli -> {
                    togglePolicyClassActive(obli);
                    refreshGrid();
                });
            });
//            contextMenu.addItem("Edit Label", event -> {
//                event.getItem().ifPresent(obli -> {
//                    editLabel(obli);
//                });
//            });
//            contextMenu.addItem("Delete", event -> {
//                event.getItem().ifPresent(obli -> {
//                    deleteObligation(obli);
//                });
//            });
        }

        private void addPolicyClass() {
            Dialog dialog = new Dialog();
            HorizontalLayout form = new HorizontalLayout();
            form.setAlignItems(FlexComponent.Alignment.BASELINE);

            TextField nameField = new TextField("PC Name");
            nameField.setRequiredIndicatorVisible(true);
            nameField.setPlaceholder("Enter Name...");
            form.add(nameField);

            TextArea propsFeild = new TextArea("Properties (key=value \\n...)");
            propsFeild.setPlaceholder("Enter Properties...");
            form.add(propsFeild);

            Button button = new Button("Submit", event -> {
                String name = nameField.getValue();
                String propString = propsFeild.getValue();
                Map<String, String> props = new HashMap<>();
                if (name == null || name == "") {
                    nameField.focus();
                    PolicyClassEditor.notify("Name is Required");
                } else {
                    if (propString != null && !propString.equals("")) {
                        try {
                            for (String prop : propString.split("\n")) {
                                props.put(prop.split("=")[0], prop.split("=")[1]);
                            }
                        } catch (Exception e) {
                            PolicyClassEditor.notify("Incorrect Formatting of Properties");
                            e.printStackTrace();
                        }
                    }
                    try {
                        //g.getPAP().getGraphPAP().createPolicyClass(name, props);
                        g.createPolicyClass(name,props);
                        refreshGrid();
                        dialog.close();
                    } catch (PMException e) {
                        //PolicyClassEditor.notify(e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            form.add(button);

            dialog.add(form);
            dialog.open();
            nameField.focus();
        }

        private void togglePolicyClassActive(PolicyClassWithActive pcwa) {
            pcwa.setActive(!pcwa.isActive());
            refreshGrid();
        }

        private void choosePolicyClasses() {
            Dialog dialog = new Dialog();
            dialog.add(new H3("Choose Active Policy Classes:"));
//            dialog.setHeight("90vh");
            dialog.setWidth("50vh");

            VerticalLayout form = new VerticalLayout();
            form.setSizeFull();
            form.setAlignItems(FlexComponent.Alignment.BASELINE);
            form.getStyle().set("background", "#DADADA");
            form.getStyle().set("border-radius", "3px");

            Set<PolicyClassWithActive> activePCs = SingletonGraph.getActivePCs();
            for (PolicyClassWithActive pc: activePCs) {
                Checkbox checkbox = new Checkbox(pc.getName());
                checkbox.setWidthFull();
                checkbox.getStyle().set("border-radius", "3px");
                checkbox.setValue(pc.isActive());
                if (checkbox.getValue()) {
                    checkbox.getStyle().set("background", "lightblue");
                } else {
                    checkbox.getStyle().set("background", "lightcoral");
                }
                checkbox.addValueChangeListener(event -> {
                    activePCs.remove(pc);
                    activePCs.add(pc.setActive(event.getValue()));
                    if (event.getValue()) {
                        checkbox.getStyle().set("background", "lightblue");
                    } else {
                        checkbox.getStyle().set("background", "lightcoral");
                    }
                    refreshGrid();
                });
                form.add(checkbox);
            }

            dialog.add(form);
            dialog.open();
        }
    }

    private static void notify (String message) {
        Notification notif = new Notification(message, 3000);
        notif.open();
    }
}
