package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import gov.nist.csd.pm.admintool.app.customElements.MapInput;
import gov.nist.csd.pm.admintool.graph.SingletonClient;
import gov.nist.csd.pm.policy.exceptions.PMException;
import gov.nist.csd.pm.policy.model.graph.nodes.Node;

import java.util.*;

public class PolicyClassEditor extends VerticalLayout {
    private SingletonClient g;
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
        private Grid<Node> policyClassWithActiveGrid;

        public PolicyClassViewer () {
            getStyle().set("background", "lightblue");
            setAlignItems(Alignment.STRETCH);
            setPadding(true);

            add(new H2("Policy Class Editor:"));

            policyClassWithActiveGrid = new Grid<>(Node.class);
            policyClassWithActiveGrid.getStyle()
                    .set("border-radius", "2px");
            policyClassWithActiveGrid.setColumnReorderingAllowed(true);
            policyClassWithActiveGrid.getColumns().forEach(col -> {
                col.setFlexGrow(1);
            });
            policyClassWithActiveGrid.removeAllColumns();
            policyClassWithActiveGrid.addColumn(Node::getName).setHeader("Policy Class Name").setSortable(true);
            add(policyClassWithActiveGrid);
            createContextMenu();

            HorizontalLayout buttons = new HorizontalLayout();
            buttons.setWidthFull();
            add(buttons);

            Button addPCButton = new Button("Add Policy Class");
            addPCButton.setWidthFull();
            addPCButton.addClickListener(event -> addPolicyClass());
            buttons.add(addPCButton);

            refreshGrid();
        }

        public void refreshGrid() {
            Set<Node> pcs = new HashSet<>();
            try {
                for (String pc: g.getPolicies()) {
                    pcs.add(g.getNode(pc));
                }
            } catch (PMException e) {
                e.printStackTrace();
            }

            policyClassWithActiveGrid.setItems(pcs);
        }

        private void createContextMenu() {
            GridContextMenu<Node> contextMenu = new GridContextMenu<>(policyClassWithActiveGrid);

            contextMenu.addItem("Add", event -> addPolicyClass());
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
    }
}
