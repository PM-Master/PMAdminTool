package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import gov.nist.csd.pm.admintool.app.customElements.MapInput;
import gov.nist.csd.pm.admintool.graph.SingletonClient;
import gov.nist.csd.pm.policy.exceptions.PMException;
import gov.nist.csd.pm.policy.model.access.AccessRightSet;
import gov.nist.csd.pm.policy.model.graph.nodes.Node;
import gov.nist.csd.pm.policy.model.graph.nodes.NodeType;
import gov.nist.csd.pm.policy.model.prohibition.ContainerCondition;
import gov.nist.csd.pm.policy.model.prohibition.Prohibition;
import gov.nist.csd.pm.policy.model.prohibition.ProhibitionSubject;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.*;

import static gov.nist.csd.pm.policy.model.prohibition.ProhibitionSubject.Type.USER_ATTRIBUTE;

@Tag("prohibition-editor")
public class ProhibitionEditor extends VerticalLayout {

    private SingletonClient g;
    private HorizontalLayout layout;
    private ButtonGroup buttonGroup;
    private ProhibitionViewer prohibitionViewer;
    private Prohibition selectedProhibition;

    public ProhibitionEditor() {
        g = SingletonClient.getInstance();
        layout = new HorizontalLayout();
        layout.setFlexGrow(1.0);
        add(layout);
        setUpLayout();
    }

    private void setUpLayout() {
        setSizeFull();
        setPadding(true);

        prohibitionViewer = new ProhibitionViewer();
        prohibitionViewer.setWidth("80%");
        prohibitionViewer.getStyle().set("height","100vh");

        buttonGroup = new ButtonGroup();
        buttonGroup.setWidth("20%");
        buttonGroup.getStyle().set("height","100vh");

        layout.add(prohibitionViewer, buttonGroup);
    }

    private class ProhibitionViewer extends VerticalLayout {
        private Grid<Prohibition> grid;

        public ProhibitionViewer() {
            grid = new Grid<>();
            setupProhibitionTableSection();
        }

        public void setupProhibitionTableSection() {
            getStyle().set("background", "lightblue");
            add(new H2("Prohibition Editor:"));

            // grid config
            grid.getStyle()
                    .set("border-radius", "1px")
                    .set("user-select", "none");
            //grid.setColumns("label", "accessRightSet", "subject", "containers", "intersection");
            grid.addColumn(Prohibition::getLabel).setHeader("label");
            grid.addColumn(Prohibition::getAccessRightSet).setHeader("accessRightSet");
            grid.addColumn(Prohibition::getSubjectName).setHeader("subject");
            grid.addColumn(Prohibition::getContainers_map).setHeader("containers");
            grid.addColumn(Prohibition::isIntersection).setHeader("intersection");

            // Single Click Action: select node
            grid.addItemClickListener(evt -> {
                selectedProhibition = grid.getSelectedItems().iterator().next();
                selectedProhibition = evt.getItem();

                if (buttonGroup != null) {
                    buttonGroup.refresh();
                }
            });

            createContextMenu(); // adds the content-specific context menu

            refreshGraph();

            add(grid);
        }

        public void updateGrid() {
            //Remove
            /*List<Prohibition> prohibitions = null;
            try {
                prohibitions = g.getAllProhibitions();
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }*/

            //Use data provider to edit prohibition
            DataProvider<Prohibition, Void> dataProvider =
                    DataProvider.fromCallbacks(
                            // First callback fetches items based on a query
                            query -> {
                                // The index of the first item to load
                                int offset = query.getOffset();

                                // The number of items to load
                                int limit = query.getLimit();

                                List<Prohibition> prohibitions = null;
                                try {
                                    prohibitions = g.getAllProhibitions();
                                } catch (PMException e) {
                                    e.printStackTrace();
                                    return null;
                                }

                                return prohibitions.stream();
                            },
                            // Second callback fetches the total number of items currently in the Grid.
                            // The grid can then use it to properly adjust the scrollbars.
                            query -> {
                                try {
                                    return g.getAllProhibitions().size();
                                } catch (PMException e) {
                                    e.printStackTrace();
                                    return 0;
                                }
                            });


            grid.setItems(dataProvider);
        }

        public void refreshGraph() {
            grid.deselectAll();
            updateGrid();
            selectedProhibition = null;
            if (buttonGroup != null) {
                buttonGroup.refresh();
            }
        }

