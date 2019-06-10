package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;

import java.util.*;

@Tag("graph-editor")
public class GraphEditor extends VerticalLayout {
    private SingletonGraph g;
    private HorizontalLayout layout;
    private NodeLayout childNode;
    private Node selectedChildNode;
    private Node selectedParentNode;
    private NodeLayout parentNode;
    private GraphButtonGroup buttonGroup;

    public GraphEditor() {
        g = SingletonGraph.getInstance();
        layout = new HorizontalLayout();
        layout.setFlexGrow(1.0);
        add(layout);
        setUpLayout();
    }

    private void setUpLayout() {
        setSizeFull();
        setPadding(true);

        childNode = new NodeLayout(true);
        childNode.setWidth("40%");
        childNode.getStyle().set("height","100vh");
        layout.add(childNode);

        parentNode = new NodeLayout(false);
        parentNode.setWidth("40%");
        parentNode.getStyle().set("height","100vh");
        layout.add(parentNode);

        buttonGroup = new GraphButtonGroup();
        layout.add(buttonGroup);
    }

    private class NodeLayout extends VerticalLayout {
        private Grid<Node> grid;
        private Button backButton;
        private Stack<Collection<Node>> prevNodes; // Contains the nodes for going 'back'
        private Stack<String> prevNodeNames; // Contains the String for going 'back'
        private Collection<Node> currNodes; // The current nodes in the grid
        private H3 currNodeName; // The current node whose children are being shown
        // for node info section
        private H4 name;
        private Paragraph children, parents;
        private boolean isSource;

        public NodeLayout(boolean isSource) {
            this.isSource = isSource;
            if (isSource) {
                getStyle().set("background", "lightblue");
            } else {
                getStyle().set("background", "lightcoral");
            }

            ///// Title Section (Title, Back Button, Current Parent Node) //////
            // title layout config
            HorizontalLayout title = new HorizontalLayout();
            title.setAlignItems(Alignment.BASELINE);
            title.setWidthFull();
            title.setJustifyContentMode(JustifyContentMode.START);
            add(title);

            // title text
            if (isSource) {
                title.add(new H2("Source:"));
            } else {
                title.add(new H2("Destination:"));
            }

            // back button
            backButton = new Button(new Icon(VaadinIcon.ARROW_BACKWARD));
            backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            backButton.addClickListener(evt -> {
                if (!prevNodes.empty()) {
                    currNodes = prevNodes.pop();
                    grid.setItems(currNodes);
                    grid.deselectAll();
                    if (isSource) {
                        selectedChildNode = null;
                    } else {
                        selectedParentNode = null;
                    }
                    buttonGroup.refreshNodeTexts();
                    buttonGroup.refreshButtonStates();
                    updateNodeInfoSection();
                }

                if (prevNodes.empty()) {
                    backButton.setEnabled(false);
                }

                if (!prevNodeNames.empty()) {
                    currNodeName.setText(prevNodeNames.pop());
                }

            });
            backButton.setEnabled(false);
            title.add(backButton);

            // current parent node whose children are being shown
            currNodeName = new H3("Root");
            title.add(currNodeName);
            ///// End of Title Section //////


            prevNodes = new Stack<>(); // for the navigation system
            prevNodeNames = new Stack<>(); //  for the navigation system

            ////// Node Grid Section //////
            // grid config
            grid = new Grid<>(Node.class);
            grid.getStyle()
                    .set("border-radius", "2px");
            createContextMenu(); // adds the content-specific context menu
            currNodes = g.getNodes();
            grid.setItems(currNodes);
            grid.setColumnReorderingAllowed(true);
            grid.getColumns().forEach(col -> {
                col.setFlexGrow(1);
            });
            grid.getColumnByKey("ID").setWidth("15%");

            // Double Click Action: go into current node's children
            grid.addItemDoubleClickListener(evt -> {
                Node n = evt.getItem();
                if(n != null) {
                    try {
                        Set<Long> childrenIDs = g.getChildren(n.getID());
                        if (!childrenIDs.isEmpty()) {
                            ArrayList<Node> nodes = new ArrayList<>();
                            Iterator<Long> idIter = childrenIDs.iterator();
                            while(idIter.hasNext()) {
                                nodes.add(g.getNode(idIter.next()));
                            }
                            prevNodes.push(currNodes);
                            currNodes = nodes;
                            grid.setItems(currNodes);

                            prevNodeNames.push(currNodeName.getText());
                            currNodeName.setText(n.getName());
                            updateNodeInfoSection();

                            backButton.setEnabled(true);
                        } else {
                            GraphEditor.this.notify("Node has no children");
                        }
                    } catch (PMException e) {
                        e.printStackTrace();
                    }
                }

            });

            // Single Click Action: select node
            grid.addItemClickListener(evt -> {
                try {
                    if (isSource) {
                        selectedChildNode = grid.getSelectedItems().iterator().next();
                    } else {
                        selectedParentNode = grid.getSelectedItems().iterator().next();
                    }

                } catch (NoSuchElementException e) {
                    if (isSource) {
                        selectedChildNode = null;
                    } else {
                        selectedParentNode = null;
                    }
                }
                buttonGroup.refreshButtonStates();
                buttonGroup.refreshNodeTexts();
                updateNodeInfoSection();
            });
            add(grid);
            ////// End of Node Grid Section //////

            //TODO: Make it look prettier
            ////// Node Info Section //////
            VerticalLayout nodeInfo = new VerticalLayout();
            nodeInfo.setWidthFull();
            nodeInfo.setHeight("30%");
            nodeInfo.getStyle()
                    .set("background", "white")
                    .set("border-radius", "2px")
                    .set("border", "1px solid lightgrey")
                    .set("padding", "10px")
                    .set("line-height", "1px");

            name = new H4("X");
            nodeInfo.add(name);

            nodeInfo.add(new Paragraph("Children: "));
            children = new Paragraph("        None");
            nodeInfo.add(children);

            nodeInfo.add(new Paragraph("Parents: "));
            parents = new Paragraph("        None");
            nodeInfo.add(parents);

            add(nodeInfo);
            ////// End Node Info Section //////
        }

