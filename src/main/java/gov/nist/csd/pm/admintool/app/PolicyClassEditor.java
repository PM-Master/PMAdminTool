package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import gov.nist.csd.pm.admintool.app.customElements.MapInput;
import gov.nist.csd.pm.admintool.graph.SingletonClient;
import gov.nist.csd.pm.admintool.graph.SingletonClient.PolicyClassWithActive;

import java.util.Map;
import java.util.Set;

public class PolicyClassEditor extends VerticalLayout {
    private SingletonClient g;
//    private HorizontalLayout layout;
    private PolicyClassViewer policyClassViewer;

    public PolicyClassEditor() {
        g = SingletonClient.getInstance();
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

            Button choosePCButton = new Button("Choose Active Policy Classes");
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
            Set<PolicyClassWithActive> currObls = SingletonClient.getActivePCs();
            System.out.println(g.toString());
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
        }

        private void addPolicyClass() {
            Dialog dialog = new Dialog();
            HorizontalLayout form = new HorizontalLayout();

            TextField nameField = new TextField("PC Name");
            nameField.setRequiredIndicatorVisible(true);
            nameField.setPlaceholder("Enter Name...");
            form.add(nameField);

            MapInput<String, String> propsField = new MapInput<>(
                    TextField.class, TextField.class,
                    null, null,
                    (keyFieldInstance) -> {
                        if (keyFieldInstance instanceof TextField) {
                            TextField temp = (TextField) keyFieldInstance;
                            String value = temp.getValue();
                            return (value != null && !value.isEmpty()) ? value : null;
                        } else {
                            MainView.notify("Not an instance of a TextField", MainView.NotificationType.ERROR);
                            return null;
                        }
                    }, null
            );
            propsField.setLabel("Properties");
            form.add(propsField);

            Button button = new Button("Submit", event -> {
                String name = nameField.getValue();
                try {
                    Map<String, String> props = propsField.getValue();
                    if (name == null || name == "") {
                        nameField.focus();
                        MainView.notify("Name is Required", MainView.NotificationType.DEFAULT);
                    } else {
                        //g.getPAP().getGraphPAP().createPolicyClass(name, props);
                        g.createPolicyClass(name, props);
                        MainView.notify("Policy Class with name: " + name + " has been created.", MainView.NotificationType.SUCCESS);
                        refreshGrid();
                        dialog.close();
                    }
                } catch (Exception e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
            });
            HorizontalLayout titleLayout = TitleFactory.generate(
                    "Add Policy Class",
                    "Make's a new PC with Default UA and OA",
                    button);


            dialog.add(titleLayout, new Hr(), form);
            dialog.open();
            nameField.focus();
        }

        private void togglePolicyClassActive(PolicyClassWithActive pcwa) {
            pcwa.setActive(!pcwa.isActive());
            SingletonClient.getActivePCs().forEach(pcs -> {
                if (pcs.getName().equalsIgnoreCase(pcwa.getName())) {
                    pcs.setActive(pcwa.isActive());
                }
            });
            refreshGrid();
        }

        private void choosePolicyClasses() {
            Dialog dialog = new Dialog();
//            dialog.add(new H3("Choose Active Policy Classes:"));
//            dialog.setHeight("90vh");
            dialog.setWidth("50vh");

            VerticalLayout form = new VerticalLayout();
//            form.setSizeFull();
            form.setAlignItems(FlexComponent.Alignment.BASELINE);
            form.setPadding(false);

            Set<PolicyClassWithActive> activePCs = SingletonClient.getActivePCs();
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

            HorizontalLayout titleLayout = TitleFactory.generate(
                    "Choose Active Policy Classes");

            dialog.add(titleLayout, new Hr(), form);
            dialog.open();
        }
    }
}