        private void createContextMenu() {
            GridContextMenu<Prohibition> contextMenu = new GridContextMenu<>(grid);

            contextMenu.addItem("Edit Prohibition", event -> {
                event.getItem().ifPresent(prohibition -> editProhibition(prohibition));
            });
            contextMenu.addItem("Delete Prohibition", event -> {
                event.getItem().ifPresent(prohibition -> deleteProhibition(prohibition));
            });


        }
    }

    private class ButtonGroup extends VerticalLayout {
        private Button addProhibitionButton, editProhibitionButton, deleteProhibitionButton;
        private H4 selectedProhibitionText;
        public ButtonGroup() {
            getStyle().set("background", "#DADADA") //#A0FFA0
                    .set("overflow-y", "scroll");
            setAlignItems(Alignment.CENTER);
            setJustifyContentMode(JustifyContentMode.START);

            selectedProhibitionText = new H4("X");

            add(new Paragraph("\n"));
            add(selectedProhibitionText);
            add(new Paragraph("\n"), new Paragraph("\n"));

            createButtons();

            refresh();
        }

        private void createButtons() {
            // Prohibition Buttons
            addProhibitionButton = new Button("Add Prohibition", evt -> {
                addProhibition();
            });
            addProhibitionButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            addProhibitionButton.setWidthFull();
            add(addProhibitionButton);

            editProhibitionButton = new Button("Edit Prohibition", evt -> {
                editProhibition(selectedProhibition);
            });
            editProhibitionButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            editProhibitionButton.setWidthFull();
            add(editProhibitionButton);

            deleteProhibitionButton = new Button("Delete Prohibition", evt -> {
                deleteProhibition(selectedProhibition);
            });
            deleteProhibitionButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteProhibitionButton.setWidthFull();
            add(deleteProhibitionButton);
            add(new Paragraph("\n"));
        }

        public void refresh() {
            if (selectedProhibition != null) {
                selectedProhibitionText.setText(selectedProhibition.getLabel());
                editProhibitionButton.setEnabled(true);
                deleteProhibitionButton.setEnabled(true);
            } else {
                selectedProhibitionText.setText("X");
                editProhibitionButton.setEnabled(false);
                deleteProhibitionButton.setEnabled(false);
            }
        }
    }