        private void updateNodeInfoSection() {
            Node gridSelecNode;
            if (isSource) {
                gridSelecNode = selectedChildNode;
            } else {
                gridSelecNode = selectedParentNode;
            }

            if (gridSelecNode != null) {
                try {
                    name.setText(gridSelecNode.getName());

                    //TODO: find a more expandable way to do this
                    children.setText("");
                    Iterator<Long> childIdIter = g.getChildren(gridSelecNode.getID()).iterator();
                    if (!childIdIter.hasNext()) {
                        children.setText("None");
                    } else {
                        while (childIdIter.hasNext()) {
                            long id = childIdIter.next();
                            children.setText(children.getText() + "{" + id + ": " + g.getNode(id).getName() + "},");
                        }
                    }

                    parents.setText("");
                    Iterator<Long> parentIdIter = g.getParents(gridSelecNode.getID()).iterator();
                    if (!parentIdIter.hasNext()) {
                        parents.setText("None");
                    } else {
                        while (parentIdIter.hasNext()) {
                            long id = parentIdIter.next();
                            parents.setText(parents.getText() + "{" + id + ": " + g.getNode(id).getName() + "},");
                        }
                    }
                } catch (PMException e) {
                    e.printStackTrace();
                    GraphEditor.this.notify(e.getMessage());
                }
            } else {
                name.setText("X");
                children.setText("None");
                parents.setText("None");
            }
        }

        public void refreshGraph() {
            currNodes = g.getNodes();
            grid.setItems(currNodes);
            grid.deselectAll();
            selectedParentNode = null;
            buttonGroup.refreshNodeTexts();
            buttonGroup.refreshButtonStates();
            backButton.setEnabled(false);
        }

        private void createContextMenu() {
            GridContextMenu<Node> contextMenu = new GridContextMenu<>(grid);

            contextMenu.addItem("Add Node", event -> addNode());
            contextMenu.addItem("Edit Node", event -> {
                event.getItem().ifPresent(node -> {
                    editNode(node);
                });
            });
            contextMenu.addItem("Delete Node", event -> {
                event.getItem().ifPresent(node -> {
                    deleteNode(node);
                });
            });
        }
    }

    private class GraphButtonGroup extends VerticalLayout {
        private Button addNodeButton,
                    addAssignmentButton, deleteAssignmentButton,
                    addAssociationButton, editAssociationButton, deleteAssociationButton;
        private H4 parentNodeText, childNodeText;
        private Component connectorSymbol;
        public GraphButtonGroup() {
            getStyle().set("background", "#a0ffa0");
            setWidth("20%");
            getStyle().set("height","100vh");
            setAlignItems(Alignment.CENTER);
            setJustifyContentMode(JustifyContentMode.START);

            childNodeText = new H4("X");
            connectorSymbol = new Icon(VaadinIcon.ARROW_DOWN);
            parentNodeText = new H4("X");

            add(new Paragraph("\n"));
            add(parentNodeText, connectorSymbol, childNodeText);
            add(new Paragraph("\n"), new Paragraph("\n"), new Paragraph("\n"));

            createButtons();
        }

