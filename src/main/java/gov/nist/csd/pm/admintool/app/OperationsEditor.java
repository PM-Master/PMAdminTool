package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import gov.nist.csd.pm.admintool.graph.SingletonClient;
import gov.nist.csd.pm.exceptions.PMException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OperationsEditor extends VerticalLayout {

    private SingletonClient g;
    private HorizontalLayout layout;
    private OperationsViewer resourcesLayout;
    private OperationsViewer adminLayout;

    public OperationsEditor() {
        g = SingletonClient.getInstance();
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

    public class OperationsViewer extends VerticalLayout {
        private boolean isResourceOps;
        private final Map<String, Predicate<? super String>> filters = new HashMap<>();

        private TextField searchBar;
        private Grid<String> grid;


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
                title.add(new H2("Resources Access Rights:"));
            } else {
                title.add(new H2("Admin Access Rights:"));
            }

            // adding the search bar
            searchBar = new TextField();
            searchBar.setWidthFull();
            searchBar.setPlaceholder("Search by name...");
            searchBar.setClearButtonVisible(true);
            searchBar.setValueChangeMode(ValueChangeMode.LAZY);
            searchBar.addValueChangeListener(evt -> {
                if (evt.getValue() != null && !evt.getValue().isEmpty()) {
                    Predicate<? super String> filterName = (nodeName -> nodeName.contains(evt.getValue()));
                    filters.put("Name", filterName);
                } else {
                    filters.remove("Name");
                }
                updateGridNodes();
            });
            add(searchBar);


            // adding the grid section
            grid = new Grid<>(String.class);
            createContextMenu();
            grid.getStyle()
                    .set("border-radius", "1px")
                    .set("user-select", "none");

            grid.removeAllColumns();
            grid.addColumn(String::toString)
                    .setHeader("Existing Access Rights")
                    .setSortable(true);

//            grid.setSelectionMode(Grid.SelectionMode.MULTI);

            updateGridNodes();
            add(grid);
        }

        private void createContextMenu() {
            GridContextMenu<String> contextMenu = new GridContextMenu<>(grid);
            contextMenu.addItem("Add Access Right", event -> {
                    addOperation();
            });
//            contextMenu.addItem("Edit Operation", event -> {
//                event.getItem().ifPresent(op -> {
//                    editOperation(op);
//                });
//            });
            contextMenu.addItem("Delete Access Right", event -> {
                event.getItem().ifPresent(op -> {
                    deleteOperation(op);
                });
            });
        }

        private void updateGridNodes() {
            Collection<String> opsList = new ArrayList<>();
            try {
                if (isResourceOps) {
                    opsList.addAll(g.getResourceOps());
                } else {
                    opsList.addAll(g.getAdminOps());
                }

                // add filter functions
                for (Predicate<? super String> filter : filters.values()) {
                    opsList = opsList.stream().filter(filter).collect(Collectors.toSet());
                }
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }

            grid.setDataProvider(DataProvider.ofCollection(opsList));
        }

        public void refresh() {
            grid.getDataProvider().refreshAll();
        }
    }

    private void deleteOperation(String ops) {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(Alignment.BASELINE);

        form.add(new Paragraph("Are You Sure?"));
        Button button = new Button("Delete", event -> {
            //remove op
            try {
                g.deleteResourceOps(ops);
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            resourcesLayout.updateGridNodes();
            adminLayout.updateGridNodes();
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

        TextField nameField = new TextField("Access Right");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setValue(ops);
        form.add(nameField);

        Button button = new Button("Submit", event -> {
            String operation = nameField.getValue();
//            if (isResourceOps){
//                resourcesOpsList
//                        .forEach(op -> {
//                            if (op.equalsIgnoreCase(ops)) {
//                                op = operation;
//                            }
//                        });
//            } else {
//                adminOpsList
//                        .forEach(op -> {
//                            if (op.equalsIgnoreCase(ops)) {
//                                op = operation;
//                            }
//                        });
//            }
            resourcesLayout.updateGridNodes();
            adminLayout.updateGridNodes();
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

        TextField nameField = new TextField("Access Right");
        nameField.setRequiredIndicatorVisible(true);
        nameField.focus();
        form.add(nameField);

        Button button = new Button("Submit", event -> {
            String operation = nameField.getValue();
            try {
                g.addResourceOps(operation);
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            resourcesLayout.updateGridNodes();
            adminLayout.updateGridNodes();
            dialog.close();
        });
        form.add(button);

        dialog.add(form);
    }
}