    private void addProhibition() {
        // get all current nodes
        HashSet<Node> nodesCol = new HashSet<>();
        try {
            nodesCol.addAll(g.getNodes());
//            nodesCol.addAll(g.getActiveNodes());
        } catch (PMException e) {
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }

        // actual dialog box
        Dialog dialog = new Dialog();

        // the form layout
        HorizontalLayout form = new HorizontalLayout();

        // prohibition name input - automatically filled by subject change
        TextField nameField = new TextField("Prohibition Name");
        nameField.setPlaceholder("Name...");
        form.add(nameField);

        // getting list of subjects
        HashSet<String> subjects = new HashSet<>();
        HashSet<Node> subjectNodes = new HashSet<>(nodesCol);
        subjectNodes.removeIf(curr -> !(curr.getType() == NodeType.UA || curr.getType() == NodeType.U));
        //TODO: Remove after super_ua fix
        subjectNodes.removeIf(n -> n.getName().equals("super_ua"));

        subjectNodes.forEach((n) -> subjects.add(n.getName()));


        // subject selector
        Select<String> subjectSelect = new Select();
        subjectSelect.setItems(subjects);
        subjectSelect.setRequiredIndicatorVisible(true);
        subjectSelect.setLabel("Subject");
        subjectSelect.setPlaceholder("Subject...");
        subjectSelect.setItemLabelGenerator((nodeName) -> {
            try {
                Node node = g.getNode(nodeName);
                return node.getName() + " (" + node.getType() + ")";
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
                return "";
            }
        });
        subjectSelect.addValueChangeListener((nodeName) -> {
            int numOfProhibitionsForSubject = 0;
            try {
                numOfProhibitionsForSubject = g.getProhibitionsFor(nodeName.getValue()).size();
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            String initialName = "deny_" + nodeName.getValue() + "-" + (numOfProhibitionsForSubject + 1);
            nameField.setValue(initialName);
        });
        form.add(subjectSelect);

        // operations multi-select
        MultiselectComboBox<String> rOpsField = new MultiselectComboBox<>();
        rOpsField.setLabel("Access Rights");
        rOpsField.setPlaceholder("Resource...");
        MultiselectComboBox<String> aOpsField = new MultiselectComboBox<>();
        aOpsField.setPlaceholder("Admin...");
        try {
            rOpsField.setItems(g.getResourceOpsWithStars());
            aOpsField.setItems(g.getAdminOpsWithStars());
        } catch (PMException e) {
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }
        VerticalLayout opsFields = new VerticalLayout(rOpsField, aOpsField);
        opsFields.getStyle().set("padding-top", "0");
        form.add(opsFields);

        // getting list of targets
        HashSet<String> targets = new HashSet<>();
        HashSet<Node> targetNodes = new HashSet<>(nodesCol);
        targetNodes.removeIf(curr -> !(curr.getType() == NodeType.OA || curr.getType() == NodeType.O));
        targetNodes.forEach((n) -> targets.add(n.getName()));

        // targets (+ compliment) selector
        MapInput<String, Boolean> containerField = new MapInput<>(Select.class, Checkbox.class,
            (keyField) -> {
                if (keyField instanceof Select) {
                    Select<String> temp = (Select<String>)keyField;
                    temp.setItems(targets);
                    temp.setItemLabelGenerator((nodeName) -> {
                        Node node = null;
                        try {
                            node = g.getNode(nodeName);
                        } catch (PMException e) {
                            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                            e.printStackTrace();
                        }
                        return node.getName() + " (" + node.getType() + ")";
                    });
                    temp.setPlaceholder("Container...");
                } else {
                    MainView.notify("Not an instance of a Select", MainView.NotificationType.ERROR);
                }
            }, null,
            null, null
        );
        containerField.setLabel("Containers (Target, Complement)");
        form.add (containerField);

        // intersection checkbox
        Checkbox intersectionFeild = new Checkbox("Intersection");
        VerticalLayout intersectionFeildLayout = new VerticalLayout(intersectionFeild);
        form.add(new VerticalLayout(intersectionFeildLayout));

        // submit button
        Button submit = new Button("Submit", event -> {
            String name = nameField.getValue();
            String subject = subjectSelect.getValue();
            AccessRightSet ops = new AccessRightSet(rOpsField.getValue());
            ops.addAll(aOpsField.getValue());
            boolean intersection = intersectionFeild.getValue();
            try {
                Map<String, Boolean> containers = containerField.getValue();
                if (ops == null || ops.isEmpty()) {
                    MainView.notify("Access Rights are Required");
                } else if (name == null || name.equals("")) {
                    nameField.focus();
                    MainView.notify("Name is Required");
                } else if (subject == null || subject.equals("")) {
                    subjectSelect.focus();
                    MainView.notify("Subject is Required");
                } else if (containers.isEmpty()) {
                    MainView.notify("Containers are Required");
                } else {
                    ProhibitionSubject pSubject = new ProhibitionSubject(subject, "USER_ATTRIBUTE");
                    List<ContainerCondition> conditions = new ArrayList<>();
                    for (String node_target:containers.keySet()) {
                        conditions.add(new ContainerCondition(node_target, containers.get(node_target)));
                    }

                    //Prohibition p = new Prohibition(name,pSubject , ops, intersection, conditions);
                    g.addProhibition(name, subject, containers, ops, intersection);
                    //g.addProhibition(p);
                    prohibitionViewer.refreshGraph();
                    dialog.close();
                }
            } catch (Exception e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
        });

        // title layout
        HorizontalLayout titleLayout = TitleFactory.generate("Add Prohibition", submit);

        // putting it all together
        dialog.add(titleLayout, new Hr(), form);
        dialog.open();
        subjectSelect.focus();
    }

    private void editProhibition(Prohibition prohibition) {
        // get all current nodes
        HashSet<Node> nodesCol = new HashSet<>();
        try {
            nodesCol.addAll(g.getNodes());
//            nodesCol.addAll(g.getActiveNodes());
        } catch (PMException e) {
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }

        // actual dialog box
        Dialog dialog = new Dialog();

        // the form layout
        HorizontalLayout form = new HorizontalLayout();

        // getting list of subjects
        HashSet<String> subjects = new HashSet<>();
        HashSet<Node> subjectNodes = new HashSet<>(nodesCol);
        subjectNodes.removeIf(curr -> !(curr.getType() == NodeType.UA || curr.getType() == NodeType.U));
        //TODO: Remove filter when super_ua fixed
        subjectNodes.removeIf(curr -> curr.getName().equals("super_ua"));
        subjectNodes.forEach((n) -> subjects.add(n.getName()));

        // subject selector
        Select<String> subjectSelect = new Select();
        subjectSelect.setItems(subjects);
        subjectSelect.setRequiredIndicatorVisible(true);
        subjectSelect.setLabel("Subject");
        subjectSelect.setPlaceholder("Subject...");
        subjectSelect.setItemLabelGenerator((nodeName) -> {
            try {
                Node node = g.getNode(nodeName);
                return node.getName() + " (" + node.getType() + ")";
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
                return "";
            }
        });
        form.add(subjectSelect);
        subjectSelect.setValue(prohibition.getSubject().name());
        subjectSelect.setEnabled(false);

        // operations multi-select
        MultiselectComboBox<String> opsField = new MultiselectComboBox<>();
        opsField.setLabel("Access Rights");
        opsField.setPlaceholder("Choose...");
        try {
            opsField.setItems(g.getAllOpsWithStars());
        } catch (PMException e) {
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }
        form.add(opsField);
        opsField.setValue(prohibition.getAccessRightSet());

        // getting list of targets
        HashSet<String> targets = new HashSet<>();
        HashSet<Node> targetNodes = new HashSet<>(nodesCol);
        targetNodes.removeIf(curr -> !(curr.getType() == NodeType.OA || curr.getType() == NodeType.O));
        targetNodes.forEach((n) -> targets.add(n.getName()));

        // targets (+ complement) selector
        MapInput<String, Boolean> containerField = new MapInput<>((new Select<String>()).getClass(), Checkbox.class,
                (keyField) -> {
                    if (keyField instanceof Select) {
                        Select<String> temp = (Select<String>)keyField;
                        temp.setItems(targets);
                        temp.setItemLabelGenerator((nodeName) -> {
                            Node node = null;
                            try {
                                node = g.getNode(nodeName);
                            } catch (PMException e) {
                                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                                e.printStackTrace();
                            }
                            return node.getName() + " (" + node.getType() + ")";
                        });
                        temp.setPlaceholder("Container...");
                    } else {
                        MainView.notify("Not an instance of a TextField", MainView.NotificationType.ERROR);
                    }
                }, null,
                null, null
        );
        containerField.setLabel("Containers (Target, Complement)");
        form.add(containerField);

        Map<String, Boolean> prohibitionContainers = new HashMap<>();
        for (ContainerCondition containerCondition: prohibition.getContainers()) {
            prohibitionContainers.put(containerCondition.name(), containerCondition.complement());
        }

        containerField.setValue(prohibitionContainers);
        // intersection checkbox
        Checkbox intersectionField = new Checkbox("Intersection");
        VerticalLayout intersectionFieldLayout = new VerticalLayout(intersectionField);
        form.add(intersectionFieldLayout);
        intersectionField.setValue(prohibition.isIntersection());

        // submit button
        Button submit = new Button("Submit", event -> {
            String name = prohibition.getLabel();
            String subject = subjectSelect.getValue();
            //TODO: switch case type for subject
            ProhibitionSubject prohibitionSubject = new ProhibitionSubject(subject, USER_ATTRIBUTE);
            AccessRightSet ops = new AccessRightSet(opsField.getValue());
            boolean intersection = intersectionField.getValue();
            try {
                Map<String, Boolean> containers = containerField.getValue();
                if (ops == null || ops.isEmpty()) {
                    MainView.notify("Access Rights are Required");
                } else if (subject == null || subject.equals("")) {
                    subjectSelect.focus();
                    MainView.notify("Subject is Required");
                } else if (containers.isEmpty()) {
                    MainView.notify("Containers are Required");
                } else {
                    g.updateProhibition(name, prohibitionSubject, containers, ops, intersection);
                    prohibitionViewer.refreshGraph();
                    dialog.close();
                }
            } catch (Exception e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
        });

        // title layout
        HorizontalLayout titleLayout = TitleFactory.generate("Edit Prohibition", prohibition.getLabel(), submit);

        // putting it all together
        dialog.add(titleLayout, new Hr(), form);
        dialog.open();
        subjectSelect.focus();
    }

    private void deleteProhibition(Prohibition prohibition) {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);

        form.add(new Paragraph("Are You Sure?"));

        Button button = new Button("Delete", event -> {
            try {
                g.deleteProhibition(prohibition.getLabel());
                MainView.notify("Prohibition with name: " + prohibition.getLabel() + " has been deleted", MainView.NotificationType.SUCCESS);
                prohibitionViewer.refreshGraph();
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            dialog.close();
        });
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        form.add(button);

        Button cancel = new Button("Cancel", event -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        form.add(cancel);

        HorizontalLayout titleLayout = TitleFactory.generate("Delete Prohibtion",
                prohibition.getLabel());

        dialog.add(titleLayout, new Hr(), form);
        dialog.open();
    }
}

