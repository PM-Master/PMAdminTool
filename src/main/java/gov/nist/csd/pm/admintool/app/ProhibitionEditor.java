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
import gov.nist.csd.pm.admintool.app.customElements.MapInput;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pip.prohibitions.model.Prohibition;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Tag("prohibition-editor")
public class ProhibitionEditor extends VerticalLayout {
    private SingletonGraph g;
    private HorizontalLayout layout;
    private ButtonGroup buttonGroup;
    private ProhibitionViewer prohibitionViewer;
    private Prohibition selectedProhibition;

    public ProhibitionEditor() {
        g = SingletonGraph.getInstance();
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
            grid = new Grid<>(Prohibition.class);
            setupProhibitionTableSection();
        }

        public void setupProhibitionTableSection() {
            getStyle().set("background", "lightblue");
            add(new H2("Prohibition Editor:"));

            // grid config
            grid.getStyle()
                    .set("border-radius", "1px")
                    .set("user-select", "none");
            grid.setColumns("name", "operations", "subject", "containers", "intersection");

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
            List<Prohibition> prohibitions = null;
            try {
                prohibitions = g.getAllProhibitions();
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            grid.setItems(prohibitions);
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
                selectedProhibitionText.setText(selectedProhibition.getName());
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
            nodesCol.addAll(g.getActiveNodes());
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
            OperationSet ops = new OperationSet(rOpsField.getValue());
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
                    g.addProhibition(name, subject, containers, ops, intersection);
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
            nodesCol.addAll(g.getActiveNodes());
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
        subjectSelect.setValue(prohibition.getSubject());
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
        opsField.setValue(prohibition.getOperations());

        // getting list of targets
        HashSet<String> targets = new HashSet<>();
        HashSet<Node> targetNodes = new HashSet<>(nodesCol);
        targetNodes.removeIf(curr -> !(curr.getType() == NodeType.OA || curr.getType() == NodeType.O));
        targetNodes.forEach((n) -> targets.add(n.getName()));

        // targets (+ compliment) selector
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
        containerField.setValue(prohibition.getContainers());

        // intersection checkbox
        Checkbox intersectionField = new Checkbox("Intersection");
        VerticalLayout intersectionFieldLayout = new VerticalLayout(intersectionField);
        form.add(intersectionFieldLayout);
        intersectionField.setValue(prohibition.isIntersection());

        // submit button
        Button submit = new Button("Submit", event -> {
            String name = prohibition.getName();
            String subject = subjectSelect.getValue();
            OperationSet ops = new OperationSet(opsField.getValue());
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
                    g.updateProhibition(name, subject, containers, ops, intersection);
                    prohibitionViewer.refreshGraph();
                    dialog.close();
                }
            } catch (Exception e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
        });

        // title layout
        HorizontalLayout titleLayout = TitleFactory.generate("Edit Prohibition", prohibition.getName(), submit);

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
                g.deleteProhibition(prohibition.getName());
                MainView.notify("Prohibition with name: " + prohibition.getName() + " has been deleted", MainView.NotificationType.SUCCESS);
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
                prohibition.getName());

        dialog.add(titleLayout, new Hr(), form);
        dialog.open();
    }
}

