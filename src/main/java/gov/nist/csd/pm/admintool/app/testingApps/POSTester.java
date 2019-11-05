package gov.nist.csd.pm.admintool.app.testingApps;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
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
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;

import java.util.*;

public class POSTester extends VerticalLayout {
    private Select<Node> userSelect;
    private Grid<Node> grid;
    private Node user;
    private SingletonGraph g;

    public POSTester () {
        setPadding(false);
        setMargin(false);
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        g = SingletonGraph.getInstance();
        userSelect = new Select<>();
        grid = new Grid<>(Node.class);

        addUserSelectForm();
        addGrid();
    }

    private void addUserSelectForm() {
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);
        form.setWidthFull();
        form.setMargin(false);

        // actual select box
        setUserSelect();
        userSelect.setRequiredIndicatorVisible(true);
        userSelect.setLabel("Choose User");
        userSelect.setPlaceholder("Select an option");
        userSelect.setEmptySelectionCaption("Select an option");
        userSelect.setEmptySelectionAllowed(true);
        userSelect.setItemEnabledProvider(Objects::nonNull);

        userSelect.addComponents(null, new Hr());
        form.add(userSelect);

        // actual submit button
        Button submit = new Button("Update POS", event -> {
            Node selectedUser = userSelect.getValue();
            if (selectedUser == null) {
                notify("User is required!");
            } else {
                user = selectedUser;
                updateGraph();
            }
        });
        form.add(submit);

        // Analyse button
        Button analyse = new Button("Analyse POS", event -> {
            Node selectedUser = userSelect.getValue();
            if (selectedUser == null) {
                notify("User is required!");
            } else {
                user = selectedUser;
//                AnalyseGraph();
            }
        });
        form.add(analyse);

        add(form);
    }

    private void addGrid() {
        // grid config
        grid.getStyle()
                .set("border-radius", "2px");

        ///// context menu /////
        GridContextMenu<Node> contextMenu = new GridContextMenu<>(grid);

//            contextMenu.addItem("Add Node", event -> addNode());
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
        ///// end of context menu /////

        grid.setItems(new HashSet<>());
        grid.setColumnReorderingAllowed(true);
        grid.getColumns().forEach(col -> {
            col.setFlexGrow(1);
        });

        grid.removeColumnByKey("ID");

        // Double Click Action: go into current node's children
//        grid.addItemDoubleClickListener(evt -> {
//            Node n = evt.getItem();
//            if(n != null) {
//                try {
//                    Set<Node> children = g.getChildren(n.getID());
//                    if (!children.isEmpty()) {
//                        prevNodes.push(currNodes);
//                        currNodes = children;
//                        grid.setItems(currNodes);
//
//                        prevNodeNames.push(currNodeName.getText());
//                        currNodeName.setText(currNodeName.getText() + " > " + n.getName());
//                        updateNodeInfoSection();
//
//                        backButton.setEnabled(true);
//                    } else {
//                        GraphEditor.this.notify("Node has no children");
//                    }
//                } catch (PMException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        });

        // Single Click Action: select node
//        grid.addItemClickListener(evt -> {
//            try {
//                if (isSource) {
//                    selectedChildNode = grid.getSelectedItems().iterator().next();
//                } else {
//                    selectedParentNode = grid.getSelectedItems().iterator().next();
//                }
//
//            } catch (NoSuchElementException e) {
//                if (isSource) {
//                    selectedChildNode = null;
//                } else {
//                    selectedParentNode = null;
//                }
//            }
//            buttonGroup.refreshButtonStates();
//            buttonGroup.refreshNodeTexts();
//            updateNodeInfoSection();
//        });
        add(grid);
    }

    public Node[] getUsers() {
        Collection<Node> nodeCollection;
        try {
            nodeCollection = new HashSet<>(g.getNodes());
        } catch (PMException e) {
            nodeCollection = new HashSet<>();
            System.out.println(e.getMessage());

            e.printStackTrace();
        }
        Iterator<Node> nodeIterator = nodeCollection.iterator();
        while (nodeIterator.hasNext()) {
            Node curr = nodeIterator.next();
            if (!(curr.getType() == NodeType.U) || curr.getProperties().get("namespace") == "super") {
                nodeIterator.remove();
            }
        }
        Node[] nodes = nodeCollection.toArray(new Node[nodeCollection.size()]);
        return nodes;
    }

    public void setUserSelect() {
        userSelect.setItems(getUsers());
    }

    public void updateGraph() {
        if (user != null) {
            Set<Node> currNodes = new HashSet<>();
            try {
                UserContext userContext = new UserContext(user.getID(), -1);
                currNodes = g.getGraphService().getNodes(userContext);
            } catch (PMException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            grid.setItems(currNodes);
        } else {
            notify("Select a User");
        }
    }

    private void anaylizePOS() {
        // This function should display more detailed analysis
        // List of objects that user has access to with the path from user to each object
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
                    updateGraph();
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
            try {
                g.deleteNode(n.getID());
            } catch (PMException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            updateGraph();
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

    public void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
    }
}