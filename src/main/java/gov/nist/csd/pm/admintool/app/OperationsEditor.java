package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.operations.Operations;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class OperationsEditor extends VerticalLayout {

    private SingletonGraph g;
    private HorizontalLayout layout;
    private OperationsViewer resourcesLayout;
    private OperationsViewer adminLayout;

    public OperationsEditor() {
        g = SingletonGraph.getInstance();
        layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setFlexGrow(1.0);
        add(layout);
        setUpLayout();
    }

    private void setUpLayout(){
        setSizeFull();
        setPadding(false);

        resourcesLayout = new OperationsViewer(true);
        resourcesLayout.setWidth("50%");
        resourcesLayout.getStyle().set("height","100vh");
        layout.add(resourcesLayout);

        adminLayout = new OperationsViewer(false);
        adminLayout.setWidth("50%");
        adminLayout.getStyle().set("height","100vh");
        layout.add(adminLayout);
    }

    public static class OperationsViewer extends VerticalLayout {
        private Grid<String> grid;
        private boolean isResourceOps;
        public static Collection<String> resourcesOpsList, adminOpsList;

        public static Collection<String> getResourcesOpsList() {
            return resourcesOpsList;
        }

        public void setResourcesOpsList(Collection<String> resourcesOpsList) {
            OperationsViewer.resourcesOpsList = resourcesOpsList;
        }

        public static Collection<String> getAdminOpsList() {
            return adminOpsList;
        }

        public void setAdminOpsList(Collection<String> adminOpsList) {
            this.adminOpsList = adminOpsList;
        }


        public OperationsViewer(boolean isResourceOps) {
            this.isResourceOps = isResourceOps;

            if (isResourceOps) {
                getStyle().set("background", "lightblue");
            } else {
                getStyle().set("background", "lightcoral");
            }

            HorizontalLayout title = new HorizontalLayout();
            title.setAlignItems(Alignment.BASELINE);
            title.setWidthFull();
            title.setJustifyContentMode(JustifyContentMode.START);
            add(title);

            if (isResourceOps) {
                title.add(new H2("Resources Operations:"));
            } else {
                title.add(new H2("Admin Operations:"));
            }

            grid = new Grid<>(String.class);
            createContextMenu();
            grid.getStyle()
                    .set("border-radius", "1px")
                    .set("user-select", "none");
            Collection<String> resourceOpsList = new ArrayList<>();
            Collection<String> adminOpsList = new ArrayList<>(Operations.ADMIN_OPS);
            adminOpsList.add(Operations.ALL_ADMIN_OPS);
            resourceOpsList.add(Operations.ALL_OPS);
            resourceOpsList.add(Operations.ALL_RESOURCE_OPS);
            resourceOpsList.add(Operations.READ);
            resourceOpsList.add(Operations.WRITE);
            resourceOpsList.add(Operations.OBJECT_ACCESS);

            OperationsViewer.adminOpsList = adminOpsList;
            OperationsViewer.resourcesOpsList = resourceOpsList;

            grid.removeAllColumns();
            grid.addColumn(String::toString).setHeader("Existing Operations");

            //grid.setSelectionMode(Grid.SelectionMode.MULTI);
            if (isResourceOps) {
                grid.setItems(resourcesOpsList);
            } else {
                grid.setItems(OperationsViewer.adminOpsList);
            }

            add(grid);
        }

        private void createContextMenu() {
            GridContextMenu<String> contextMenu = new GridContextMenu<>(grid);
            contextMenu.addItem("Add Operation", event -> {
                    addOperation();
            });

            contextMenu.addItem("Edit Operation", event -> {
                event.getItem().ifPresent(ops -> {
                    editOperation(ops);
                });
            });
            contextMenu.addItem("Delete Operation", event -> {
                event.getItem().ifPresent(ops -> {
                    deleteOperation(ops);
                });
            });
        }

        private void deleteOperation(String ops) {
            Dialog dialog = new Dialog();
            HorizontalLayout form = new HorizontalLayout();
            form.setAlignItems(Alignment.BASELINE);

            form.add(new Paragraph("Are You Sure?"));
            Button button = new Button("Delete", event -> {
                //remove op
                if (isResourceOps) {
                    resourcesOpsList.remove(ops);
                } else {
                    adminOpsList.remove(ops);
                }
                    refreshGrid(resourcesOpsList, adminOpsList);
                dialog.close();
            });
            button.addThemeVariants(ButtonVariant.LUMO_ERROR);
            form.add(button);

            Button cancel = new Button("Cancel", event -> {
                dialog.close();
            });
            cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            form.add(cancel);
            dialog.add(form);
            dialog.open();
        }

        private void editOperation(String ops) {
            Dialog dialog = new Dialog();
            dialog.open();
            HorizontalLayout form = new HorizontalLayout();
            form.setAlignItems(Alignment.BASELINE);

            TextField nameField = new TextField("Operation");
            nameField.setRequiredIndicatorVisible(true);
            nameField.setValue(ops);
            form.add(nameField);

            Button button = new Button("Submit", event -> {
                String operation = nameField.getValue();
                if (isResourceOps){
                    resourcesOpsList
                            .forEach(op -> {
                                if (op.equalsIgnoreCase(ops)) {
                                    op = operation;
                                }
                            });
                } else {
                    adminOpsList
                            .forEach(op -> {
                                if (op.equalsIgnoreCase(ops)) {
                                    op = operation;
                                }
                            });
                }
                refreshGrid(resourcesOpsList, adminOpsList);
                dialog.close();
            });
            form.add(button);
            dialog.add(form);
            }

        private void addOperation() {
            Dialog dialog = new Dialog();
            HorizontalLayout form = new HorizontalLayout();
            form.setAlignItems(Alignment.BASELINE);
            dialog.open();

            TextField nameField = new TextField("Operation");
            nameField.setRequiredIndicatorVisible(true);
            nameField.focus();
            form.add(nameField);

            Button button = new Button("Submit", event -> {
                String operation = nameField.getValue();
                if (isResourceOps) {
                    resourcesOpsList.add(operation);
                } else {
                    adminOpsList.add(operation);
                }
                refreshGrid(resourcesOpsList, adminOpsList);
                dialog.close();
            });
            form.add(button);

            dialog.add(form);
        }

        private void refreshGrid(Collection<String> resources, Collection<String> admins) {
            if (isResourceOps) {
                final ListDataProvider<String> dataProvider = DataProvider.ofCollection(resources);
                grid.setDataProvider(dataProvider);
            }else {
                final ListDataProvider<String> dataProvider = DataProvider.ofCollection(admins);
                grid.setDataProvider(dataProvider);
            }
        }
    }
}