        private void createButtons() {
            // Node Buttons
            addNodeButton = new Button("Add Node", evt -> {
                addNode();
            });
            addNodeButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            addNodeButton.setWidthFull();
            add(addNodeButton);
//            Button editNodeButton = new Button("Edit Node", evt -> {
//                editNode();
//            });
//            editNodeButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
//            add(editNodeButton);
//            Button deleteNodeButton = new Button("Delete Node", evt -> {
//                deleteNode();
//            });
//            deleteNodeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
//            add(deleteNodeButton);
            add(new Paragraph("\n"));



            // Assignment Buttons
            addAssignmentButton = new Button("Add Assignment", evt -> {
                if (selectedChildNode != null && selectedParentNode != null) {
                    addAssignment(selectedChildNode, selectedParentNode);
                    childNode.updateNodeInfoSection();
                    parentNode.updateNodeInfoSection();
                } else {
                    GraphEditor.this.notify("");
                }
            });
            addAssignmentButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            addAssignmentButton.setEnabled(false);
            addAssignmentButton.setWidthFull();
            add(addAssignmentButton);

            deleteAssignmentButton = new Button("Delete Assignment", evt -> {
                deleteAssignment(selectedChildNode, selectedParentNode);
                childNode.updateNodeInfoSection();
                parentNode.updateNodeInfoSection();
            });
            deleteAssignmentButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteAssignmentButton.setEnabled(false);
            deleteAssignmentButton.setWidthFull();
            add(deleteAssignmentButton);
            add(new Paragraph("\n"));



            // Association Buttons
            addAssociationButton = new Button("Add Association", evt -> {
                addAssociation();
            });
            addAssociationButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            addAssociationButton.setEnabled(false);
            addAssociationButton.setWidthFull();
            add(addAssociationButton);

            editAssociationButton = new Button("Edit Association", evt -> {
                editAssociation();
            });
            editAssociationButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            editAssociationButton.setEnabled(false);
            editAssociationButton.setWidthFull();
            add(editAssociationButton);

            deleteAssociationButton = new Button("Delete Association", evt -> {
                deleteAssociation();
            });
            deleteAssociationButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteAssociationButton.setEnabled(false);
            deleteAssociationButton.setWidthFull();
            add(deleteAssociationButton);
            add(new Paragraph("\n"));
        }

        public void refreshNodeTexts() {
            if (selectedParentNode != null) {
//                parentNodeText = new H4(selectedParentNode.getName());
                parentNodeText.setText(selectedParentNode.getName());
            } else {
//                parentNodeText = new Icon(VaadinIcon.CLOSE);
                parentNodeText.setText("X");
            }

            if (selectedChildNode != null) {
//                childNodeText = new H4(selectedChildNode.getName());
                childNodeText.setText(selectedChildNode.getName());
            } else {
//                childNodeText = new Icon(VaadinIcon.CLOSE);
                childNodeText.setText("X");
            }
        }

        public void refreshButtonStates() {
            if (selectedChildNode != null && selectedParentNode != null) {
                addAssignmentButton.setEnabled(true);
                deleteAssignmentButton.setEnabled(true);
                NodeType childType = selectedChildNode.getType();
                NodeType parentType = selectedParentNode.getType();
                if ((childType == NodeType.O || childType == NodeType.OA) && (parentType == NodeType.U || parentType == NodeType.UA)) {
                    addAssociationButton.setEnabled(true);
                    editAssociationButton.setEnabled(true);
                    deleteAssociationButton.setEnabled(true);
                } else {
                    addAssociationButton.setEnabled(false);
                    editAssociationButton.setEnabled(false);
                    deleteAssociationButton.setEnabled(false);
                }
            } else {
                addAssignmentButton.setEnabled(false);
                deleteAssignmentButton.setEnabled(false);
                addAssociationButton.setEnabled(false);
                editAssociationButton.setEnabled(false);
                deleteAssociationButton.setEnabled(false);
            }
        }
    }

    private void addNode() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);

        NumberField idField = new NumberField("ID");
        idField.setRequiredIndicatorVisible(true);
        idField.setValue(g.getNextID() * 1.0);
        idField.setMin(1);
        idField.setHasControls(true);
        form.add(idField);

        TextField nameField = new TextField("Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setPlaceholder("Enter Name...");
        form.add(nameField);

        Select<NodeType> typeSelect = new Select<>(NodeType.values());
        typeSelect.setRequiredIndicatorVisible(true);
        typeSelect.setLabel("Type");
        typeSelect.setPlaceholder("Select Type...");
        form.add(typeSelect);

        TextArea propsFeild = new TextArea("Properties (key=value \\n...)");
        propsFeild.setPlaceholder("Enter Properties...");
        form.add(propsFeild);

        Button button = new Button("Submit", event -> {
            Long id = idField.getValue().longValue();
            String name = nameField.getValue();
            NodeType type = typeSelect.getValue();
            String propString = propsFeild.getValue();
            Map<String, String> props = new HashMap<>();
            if (id == null) {
                idField.focus();
                notify("ID is Required");
            } else if (name == null || name == "") {
                nameField.focus();
                notify("Name is Required");
            } else if (type == null) {
                typeSelect.focus();
                notify("Type is Required");
            } else {
                if (propString != null && !propString.equals("")) {
                    try {
                        for (String prop : propString.split("\n")) {
                            props.put(prop.split("=")[0], prop.split("=")[1]);
                        }
                    } catch (Exception e) {
                        notify("Incorrect Formatting of Properties");
                        e.printStackTrace();
                    }
                }
                try {
//                    System.out.println(props);
                    g.createNode(id, name, type, props);
                    childNode.refreshGraph();
                    parentNode.refreshGraph();
                    dialog.close();
                } catch (IllegalArgumentException e) {
                    notify(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        form.add(button);

        dialog.add(form);
        dialog.open();
        nameField.focus();
    }

    private void editNode(Node n) {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);

        NumberField idField = new NumberField("ID");
        idField.setRequiredIndicatorVisible(true);
        idField.setValue(((double) n.getID()));
        idField.setMin(1);
        idField.setHasControls(true);
        form.add(idField);

        TextField nameField = new TextField("Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setValue(n.getName());
        form.add(nameField);

        TextArea propsFeild = new TextArea("Properties (key=value \\n...)");
        propsFeild.setPlaceholder("Enter Properties...");
        String pStr = n.getProperties().toString().replaceAll(", ", "\n");
        propsFeild.setValue(pStr.substring(1,pStr.length()-1));
        form.add(propsFeild);

        Button button = new Button("Submit", event -> {
            Long id = idField.getValue().longValue();
            String name = nameField.getValue();
            String propString = propsFeild.getValue();
            Map<String, String> props = new HashMap<>();
            if (id == null) {
                idField.focus();
                notify("ID is Required");
            } else if (name == null || name == "") {
                nameField.focus();
                notify("Name is Required");
            } else {
                if (propString != null) {
                    try {
                        for (String prop : propString.split("\n")) {
                            props.put(prop.split("=")[0], prop.split("=")[1]);
                        }
                    } catch (Exception e) {
                        notify("Incorrect Formatting of Properties");
                        e.printStackTrace();
                    }
                }
                try {
//                    System.out.println(props);
                    g.updateNode(id, name, props);
                    childNode.refreshGraph();
                    parentNode.refreshGraph();
                    dialog.close();
                } catch (Exception e) {
                    notify(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        form.add(button);

        dialog.add(form);
        dialog.open();
    }

    private void deleteNode(Node n) {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);

        form.add(new Paragraph("Are You Sure?"));

        Button button = new Button("Delete", event -> {
            g.deleteNode(n.getID());
            childNode.refreshGraph();
            parentNode.refreshGraph();
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


    private void addAssignment(Node child, Node parent) {
        if (child != null && parent != null) {
            try {
                g.assign(child.getID(), parent.getID());
                notify(child.getName() + " assigned to " + parent.getName());
            } catch (PMException e) {
                e.printStackTrace();
                notify(e.getMessage());
            }
        } else {
            notify("Must choose both a parent and a child for assignment");
        }
    }

    private void deleteAssignment(Node child, Node parent) {
        if (child != null && parent != null) {
            Dialog dialog = new Dialog();
            HorizontalLayout form = new HorizontalLayout();
            form.setAlignItems(FlexComponent.Alignment.BASELINE);

            form.add(new Paragraph("Are You Sure?"));

            Button button = new Button("Delete", event -> {
                g.deassign(child.getID(), parent.getID());
                childNode.refreshGraph();
                parentNode.refreshGraph();
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
        } else {
            notify("Must choose both a parent and a child for de-assignment");
        }
    }


    private void addAssociation() {
        notify("add association");
    }

    private void editAssociation() {
        notify("edit association");
    }

    private void deleteAssociation() {
        notify("delete association");
    }


    public void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
    }

}
