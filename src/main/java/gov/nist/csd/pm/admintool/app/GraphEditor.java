package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.value.ValueChangeMode;
import gov.nist.csd.pm.admintool.app.blips.AssociationBlip;
import gov.nist.csd.pm.admintool.app.blips.NodeDataBlip;
import gov.nist.csd.pm.admintool.app.blips.ProhibitionBlip;
import gov.nist.csd.pm.admintool.app.customElements.MapInput;
import gov.nist.csd.pm.admintool.app.customElements.Toggle;
import gov.nist.csd.pm.admintool.graph.SingletonClient;
import gov.nist.csd.pm.policy.exceptions.PMException;
import gov.nist.csd.pm.policy.model.access.AccessRightSet;
import gov.nist.csd.pm.policy.model.graph.nodes.Node;
import gov.nist.csd.pm.policy.model.graph.nodes.NodeType;
import gov.nist.csd.pm.policy.model.graph.relationships.Association;
import gov.nist.csd.pm.policy.model.prohibition.Prohibition;
import gov.nist.csd.pm.policy.model.prohibition.ProhibitionSubject;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gov.nist.csd.pm.policy.model.access.AdminAccessRights.*;

@Tag("graph-editor")
public class GraphEditor extends VerticalLayout {
    private final SingletonClient g;
    private final HorizontalLayout layout;
    private NodeLayout childNode;
    private NodeLayout parentNode;
    private Node selectedChildNode;
    private Node selectedParentNode;
    private GraphButtonGroup buttonGroup;
    private final boolean hideSuperPolicy = Settings.hidePolicy;

    private Node dragNode;

    public GraphEditor() throws PMException {
        g = SingletonClient.getInstance();
        layout = new HorizontalLayout();
        layout.setFlexGrow(1.0);
        add(layout);
        setUpLayout();
    }

    private void setUpLayout() throws PMException {
        setSizeFull();
        setPadding(true);
        buttonGroup = new GraphButtonGroup();

        childNode = new NodeLayout(true);
        childNode.setWidth("45%");
        childNode.getStyle().set("height", "100vh");
        layout.add(childNode);

        parentNode = new NodeLayout(false);
        parentNode.setWidth("45%");
        parentNode.getStyle().set("height", "100vh");
        layout.add(parentNode);

        layout.add(buttonGroup);
    }

    private class NodeLayout extends VerticalLayout {
        // general fields
        private TreeGrid<Node> grid;
        private final boolean isSource;
        private Stack<Collection<Node>> prevNodes; // Contains the nodes for going 'back'
        private Stack<String> prevNodeNames; // Contains the String for going 'back'
        private Collection<Node> currNodes; // The current nodes in the grid
        private final Map<String, Predicate<? super String>> filters;

        // for title section
        private HorizontalLayout title; // contains Title, Back Button, Current Parent Node
        private H2 titleText;
        private H3 currNodeName; // The current node whose children are being shown
        private Button backButton;
        private Toggle ouToggle;
        private Toggle gridGraphToggle;
        private TextField searchBar;

        // for graph viewer section
        private CytoscapeElement graphViewer;
        private boolean graphViewerAdded = false;
        private boolean graphViewerPendingUpdate = false;

        // for node info section
        private VerticalLayout nodeInfo; // overall node info layout
        private H3 name;
        private Div childrenList, parentList;   // for relations
        private Div outgoingAssociationList, incomingAssociationList; // for associations
        private Div outgoingProhibitionList, incomingProhibitionList; // for prohibitions

        // debugging things
        private int calls;

        public NodeLayout(boolean isSource) throws PMException {
            this.isSource = isSource;
            if (isSource) {
                getStyle().set("background", "lightblue");
            } else {
                getStyle().set("background", "lightcoral");
            }

            filters = new HashMap<>();
            dragNode = null;

            addTitleLayout();
            addGridLayout();
            addGraphLayout();
            addNodeInfoLayout();

            // get data and expand policy classes
            resetGrid();
        }

        private void addTitleLayout() {
            // graph/grid selector
            gridGraphToggle = new Toggle("Grid", "Grid", "Graph");
            gridGraphToggle.getElement().getStyle()
                    .set("margin-top", "2vh")
                    .set("margin-bottom", "1vh");
            gridGraphToggle.addValueChangeListener(event -> {
                switch (event.getValue()) {
                    case "Grid":
                        add(ouToggle, title, searchBar, grid, nodeInfo);
                        graphViewer.setVisible(false);
                        break;
                    case "Graph":
                        remove(ouToggle, title, searchBar, grid, nodeInfo);
                        if (!graphViewerAdded) {
                            add(graphViewer);
                            graphViewerAdded = true;
                        }
                        graphViewer.setVisible(true);

                        // if new changes have occurred while in visible
                        if (graphViewerPendingUpdate) {
                            try {
                                refreshGraphViewer();
                            } catch (PMException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            });
            add(gridGraphToggle);

            // object/user selector
            ouToggle = new Toggle("All", "Users", "All", "Objects");
            ouToggle.addValueChangeListener(event -> {
                switch (event.getValue()) {
                    case "Users":
                        Predicate<? super String> filterUsers = nodeName -> {
                            try {
                                return !(g.getNode(nodeName).getType() == NodeType.O || g.getNode(nodeName).getType() == NodeType.OA);
                            } catch (PMException e) {
                                e.printStackTrace();
                                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                                return false;
                            }
                        };
                        filters.remove("Objects");
                        filters.put("Users", filterUsers);
                        break;
                    case "Objects":
                        Predicate<? super String> filterObjects = nodeName -> {
                            try {
                                return !(g.getNode(nodeName).getType() == NodeType.U || g.getNode(nodeName).getType() == NodeType.UA);
                            } catch (PMException e) {
                                e.printStackTrace();
                                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                                return false;
                            }
                        };
                        filters.remove("Users");
                        filters.put("Objects", filterObjects);
                        break;
                    case "All":
                        filters.remove("Users");
                        filters.remove("Objects");
                        break;
                }
                try {
                    refresh();
                } catch (PMException e) {
                    e.printStackTrace();
                }
            });
            add(ouToggle);

            // title layout config
            title = new HorizontalLayout();
            title.setAlignItems(Alignment.BASELINE);
            title.setJustifyContentMode(JustifyContentMode.START);
            title.setWidthFull();
            title.getStyle()
                    .set("overflow-y", "hidden")
                    .set("overflow-x", "scroll")
                    .set("margin-bottom", "0");
            add(title);

            // title text
            titleText = new H2();
            titleText.getStyle()
                    .set("user-select", "none")
                    .set("margin-top", "0");
            if (isSource) {
                titleText.setText("Source:");
            } else {
                titleText.setText("Destination:");
            }
            title.add(titleText);

            // current parent node whose children are being shown
            currNodeName = new H3("All Nodes");
            currNodeName.getStyle()
                    .set("user-select", "none")
                    .set("margin-top", "0");
            title.add(currNodeName);

            // back button
            backButton = new Button(new Icon(VaadinIcon.ARROW_BACKWARD));
            backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            backButton.addClickListener(evt -> {
                if (!prevNodes.empty()) {
                    currNodes = prevNodes.pop();
                    //grid.setItems(currNodes);
                    updateGridNodes(currNodes);
                    grid.deselectAll();
                    if (isSource) {
                        selectedChildNode = null;
                    } else {
                        selectedParentNode = null;
                    }
                    buttonGroup.refreshNodeTexts();
                    buttonGroup.refreshButtonStates();
                    updateNodeInfo();
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

            // search bar
            searchBar = new TextField();
            searchBar.getStyle()
                    .set("margin-top", "0");;
            searchBar.setWidthFull();
            searchBar.setPlaceholder("Search by name...");
            searchBar.setClearButtonVisible(true);
            searchBar.setValueChangeMode(ValueChangeMode.LAZY);
            searchBar.addValueChangeListener(evt -> {
                if (evt.getValue() != null && !evt.getValue().isEmpty()) {
                    calls = 0;
                    Predicate<? super String> filterName = (nodeName -> {
                        return recursiveContains(nodeName, evt.getValue());
                    });
                    filters.put("Name", filterName);
                } else {
                    filters.remove("Name");
                }
                try {
                    refresh();
                } catch (PMException e) {
                    e.printStackTrace();
                }
            });
            add(searchBar);
        }

        private boolean recursiveContains(String nodeName, String searchString) {
            System.out.println(++calls + " " + nodeName);
            if (nodeName.contains(searchString)) {
                return true;
            } else {
                Set<String> children;
                try {
                    children = g.getChildren(nodeName); // TODO: using this function is fine for U's and O's but will show false positives for PC's
                } catch (PMException e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                    return false;
                }
//                Stream<Node> children = grid.getDataProvider().fetchChildren(new HierarchicalQuery<>(null, node));
                return children.stream().anyMatch(child -> recursiveContains(child, searchString));
            }
        }

        private void addGridLayout() {
            prevNodes = new Stack<>(); // for the navigation system
            prevNodeNames = new Stack<>(); //  for the navigation system

            // grid config
            grid = new TreeGrid<>(Node.class);
            createContextMenu(); // adds the content-specific context menu

            grid.getStyle()
                    .set("border-radius", "1px")
                    .set("user-select", "none");
            //grid.removeColumnByKey("id");
            grid.removeColumnByKey("properties");
            grid.setColumnReorderingAllowed(true);
            grid.setRowsDraggable(true);
            grid.setDropMode(GridDropMode.ON_TOP);
            grid.setHierarchyColumn("name");
            grid.getColumnByKey("name")
                    .setWidth("80%")
                    .setResizable(true);
            grid.getColumnByKey("type")
                    .setTextAlign(ColumnTextAlign.END)
                    .setWidth("20%");

            // Double Click Action: go into current node's children
            grid.addItemDoubleClickListener(evt -> {
                Node n = evt.getItem();
                if (n != null) {
                    try {
                        Set<String> children = g.getChildren(n.getName());
                        if (!children.isEmpty()) {
                            prevNodes.push(currNodes);
//                            currNodes = g.getActiveNodes().stream()
                            currNodes = g.getNodes().stream()
                                    .filter(node_k -> children.contains(node_k.getName())).collect(Collectors.toList());
                            //grid.setItems(currNodes);
                            updateGridNodes(currNodes);

                            prevNodeNames.push(currNodeName.getText());
                            currNodeName.setText(currNodeName.getText() + " > " + n.getName());
                            updateNodeInfo();

                            backButton.setEnabled(true);
                        } else {
                            MainView.notify("Node has no children", MainView.NotificationType.DEFAULT);
                        }
                    } catch (PMException e) {
                        MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
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
                updateNodeInfo();
            });

            // drag + drop action: create assignment or association
            grid.addDropListener((gridDropEvent) -> {
                if (dragNode != null) {
                    Optional<Node> targetNodeOpt = gridDropEvent.getDropTargetItem();
                    if (targetNodeOpt.isPresent()) {
                        Node parentNode = targetNodeOpt.get();
                        if (dragNode != parentNode) {
                            NodeType parentNodeType = parentNode.getType();
                            NodeType dragNodeType = dragNode.getType();
                            if (parentNodeType == NodeType.UA) {
                                if (dragNodeType == NodeType.UA) {
                                    // assignment or association?
                                    Dialog dialog = new Dialog();
                                    dialog.add("Assignment or Association?");

                                    HorizontalLayout form = new HorizontalLayout();
                                    form.setAlignItems(Alignment.BASELINE);

                                    Button assignmentButton = new Button("Assignment", event -> {
                                        // assignment
                                        addAssignment(dragNode, parentNode);
                                        dragNode = null;
                                        dialog.close();
                                    });
                                    assignmentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                                    form.add(assignmentButton);

                                    Button associationButton = new Button("Association", event -> {
                                        // association
                                        addAssociation(dragNode, parentNode);
                                        dragNode = null;
                                        dialog.close();
                                    });
                                    associationButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                                    form.add(associationButton);

                                    Button cancel = new Button("Cancel", event -> {
                                        dialog.close();
                                    });
                                    cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                                    form.add(cancel);

                                    dialog.add(form);
                                    dialog.open();
                                } else if (dragNodeType == NodeType.U) {
                                    // assignment
                                    addAssignment(dragNode, parentNode);
                                    dragNode = null;
                                }
                            } else if (parentNodeType == NodeType.OA) {
                                if (dragNodeType == NodeType.UA) {
                                    // association
                                    addAssociation(dragNode, parentNode);
                                } else if (dragNodeType == NodeType.O || dragNodeType == NodeType.OA) {
                                    // assignment
                                    addAssignment(dragNode, parentNode);
                                }
                                dragNode = null;
                            } else if (parentNodeType == NodeType.PC) {
                                if (dragNodeType == NodeType.UA || dragNodeType == NodeType.OA) {
                                    // assignment
                                    addAssignment(dragNode, parentNode);
                                }
                                dragNode = null;
                            }
                        }
                    }
                }
            });
            grid.addDragStartListener((gridDragStartEvent) -> {
                dragNode = gridDragStartEvent.getDraggedItems().get(0);
            });

            add(grid);
        }

        private void createContextMenu() {
            GridContextMenu<Node> contextMenu = new GridContextMenu<>(grid);

            //contextMenu.addItem("Add Node", event -> addNode());
            contextMenu.addItem("Edit Node", event -> {
                event.getItem().ifPresent(node -> {
                    try {
                        if (g.checkPermissions(node.getName(), SET_NODE_PROPERTIES))
                            editNode(node);
                        else
                            MainView.notify("Current User ('" + g.getCurrentContext() +
                                    "') does not have permission to edit node ('" + node.getName() +
                                    "')", MainView.NotificationType.ERROR);
                    } catch (PMException e) {
                        MainView.notify("Error on Edit Node: " + e.getMessage(), MainView.NotificationType.ERROR);
                        e.printStackTrace();
                    }
                });
            });
            contextMenu.addItem("Delete Node", event -> {
                event.getItem().ifPresent(node -> {
                    deleteNode(node);
                });
            });
            contextMenu.addItem("Explain", event -> {
                event.getItem().ifPresent(node -> {
                    explain(node.getName());
                });
            });
        }

        private void addGraphLayout() throws PMException {
            graphViewer = new CytoscapeElement(isSource ? "cy1":"cy2");
            graphViewer.addClassName("cy");
            graphViewer.setHeight("100%");
            graphViewer.setWidth("100%");

            graphViewer.setClickListener(node_id -> {
                if (node_id == null || node_id.isEmpty()) {
                    if (isSource) selectedChildNode = null;
                    else selectedParentNode = null;
                } else {
                    try {
                        if (isSource) selectedChildNode = g.getNode(node_id);
                        else selectedParentNode = g.getNode(node_id);
                    } catch (PMException e) {
                        e.printStackTrace();

                        if (isSource) selectedChildNode = null;
                        else selectedParentNode = null;
                    }
                }

                buttonGroup.refreshButtonStates();
                buttonGroup.refreshNodeTexts();
            });
        }

        private void addNodeInfoLayout() {
            nodeInfo = new VerticalLayout();
            nodeInfo.setWidthFull();
            nodeInfo.setHeight("30%");
            nodeInfo.getStyle()
                    .set("background", "white")
                    .set("border-radius", "2px")
                    .set("border", "1px solid lightgrey")
                    .set("padding", "10px")
                    .set("line-height", "1px")
                    .set("text-align", "center")
                    .set("overflow-y", "scroll")
                    .set("overflow-x", "hidden")
                    .set("user-select", "none");
            add(nodeInfo);

            name = new H3("X");
            name.setWidthFull();
            nodeInfo.add(name);
            /// TODO: add properties


            nodeInfo.add(new Hr());


            ///// section with assignments ////////////
            Paragraph assignmentsText = new Paragraph("Assignments:");
            assignmentsText.setWidthFull();
            assignmentsText.getStyle().set("font-weight", "bold");
            nodeInfo.add(assignmentsText);

            HorizontalLayout assignments = new HorizontalLayout();
            assignments.setMargin(true);
            assignments.getStyle().set("margin-top", "0");
            assignments.getStyle().set("margin-bottom", "0");
            assignments.setWidthFull();

            // children layout
            VerticalLayout children = new VerticalLayout();
            children.setSizeFull();
            children.setMargin(true);
            children.getStyle().set("margin-top", "0");
            children.getStyle().set("margin-bottom", "0");

            children.add(new Paragraph("Children:"));

            childrenList = new Div();
            childrenList.setSizeFull();
            childrenList.getStyle()
                    .set("margin-top", "0")
                    .set("margin-bottom", "0")
                    .set("overflow", "scroll");
            children.add(childrenList);

            // parent layout
            VerticalLayout parents = new VerticalLayout();
            parents.setMargin(true);
            parents.setSizeFull();
            parents.getStyle().set("margin-top", "0");
            parents.getStyle().set("margin-bottom", "0");

            parents.add(new Paragraph("Parents: "));

            parentList = new Div();
            parentList.setSizeFull();
            parentList.getStyle()
                    .set("margin-top", "0")
                    .set("margin-bottom", "0")
                    .set("overflow", "scroll");
//                    .set("background","green");
            parents.add(parentList);


            // adding it all together
            assignments.add(children);
            assignments.add(parents);

            nodeInfo.add(assignments);
            ///////////////////////////////////////////

            nodeInfo.add(new Hr());


            ///// section with associations ///////////
            Paragraph associationsText = new Paragraph("Associations:");
            associationsText.setWidthFull();
            associationsText.getStyle().set("font-weight", "bold");
            nodeInfo.add(associationsText);

            HorizontalLayout associations = new HorizontalLayout();
            associations.setMargin(true);
            associations.getStyle().set("margin-top", "0");
            associations.getStyle().set("margin-bottom", "0");
            associations.setWidthFull();

            // incoming layout
            VerticalLayout incoming = new VerticalLayout();
            incoming.setMargin(true);
            incoming.setSizeFull();
            incoming.getStyle().set("margin-top", "0");
            incoming.getStyle().set("margin-bottom", "0");

            incoming.add(new Paragraph("Incoming: "));

            incomingAssociationList = new Div();
            incomingAssociationList.setSizeFull();
            incomingAssociationList.getStyle()
                    .set("margin-top", "0")
                    .set("margin-bottom", "0")
                    .set("overflow", "scroll");
            incoming.add(incomingAssociationList);

            // outgoing layout
            VerticalLayout outgoing = new VerticalLayout();
            outgoing.setSizeFull();
            outgoing.setMargin(true);
            outgoing.getStyle().set("margin-top", "0");
            outgoing.getStyle().set("margin-bottom", "0");

            outgoing.add(new Paragraph("Outgoing:"));

            outgoingAssociationList = new Div();
            outgoingAssociationList.setSizeFull();
            outgoingAssociationList.getStyle()
                    .set("margin-top", "0")
                    .set("margin-bottom", "0")
                    .set("overflow", "scroll");
            outgoing.add(outgoingAssociationList);


            // adding it all together
            associations.add(incoming);
            associations.add(outgoing);

            nodeInfo.add(associations);
            ///////////////////////////////////////////////


            nodeInfo.add(new Hr());


            ///// section with prohibitions ///////////////
            Paragraph ProhibitonsText = new Paragraph("Prohibitions:");
            ProhibitonsText.setWidthFull();
            ProhibitonsText.getStyle().set("font-weight", "bold");
            nodeInfo.add(ProhibitonsText);

            HorizontalLayout prohibitions = new HorizontalLayout();
            prohibitions.setMargin(true);
            prohibitions.getStyle().set("margin-top", "0");
            prohibitions.getStyle().set("margin-bottom", "0");
            prohibitions.setWidthFull();

            // outgoing layout
            VerticalLayout outgoingProhibitions = new VerticalLayout();
            outgoingProhibitions.setSizeFull();
            outgoingProhibitions.setMargin(true);
            outgoingProhibitions.getStyle().set("margin-top", "0");
            outgoingProhibitions.getStyle().set("margin-bottom", "0");

            outgoingProhibitions.add(new Paragraph(""));
//            outgoingProhibitions.add(new Paragraph("Outgoing: "));

            outgoingProhibitionList = new Div();
            outgoingProhibitionList.setSizeFull();
            outgoingProhibitionList.getStyle()
                    .set("margin-top", "0")
                    .set("margin-bottom", "0")
                    .set("overflow", "scroll");

            outgoingProhibitions.add(outgoingProhibitionList);


            // incoming layout
//            VerticalLayout incomingProhibitions = new VerticalLayout();
//            incomingProhibitions.setMargin(true);
//            incomingProhibitions.setSizeFull();
//            incomingProhibitions.getStyle().set("margin-top", "0");
//            incomingProhibitions.getStyle().set("margin-bottom", "0");
//
//            incomingProhibitions.add(new Paragraph("Incoming: "));
//
//            incomingProhibitionList = new Div();
//            incomingProhibitionList.setSizeFull();
//            incomingProhibitionList.getStyle()
//                    .set("margin-top", "0")
//                    .set("margin-bottom", "0")
//                    .set("overflow","scroll");
//
//            incomingProhibitions.add(incomingProhibitionList);

            // adding it all together
            prohibitions.add(outgoingProhibitions);
//            prohibitions.add(incomingProhibitions);

            nodeInfo.add(prohibitions);
            //////////////////////////////////////////////////
        }

        private void updateAssignmentInfo(Node gridSelecNode) throws PMException {
            // assignments
            try {
                Iterator<String> childIter = g.getChildren(gridSelecNode.getName()).iterator();
                if (!childIter.hasNext()) {
                    childrenList.add(new Paragraph("None"));
                } else {
                    while (childIter.hasNext()) {
                        String child = childIter.next();
                        try {
                            Node childNode = g.getNode(child);
                            childrenList.add(new NodeDataBlip(childNode, false));
                        } catch (PMException e) {
                            childrenList.add(new Paragraph(child));
                        }
                    }
                }
            } catch (PMException e) {
                childrenList.add(new Paragraph("Inadequate Permissions"));
            }

            try {
                Iterator<String> parentIter = g.getParents(gridSelecNode.getName()).iterator();
                if (!parentIter.hasNext()) {
                    parentList.add(new Paragraph("None"));
                } else {
                    while (parentIter.hasNext()) {
                        String parent = parentIter.next();
                        try {
                            Node parentNode = g.getNode(parent);
                            parentList.add(new NodeDataBlip(parentNode, true));
                        } catch (PMException e) {
                            parentList.add(new Paragraph(parent));
                        }
                    }
                }
            } catch (PMException e) {
                parentList.add(new Paragraph("Inadequate Permissions"));
            }
        }

        private void updateAssociationInfo(Node gridSelecNode) throws PMException {
            // associations
            if (gridSelecNode.getType() == NodeType.UA) {
                try {
                    List<Association> outgoingList = g.getSourceAssociations(gridSelecNode.getName());
                    Iterator<Association> outgoingKeySet = outgoingList.iterator();
                    if (!outgoingKeySet.hasNext()) {
                        outgoingAssociationList.add(new Paragraph("None"));
                    } else {
                        while (outgoingKeySet.hasNext()) {
                            String name = outgoingKeySet.next().getTarget();
                            try {
                                Node node = g.getNode(name);
                                outgoingAssociationList.add(new AssociationBlip(node, true, outgoingKeySet.next().getAccessRightSet()));
                            } catch (PMException e) {
                                outgoingAssociationList.add(new Paragraph(name));
                            }
                        }
                    }
                } catch (PMException e) {
                    outgoingAssociationList.add(new Paragraph("Inadequate Permissions"));
                }
            }

            if (gridSelecNode.getType() == NodeType.UA || gridSelecNode.getType() == NodeType.OA) {
                try {
                    List<Association> incomingList = g.getTargetAssociations(gridSelecNode.getName());
                    Iterator<Association> incomingKeySet = incomingList.iterator();
                    if (!incomingKeySet.hasNext()) {
                        incomingAssociationList.add(new Paragraph("None"));
                    } else {
                        while (incomingKeySet.hasNext()) {
                            String name = incomingKeySet.next().getSource();
                            try {
                                Node node = g.getNode(name);
                                incomingAssociationList.add(new AssociationBlip(node, false, incomingKeySet.next().getAccessRightSet()));
                            } catch (PMException e) {
                                incomingAssociationList.add(new Paragraph(name));
                            }
                        }
                    }
                } catch (PMException e) {
                    incomingAssociationList.add(new Paragraph("Inadequate Permissions"));
                }
            }
        }

        private void updateProhibitionInfo(Node gridSelecNode) throws PMException {
            // prohibitions
            if (gridSelecNode.getType() == NodeType.UA || gridSelecNode.getType() == NodeType.U) {
                try {
                    List<Prohibition> outgoingList = g.getProhibitionsFor(gridSelecNode.getName());
                    Iterator<Prohibition> outgoingIterator = outgoingList.iterator();
                    if (!outgoingIterator.hasNext()) {
                        outgoingProhibitionList.add(new Paragraph("None"));
                    } else {
                        while (outgoingIterator.hasNext()) {
                            Prohibition prohibition = outgoingIterator.next();
                            outgoingProhibitionList.add(new ProhibitionBlip(prohibition));
                        }
                    }
                } catch (PMException e) {
                    outgoingAssociationList.add(new Paragraph("Inadequate Permissions"));
                }
            }

//            if (gridSelecNode.getType() == NodeType.UA || gridSelecNode.getType() == NodeType.OA
//                    || gridSelecNode.getType() == NodeType.U || gridSelecNode.getType() == NodeType.O) {
//                List<Prohibition> incomingList = SingletonClient.getPap().getProhibitionsPAP().getAll();
//                incomingList.removeIf(prohibition -> !prohibition.getContainers().keySet().contains(gridSelecNode.getName()));
//                Iterator<Prohibition> incomingIterator = incomingList.iterator();
//                if (!incomingIterator.hasNext()) {
//                    incomingProhibitionList.add(new Paragraph("None"));
//                } else {
//                    while (incomingIterator.hasNext()) {
//                        Prohibition prohibition = incomingIterator.next();
//                        Iterator<String> containerKeySetIterator = prohibition.getContainers().keySet().iterator();
//                        while (containerKeySetIterator.hasNext()) {
//                            String targetName = containerKeySetIterator.next();
//                            boolean isComplement = prohibition.getContainers().get(targetName);
//                            if (targetName.equals(gridSelecNode.getName())) {
//                                Node subject = SingletonClient.getPap().getGraphPAP().getNode(prohibition.getSubject());
//                                incomingProhibitionList.add(
//                                        new ProhibitonBlip(
//                                                subject,
//                                                gridSelecNode,
//                                                isComplement,
//                                                prohibition.getOperations(),
//                                                prohibition.isIntersection(),
//                                                false
//                                        )
//                                );
//                            }
//                        }
//                    }
//                }
//            }
        }

        public void updateGridNodes(Collection<Node> all_nodes) {
            // TODO: cache grid
            HierarchicalDataProvider dataProvider = new AbstractBackEndHierarchicalDataProvider<Node, Void>() {
                @Override
                public int getChildCount(HierarchicalQuery<Node, Void> query) {
                    try {
                        if (g == null) {
                            System.out.println("Singleton Graph is null");
                            return 0;
                        } else if (query == null) {
                            System.out.println("query is null");
                            return 0;
                        } else {
                            Optional<Node> node = query.getParentOptional();
                            if (node.isPresent()) {
                                Set<String> children = g.getChildren(node.get().getName());
                                if (hideSuperPolicy) {
                                    children = g.getChildrenNoSuperPolicy(node.get().getName());
                                }
                                for (Predicate<? super String> filter : filters.values()) {
                                    children = children.stream().filter(filter).collect(Collectors.toSet());
                                }
                                return children.size();
                            } else {
                                Set<String> temp_all_nodes = new HashSet<>();
                                for (Node n : all_nodes) { // TODO: make function for nodes/strings
                                    temp_all_nodes.add(n.getName());
                                }
                                for (Predicate<? super String> filter : filters.values()) {
                                    temp_all_nodes = temp_all_nodes.stream().filter(filter).collect(Collectors.toSet());
                                }
                                return temp_all_nodes.size();
                            }
                        }
                    } catch (PMException e) {
                        e.printStackTrace();
                        MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                        return 0;
                    }
                }

                @Override
                public boolean hasChildren(Node item) {
                    try {
                        Set<String> children = g.getChildren(item.getName());
                        if (hideSuperPolicy) {
                            children = g.getChildrenNoSuperPolicy(item.getName());
                        }
                        for (Predicate<? super String> filter : filters.values()) {
                            children = children.stream().filter(filter).collect(Collectors.toSet());
                        }
                        return children.size() > 0;
                    } catch (PMException e) {
                        e.printStackTrace();
                        MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                        return false;
                    }
                }

                @Override
                protected Stream<Node> fetchChildrenFromBackEnd(HierarchicalQuery<Node, Void> query) {
                    Collection<Node> children = new HashSet<>();
                    try {
                        if (g == null) {
                            System.out.println("Singleton Graph is null");
                        } else if (query == null) {
                            System.out.println("query is null");
                        } else {
                            Optional<Node> node = query.getParentOptional();
                            if (node.isPresent()) {
                                Set<String> childrenNames = g.getChildren(query.getParent().getName());

                                if (hideSuperPolicy) {
                                    childrenNames = g.getChildrenNoSuperPolicy(query.getParent().getName());
                                }
                                for (Predicate<? super String> filter : filters.values()) {
                                    childrenNames = childrenNames.stream().filter(filter).collect(Collectors.toSet());
                                }

                                for (String name : childrenNames) {
                                    children.add(g.getNode(name));
                                }

                            } else {
                                Set<String> temp_all_nodes = new HashSet<>();
                                for (Node n : all_nodes) { // TODO: make function for nodes/strings
                                    temp_all_nodes.add(n.getName());
                                }
                                for (Predicate<? super String> filter : filters.values()) {
                                    temp_all_nodes = temp_all_nodes.stream().filter(filter).collect(Collectors.toSet());
                                }
                                for (String name : temp_all_nodes) { // TODO: make function for nodes/strings
                                    children.add(g.getNode(name));
                                }
                            }
                        }
                    } catch (PMException e) {
                        e.printStackTrace();
                        MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    }
                    return children.stream();
                }
            };
            grid.setDataProvider(dataProvider);
        }

        public void updateNodeInfo() {
            Node gridSelecNode;
            if (isSource) {
                gridSelecNode = selectedChildNode;
            } else {
                gridSelecNode = selectedParentNode;
            }

            childrenList.removeAll();
            parentList.removeAll();

            outgoingAssociationList.removeAll();
            incomingAssociationList.removeAll();

            outgoingProhibitionList.removeAll();
//            incomingProhibitionList.removeAll();

            if (gridSelecNode != null) {
                try {
                    name.setText(gridSelecNode.getName() + " (" + gridSelecNode.getType().toString() + ")");

                    updateAssignmentInfo(gridSelecNode);
                    updateAssociationInfo(gridSelecNode);
                    updateProhibitionInfo(gridSelecNode);
                } catch (PMException e) {
                    e.printStackTrace();
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                }
            } else {
                name.setText("X");

                childrenList.add(new Paragraph("None"));
                parentList.add(new Paragraph("None"));

                outgoingAssociationList.add(new Paragraph("None"));
                incomingAssociationList.add(new Paragraph("None"));

                outgoingProhibitionList.add(new Paragraph("None"));
//                incomingProhibitionList.add(new Paragraph("None"));
            }
        }

        public void resetGrid() {
            currNodes = new HashSet<>();
            try {
                Set<Node> allNodes = g.getNodes();
//                Set<Node> allNodes = g.getActiveNodes();
                Set<String> visitedNodes = new HashSet<>();

                for (Node n: allNodes) {
                    if (!visitedNodes.contains(n.getName())) { // has not already been visited
                        visitedNodes.add(n.getName());

                        // if no parents, add to curr nodes
                        if (g.getParents(n.getName()).isEmpty()) {
                            if (n.getType().equals(NodeType.PC)) {
//                                if (g.isPCActive(n)) {
                                    currNodes.add(n);
//                                }
                            } else {
                                currNodes.add(n);
                            }
                        }

                        // add children to visited
                        visitedNodes.addAll(g.getChildren(n.getName()));
                    }
                }
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            updateGridNodes(currNodes);
//            expandPolicies();


            // general resetting
            if (isSource) {
                selectedChildNode = null;
            } else {
                selectedParentNode = null;
            }
            updateNodeInfo();

            buttonGroup.refreshNodeTexts();
            buttonGroup.refreshButtonStates();
            backButton.setEnabled(false);
        }

        public void refresh() throws PMException {
//            grid.getDataCommunicator().reset();
            grid.getDataProvider().refreshAll();

            if (graphViewer.isVisible()) {
                refreshGraphViewer();
            } else {
                graphViewerPendingUpdate = true;
            }
        }

        public void refresh(Node... nodes) throws PMException {
            for (Node node : nodes)
                grid.getDataProvider().refreshItem(node, true);

            if (graphViewer.isVisible()) {
                refreshGraphViewer();
            } else {
                graphViewerPendingUpdate = true;
            }
        }

        public void refreshGraphViewer() throws PMException {
            graphViewer.reset();
            graphViewerPendingUpdate = false;
        }


        public void expandPolicies() {
            Set<Node> policies = new HashSet<>();
            try {
                Set<String> policyNames = g.getPolicies();
                for (String policyName : policyNames) {
                    policies.add(g.getNode(policyName));
                }
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            grid.expand(policies);
        }
    }

    private class GraphButtonGroup extends VerticalLayout {
        private Button addNodeButton, addUserButton, addObjectButton,
                addAssignmentButton, deleteAssignmentButton,
                addAssociationButton, editAssociationButton, deleteAssociationButton,
                addProhibitionButton,
                resetButton;
        private final H4 parentNodeText;
        private final H4 childNodeText;
        private final Component connectorSymbol;

        public GraphButtonGroup() {
            getStyle().set("background", "#DADADA") //#A0FFA0
                    .set("overflow-y", "scroll");
            setWidth("20%");
            getStyle().set("height", "100vh");
            setAlignItems(Alignment.CENTER);
            setJustifyContentMode(JustifyContentMode.START);

            childNodeText = new H4("X");
            childNodeText.getStyle().set("user-select", "none");
            connectorSymbol = new H6(new Icon(VaadinIcon.ARROW_DOWN));
            parentNodeText = new H4("X");
            parentNodeText.getStyle().set("user-select", "none");


            add(new Paragraph("\n"));
            add(childNodeText, connectorSymbol, parentNodeText);
            add(new Paragraph("\n"), new Paragraph("\n"));

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

            addUserButton = new Button("Add User", evt -> {
                addUser();
            });
            addUserButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            addUserButton.setWidthFull();
            add(addUserButton);

            addObjectButton = new Button("Add Object", evt -> {
                addObject();
            });
            addObjectButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            addObjectButton.setWidthFull();
            add(addObjectButton);
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
                    childNode.updateNodeInfo();
                    parentNode.updateNodeInfo();
                }
            });
            addAssignmentButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            addAssignmentButton.setEnabled(false);
            addAssignmentButton.setWidthFull();
            add(addAssignmentButton);

            deleteAssignmentButton = new Button("Delete Assignment", evt -> {
                deleteAssignment(selectedChildNode, selectedParentNode);
                childNode.updateNodeInfo();
                parentNode.updateNodeInfo();
            });
            deleteAssignmentButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteAssignmentButton.setEnabled(false);
            deleteAssignmentButton.setWidthFull();
            add(deleteAssignmentButton);
            add(new Paragraph("\n"));


            // Association Buttons
            addAssociationButton = new Button("Add Association", evt -> {
                addAssociation(selectedChildNode, selectedParentNode);
            });
            addAssociationButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            addAssociationButton.setEnabled(false);
            addAssociationButton.setWidthFull();
            add(addAssociationButton);

            editAssociationButton = new Button("Edit Association", evt -> {
                editAssociation(selectedChildNode, selectedParentNode);
            });
            editAssociationButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            editAssociationButton.setEnabled(false);
            editAssociationButton.setWidthFull();
            add(editAssociationButton);

            deleteAssociationButton = new Button("Delete Association", evt -> {
                deleteAssociation(selectedChildNode, selectedParentNode);
            });
            deleteAssociationButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteAssociationButton.setEnabled(false);
            deleteAssociationButton.setWidthFull();
            add(deleteAssociationButton);
            add(new Paragraph("\n"));


            // Prohibition Buttons
            addProhibitionButton = new Button("Add Prohibition", evt -> {
                addProhibition();
            });
            addProhibitionButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            addProhibitionButton.setEnabled(false);
            addProhibitionButton.setWidthFull();
            add(addProhibitionButton);
            add(new Paragraph("\n"));


            // General Buttons
            resetButton = new Button("Reset Graph", evt -> {
                resetGraph();
            });
            resetButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            resetButton.setWidthFull();
            add(resetButton);
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
                NodeType childType = selectedChildNode.getType();
                NodeType parentType = selectedParentNode.getType();
                if ((parentType == NodeType.UA && (childType == NodeType.U || childType == NodeType.UA))
                        || (parentType == NodeType.OA && (childType == NodeType.O || childType == NodeType.OA))
                        || (parentType == NodeType.PC && (childType == NodeType.UA || childType == NodeType.OA))) {

                    boolean canAssign = false, canDeassign = false;
                    try {
                        canAssign = g.checkPermissions(selectedChildNode.getName(), ASSIGN) && g.checkPermissions(selectedParentNode.getName(), ASSIGN_TO);
                        canDeassign = g.checkPermissions(selectedChildNode.getName(), DEASSIGN) && g.checkPermissions(selectedParentNode.getName(), DEASSIGN_FROM);
                    } catch (PMException e) {
                        MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    }

                    addAssignmentButton.setEnabled(canAssign);
                    deleteAssignmentButton.setEnabled(canDeassign);
                } else {
                    addAssignmentButton.setEnabled(false);
                    deleteAssignmentButton.setEnabled(false);
                }


                if ((childType == NodeType.UA) && (parentType == NodeType.UA || parentType == NodeType.OA || parentType == NodeType.O)) {
                    boolean canAssociate = false, canDisassociate = false;
                    try {
                        canAssociate = g.checkPermissions(selectedChildNode.getName(), ASSOCIATE) && g.checkPermissions(selectedParentNode.getName(), ASSOCIATE);
                        canDisassociate = g.checkPermissions(selectedChildNode.getName(), DISSOCIATE) && g.checkPermissions(selectedParentNode.getName(), DISSOCIATE);
                    } catch (PMException e) {
                        MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    }

                    addAssociationButton.setEnabled(canAssociate);
                    editAssociationButton.setEnabled(canAssociate);
                    deleteAssociationButton.setEnabled(canDisassociate);
                } else {
                    addAssociationButton.setEnabled(false);
                    editAssociationButton.setEnabled(false);
                    deleteAssociationButton.setEnabled(false);
                }


                addProhibitionButton.setEnabled((childType == NodeType.UA || childType == NodeType.U) && (parentType == NodeType.OA || parentType == NodeType.O));
            } else {
                addAssignmentButton.setEnabled(false);
                deleteAssignmentButton.setEnabled(false);
                addAssociationButton.setEnabled(false);
                editAssociationButton.setEnabled(false);
                deleteAssociationButton.setEnabled(false);
                addProhibitionButton.setEnabled(false);
            }
        }
    }

    private void addNode() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();

        TextField nameField = new TextField("Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setPlaceholder("Enter Name...");
        form.add(nameField);

        NodeType[] types = new NodeType[4];
        types[0] = NodeType.U;
        types[1] = NodeType.UA;
        types[2] = NodeType.O;
        types[3] = NodeType.OA;
        Select<NodeType> typeSelect = new Select<>();
        typeSelect.setRequiredIndicatorVisible(true);
        typeSelect.setLabel("Type");
        typeSelect.setPlaceholder("Select Type...");
        typeSelect.setItems(types);
        form.add(typeSelect);

        MultiselectComboBox<Node> parentSelect = new MultiselectComboBox<>();
        parentSelect.setRequiredIndicatorVisible(true);
        parentSelect.setLabel("Parent");
        parentSelect.setPlaceholder("Select a parent node...");
        parentSelect.setItemLabelGenerator(Node::getName);

        typeSelect.addValueChangeListener(event -> {
            Collection<Node> nodeCollection;
            try {
                nodeCollection = new HashSet<>(g.getNodes());
//                nodeCollection = new HashSet<>(g.getActiveNodes());
            } catch (PMException e) {
                nodeCollection = new HashSet<>();
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            Collection<Node> finalNodeCollection = nodeCollection;
            switch (event.getValue()) {
                case UA:
                    finalNodeCollection.removeIf(curr -> !(curr.getType() == NodeType.UA || curr.getType() == NodeType.PC));
                    break;
                case OA:
                    finalNodeCollection.removeIf(curr -> !(curr.getType() == NodeType.OA || curr.getType() == NodeType.PC));
                    break;
                case U:
                    finalNodeCollection.removeIf(curr -> !(curr.getType() == NodeType.UA));
                    break;
                case O:
                    finalNodeCollection.removeIf(curr -> !(curr.getType() == NodeType.OA));
                    break;
                default:
                    finalNodeCollection = nodeCollection;
            }
            //TODO: for now we remove PCs - we could remove the default nodes instead ?

            finalNodeCollection.removeIf(curr -> curr.getType() == NodeType.PC);
            parentSelect.setItems(finalNodeCollection);
            parentSelect.setEnabled(true);
        });

        form.add(parentSelect);

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

        // ----- Title Section -----
        Button submitButton = new Button("Submit", event -> {
            String name = nameField.getValue();
            NodeType type = typeSelect.getValue();
            Set<Node> parents = parentSelect.getSelectedItems();
            try {
                Map<String, String> props = propsField.getValue();
                if (name == null || name.equals("")) {
                    nameField.focus();
                    MainView.notify("Name is Required", MainView.NotificationType.DEFAULT);
                } else if (type == null) {
                    typeSelect.focus();
                    MainView.notify("Type is Required", MainView.NotificationType.DEFAULT);
                } else if (parents.isEmpty()) {
                    MainView.notify("Parent is Required", MainView.NotificationType.DEFAULT);
                } else {
                    g.createNode(name, type, props, parents.iterator().next().getName());
                    for (Node parent : parents) {

                        switch (type) {
                            case UA:
                                if (!g.getParents(name).contains(parent.getName())) {
                                    if (parent.getType() == NodeType.UA) {
                                        g.assign(name, parent.getName());
                                    }
                                }
                                break;
                            case O:
                                if (parent.getType() == NodeType.OA && !g.getParents(name).contains(parent.getName())) {
                                    g.assign(name, parent.getName());
                                }
                                break;
                            case U:
                                if (parent.getType() == NodeType.UA && !g.getParents(name).contains(parent.getName())) {
                                    g.assign(name, parent.getName());
                                }
                                break;
                            case OA:
                                if (!g.getParents(name).contains(parent.getName())) {
                                    if (parent.getType() == NodeType.OA) {
                                        g.assign(name, parent.getName());
                                    }
                                }
                                break;
                        }
                    }
                    MainView.notify("Node with name: " + name + " created", MainView.NotificationType.SUCCESS);
                    childNode.refresh(parents.toArray(new Node[0]));
                    parentNode.refresh(parents.toArray(new Node[0]));
                    dialog.close();
                }
            } catch (Exception e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
        });
        HorizontalLayout titleLayout = TitleFactory.generate("Add Node", submitButton);

        dialog.add(titleLayout, new Hr(), form);
        dialog.open();
        nameField.focus();
    }

    private void addUser() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();

        TextField nameField = new TextField("Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setPlaceholder("Enter Name...");
        form.add(nameField);

        Collection<Node> nodeCollection;
        try {
            nodeCollection = new HashSet<>(g.getNodes());
//            nodeCollection = new HashSet<>(g.getActiveNodes());

        } catch (PMException e) {
            nodeCollection = new HashSet<>();
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }

        nodeCollection.removeIf(curr -> !(curr.getType() == NodeType.UA || curr.getType() == NodeType.PC));
        MultiselectComboBox<Node> parentSelect = new MultiselectComboBox<>();
        /*if (nodeCollection.size() == 0) {
            parentSelect.setEnabled(false);
        }
*/
        List<Node> nodes = new ArrayList<>(nodeCollection);
        //parentSelect.setRequiredIndicatorVisible(true);
        parentSelect.setLabel("Parent");
        parentSelect.setPlaceholder("Select UA or PC...");

        parentSelect.setItemLabelGenerator(Node::getName);
        parentSelect.setItems(nodes);
        form.add(parentSelect);

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

        // ----- Title Section -----
        Button button = new Button("Submit", event -> {
            String name = nameField.getValue();
            Set<Node> parents = parentSelect.getSelectedItems();
            try {
                Map<String, String> props = propsField.getValue();
                if (name == null || name == "") {
                    nameField.focus();
                    MainView.notify("Name is Required", MainView.NotificationType.DEFAULT);
                } else if (parents.isEmpty()) {
                    MainView.notify("At least one parent is required", MainView.NotificationType.DEFAULT);
                } else {
                    g.createNode(name, NodeType.U, props, parents.iterator().next().getName());
                    for (Node parent : parents) {
                        if (parent.getType() == NodeType.UA && !g.getParents(name).contains(parent.getName())) {
                            g.assign(name, parent.getName());
                        }
                    }
                    MainView.notify("User with name: " + name + " has been created", MainView.NotificationType.SUCCESS);

                    childNode.refresh(parents.toArray(new Node[0]));
                    parentNode.refresh(parents.toArray(new Node[0]));
                    dialog.close();
                }
            } catch (Exception e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
        });
        if (nodeCollection.size() == 0) {
            button.setEnabled(false);
        }
        HorizontalLayout titleLayout = TitleFactory.generate("Add User", button);

        dialog.add(titleLayout, new Hr(), form);
        dialog.open();
        nameField.focus();
    }

    private void addObject() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();

        TextField nameField = new TextField("Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setPlaceholder("Enter Name...");
        form.add(nameField);

        Collection<Node> nodeCollection;
        try {
            //filter nodes
            g.getNodes();
            nodeCollection = new HashSet<>(g.getNodes());
        } catch (PMException e) {
            nodeCollection = new HashSet<>();
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }
        nodeCollection.removeIf(curr -> curr.getType() != NodeType.OA);

        MultiselectComboBox<Node> parentSelect = new MultiselectComboBox<Node>("Parent", nodeCollection);
        if (nodeCollection.size() == 0) {
            parentSelect.setEnabled(false);
        }

        parentSelect.setItemLabelGenerator(Node::getName);
        parentSelect.setRequiredIndicatorVisible(true);
        parentSelect.setPlaceholder("Select OA...");
        form.add(parentSelect);

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

        // ----- Title Section -----
        Button button = new Button("Submit", event -> {
            String name = nameField.getValue();
            Set<Node> parents = parentSelect.getSelectedItems();
            try {
                Map<String, String> props = propsField.getValue();
                if (name == null || name == "") {
                    nameField.focus();
                    MainView.notify("Name is Required", MainView.NotificationType.DEFAULT);
                } else if (parents.isEmpty()) {
                    MainView.notify("At least one parent is required", MainView.NotificationType.DEFAULT);
                } else {
                    g.createNode(name, NodeType.O, props, parents.iterator().next().getName());
                    for (Node parent : parents) {
                        if (parent.getType() == NodeType.OA && !g.getParents(name).contains(parent.getName())) {
                            g.assign(name, parent.getName());
                        }
                    }
                    MainView.notify("Object with name: " + name + " has been created", MainView.NotificationType.SUCCESS);
                    childNode.refresh(parents.toArray(new Node[0]));
                    parentNode.refresh(parents.toArray(new Node[0]));
                    dialog.close();
                }
            } catch (Exception e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
        });
        if (nodeCollection.size() == 0) {
            button.setEnabled(false);
        }
        HorizontalLayout titleLayout = TitleFactory.generate("Add Object", button);

        dialog.add(titleLayout, new Hr(), form);
        dialog.open();
        nameField.focus();
    }

    private void editNode(Node n) {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();

        TextField nameField = new TextField("Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setValue(n.getName());
        nameField.setEnabled(false);
        form.add(nameField);

//        TextArea propsFeild = new TextArea("Properties (key=value \\n...)");
//        propsFeild.setPlaceholder("Enter Properties...");
//        String pStr = n.getProperties().toString().replaceAll(", ", "\n");
//        propsFeild.setValue(pStr.substring(1, pStr.length() - 1));
//        form.add(propsFeild);

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
        propsField.setValue(n.getProperties());
        form.add(propsField);

        // ----- Title Section -----
        Button submitButton = new Button("Submit", event -> {
            String name = nameField.getValue();
            try {
                Map<String, String> props = propsField.getValue();
                if (name == null || name == "") {
                    nameField.focus();
                    MainView.notify("Name is Required", MainView.NotificationType.DEFAULT);
                } else {
                    g.updateNode(name, props);
                    MainView.notify("Node with name: " + name + " has been edited", MainView.NotificationType.SUCCESS);
                    childNode.updateNodeInfo();
                    parentNode.updateNodeInfo();

                    childNode.refreshGraphViewer();
                    parentNode.refreshGraphViewer();
                    dialog.close();
                }
            } catch (Exception e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
        });
        HorizontalLayout titleLayout = TitleFactory.generate("Edit Node", n.getName(), submitButton);

        dialog.add(titleLayout, new Hr(), form);
        dialog.open();
    }

    private void deleteNode(Node n) {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(Alignment.BASELINE);

        form.add(new Paragraph("Are You Sure?"));

        Button button = new Button("Delete", event -> {
            try {
                String name = n.getName();
                Set<String> parentStrings = g.getParents(n.getName());
                Collection<Node> parents = new HashSet<>();
                parentStrings.forEach((parentName) -> {
                    try {
                        parents.add(g.getNode(parentName));
                    } catch (PMException e) {
                        MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                        e.printStackTrace();
                    }
                });
                g.deleteNode(name);
                MainView.notify("Node with name: " + name + " has been deleted", MainView.NotificationType.SUCCESS);
                childNode.resetGrid();
                parentNode.resetGrid();
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
//                MainView.notify("You have to delete all assignment on that node first.");
                e.printStackTrace();
            }

            dialog.close();
        });
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        form.add(button);

        Button cancel = new Button("Cancel", event -> {
            dialog.close();
        });
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        form.add(cancel);

        HorizontalLayout titleLayout = TitleFactory.generate("Delete Node", n.getName());

        dialog.add(titleLayout, new Hr(), form);
        dialog.open();
    }

    private void addAssignment(Node child, Node parent) {
        if (child != null && parent != null) {
            try {
                g.assign(child.getName(), parent.getName());
                MainView.notify(child.getName() + " assigned to " + parent.getName(), MainView.NotificationType.SUCCESS);
                childNode.refresh(parent);
                parentNode.refresh(parent);
            } catch (Exception e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
        } else {
            MainView.notify("Must choose both a parent and a child for assignment", MainView.NotificationType.DEFAULT);
        }
    }

    private void deleteAssignment(Node child, Node parent) {
        if (child != null && parent != null) {
            Dialog dialog = new Dialog();
            HorizontalLayout form = new HorizontalLayout();
            form.setAlignItems(Alignment.BASELINE);
            form.add(new Paragraph("Are You Sure?"));

            Button button = new Button("Delete", event -> {
                try {
                    if (g.getParents(child.getName()).size() != 1) {
                        g.deassign(child.getName(), parent.getName());
                        MainView.notify(child.getName() + " un-assigned from " + parent.getName(), MainView.NotificationType.SUCCESS);

                    } else {
                        Dialog dialog2 = new Dialog();
                        VerticalLayout form2 = new VerticalLayout();
                        form2.setAlignItems(Alignment.BASELINE);

                        form2.add(new Header(new Span("You cannot un-assigned " + child.getName() + " from " + parent.getName() + " otherwise " + child.getName() + " will become a standalone node.")));
                        form2.add(new Span("Do you want to delete " + child.getName() + " instead ?"));

                        Button button2 = new Button("Delete", event2 -> {
                            try {
                                g.deleteNode(child.getName());
                            } catch (PMException e) {
                                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                                e.printStackTrace();
                            }
                            childNode.resetGrid();
                            parentNode.resetGrid();
                            dialog2.close();
                            dialog.close();
                        });

                        Button button_cancel_all = new Button("Cancel", event_cancel -> {
                            dialog2.close();
                            dialog.close();
                        });

                        button2.addThemeVariants(ButtonVariant.LUMO_ERROR);
                        dialog2.add(form2);
                        form2.add(new Span(button2, button_cancel_all));
                        dialog2.open();
                    }
                } catch (PMException e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
                try {
                    childNode.refresh();
                } catch (PMException e) {
                    e.printStackTrace();
                }
                try {
                    parentNode.refresh();
                } catch (PMException e) {
                    e.printStackTrace();
                }
                dialog.close();
            });

            button.addThemeVariants(ButtonVariant.LUMO_ERROR);
            form.add(button);

            Button cancel = new Button("Cancel", event -> {
                dialog.close();
            });
            cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            form.add(cancel);

            HorizontalLayout titleLayout = TitleFactory.generate("Delete Assignment", child.getName() + " -> " + parent.getName());

            dialog.add(titleLayout, new Hr(), form);
            dialog.open();
        } else {
            MainView.notify("Must choose both a parent and a child for de-assignment", MainView.NotificationType.DEFAULT);
        }
    }

    private void addAssociation(Node source, Node target) {
        if (source != null && target != null) {
            Dialog dialog = new Dialog();
            dialog.setWidth("75vh");

//        Paragraph opsParagraph = new Paragraph("[]");
//        HorizontalLayout name =
//            new HorizontalLayout(
//                new NodeDataBlip(source, false),
//                new Icon(VaadinIcon.ARROW_RIGHT),
//                opsParagraph,
//                new Icon(VaadinIcon.ARROW_RIGHT),
//                new NodeDataBlip(target, true)
//            );
//        name.setAlignItems(Alignment.CENTER);
//        name.setJustifyContentMode(JustifyContentMode.CENTER);
//        dialog.add(name);

            HorizontalLayout form = new HorizontalLayout();
            form.setAlignItems(Alignment.BASELINE);

            // resource ops multi-select
            MultiselectComboBox<String> opsSelectRessource = new MultiselectComboBox<>();
            opsSelectRessource.setLabel("Resource Access Rights");
            opsSelectRessource.setPlaceholder("Choose Access Rights");
            try {
                opsSelectRessource.setItems(g.getResourceOpsWithStars());
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            opsSelectRessource.setWidth("100%");

            // admin ops multi-select
            MultiselectComboBox<String> opsSelectAdmin = new MultiselectComboBox<>();
            opsSelectAdmin.setLabel("Admin Access Rights");
            opsSelectAdmin.setPlaceholder("Choose Access Rights");
            try {
                opsSelectAdmin.setItems(g.getAdminOpsWithStars());
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            opsSelectAdmin.setWidth("100%");

//        opsSelectRessource.addValueChangeListener((evt) -> {
//            Set<String> tempOps = evt.getValue();
//            tempOps.addAll(opsSelectAdmin.getValue());
//            opsParagraph.setText(tempOps.toString());
//        });
//        opsSelectAdmin.addValueChangeListener((evt) -> {
//            Set<String> tempOps = evt.getValue();
//            tempOps.addAll(opsSelectRessource.getValue());
//            opsParagraph.setText(tempOps.toString());
//        });

            form.add(opsSelectRessource);
            form.add(opsSelectAdmin);

            // ----- Title Section -----
            Button submit = new Button("Submit", event -> {
                List<String> opString = new ArrayList<>();
                opString.addAll(opsSelectRessource.getValue());
                opString.addAll(opsSelectAdmin.getValue());
                AccessRightSet ops = new AccessRightSet(opString);
                if (opString == null) {
                    MainView.notify("Access Rights are Required", MainView.NotificationType.DEFAULT);
                } else {
                    try {
                        g.associate(source.getName(), target.getName(), ops);
                        MainView.notify(source.getName() + " assigned to " + target.getName(), MainView.NotificationType.SUCCESS);
                        childNode.updateNodeInfo();
                        parentNode.updateNodeInfo();

                        childNode.refreshGraphViewer();
                        parentNode.refreshGraphViewer();
                        dialog.close();
                    } catch (Exception e) {
                        MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                        e.printStackTrace();
                    }
                }
            });
            HorizontalLayout titleLayout = TitleFactory.generate("Add Association",
                    source.getName() + " -> " + target.getName(), submit);

            dialog.add(titleLayout, new Hr(), form);
            dialog.open();
        } else {
            MainView.notify("Must choose both a source and a target for association", MainView.NotificationType.DEFAULT);
        }
    }

    private void editAssociation(Node source, Node target) {
        if (source != null && target != null) {
            Dialog dialog = new Dialog();
            dialog.setWidth("75vh");
            HorizontalLayout form = new HorizontalLayout();
            form.setAlignItems(Alignment.BASELINE);

            MultiselectComboBox<String> opsSelectRessource = new MultiselectComboBox<>();
            opsSelectRessource.setLabel("Resource Access Rights");
            opsSelectRessource.setPlaceholder("Choose Access Rights");
            try {
                opsSelectRessource.setItems(g.getResourceOpsWithStars());
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            opsSelectRessource.setWidth("100%");

            MultiselectComboBox<String> opsSelectAdmin = new MultiselectComboBox<>();
            opsSelectAdmin.setLabel("Admin Access Rights");
            opsSelectAdmin.setPlaceholder("Choose Access Rights");
            try {
                opsSelectAdmin.setItems(g.getAdminOpsWithStars());
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            opsSelectAdmin.setWidth("100%");

            form.add(opsSelectRessource);
            form.add(opsSelectAdmin);
            try {
                if (source.getType() == NodeType.UA) {
                    List<Association> sourceOps = g.getSourceAssociations(source.getName());
                    Set<String> sourceToTargetOps = new HashSet<>();
                    sourceOps.forEach((targetAssoc) -> {
                        if (targetAssoc.getSource().equalsIgnoreCase(target.getName())) {
                            sourceToTargetOps.addAll(targetAssoc.getAccessRightSet());
                        }
                    });
                    HashSet<String> existingResourcesOp = new HashSet<>();
                    HashSet<String> existingAdminsOp = new HashSet<>();
                    sourceToTargetOps.forEach(op -> {
                        try {
                            if (g.getResourceOpsWithStars().contains(op)) {
                                existingResourcesOp.add(op);
                            } else if (g.getAdminOpsWithStars().contains(op)) {
                                existingAdminsOp.add(op);
                            }
                        } catch (PMException e) {
                            e.printStackTrace();
                            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                        }
                    });
                    opsSelectRessource.setValue(existingResourcesOp);
                    opsSelectAdmin.setValue(existingAdminsOp);
                }
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }

            // ----- Title Section -----
            Button submit = new Button("Submit", event -> {
                List<String> opString = new ArrayList<>();
                opString.addAll(opsSelectRessource.getValue());
                opString.addAll(opsSelectAdmin.getValue());
                AccessRightSet ops = new AccessRightSet(opString);
                if (opString == null) {
                    MainView.notify("Access Rights are Required", MainView.NotificationType.DEFAULT);
                } else {
                    try {
                        g.associate(source.getName(), target.getName(), ops);
                        MainView.notify("Association between " + source.getName() + " and " + target.getName() + " has been modified", MainView.NotificationType.SUCCESS);
                        childNode.updateNodeInfo();
                        parentNode.updateNodeInfo();

                        childNode.refreshGraphViewer();
                        parentNode.refreshGraphViewer();
                        dialog.close();
                    } catch (Exception e) {
                        MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                        e.printStackTrace();
                    }
                }
            });
            HorizontalLayout titleLayout = TitleFactory.generate("Edit Association",
                    source.getName() + " -> " + target.getName(), submit);

            dialog.add(titleLayout, new Hr(), form);
            dialog.open();
        } else {
            MainView.notify("Must choose both a source and a target for association", MainView.NotificationType.DEFAULT);
        }
    }

    private void deleteAssociation(Node source, Node target) {
        if (source != null && target != null) {
            Dialog dialog = new Dialog();
            HorizontalLayout form = new HorizontalLayout();
            form.setAlignItems(Alignment.BASELINE);

            form.add(new Paragraph("Are You Sure?"));

            Button button = new Button("Delete", event -> {
                try {
                    g.dissociate(source.getName(), target.getName());
                    MainView.notify("Association between " + source.getName() + " and " + target.getName() + " has been deleted", MainView.NotificationType.SUCCESS);
                    childNode.updateNodeInfo();
                    parentNode.updateNodeInfo();

                    childNode.refreshGraphViewer();
                    parentNode.refreshGraphViewer();
                } catch (PMException e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
                dialog.close();
            });
            button.addThemeVariants(ButtonVariant.LUMO_ERROR);
            form.add(button);

            Button cancel = new Button("Cancel", event -> {
                dialog.close();
            });
            cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            form.add(cancel);

            HorizontalLayout titleLayout = TitleFactory.generate("Delete Association",
                    source.getName() + " -> " + target.getName());

            dialog.add(titleLayout, new Hr(), form);
            dialog.open();
        } else {
            MainView.notify("Must choose both a source and a target for association", MainView.NotificationType.DEFAULT);
        }
    }

    private void addProhibition() {
        Dialog dialog = new Dialog();

        HorizontalLayout form = new HorizontalLayout();
        // form.setAlignItems(Alignment.BASELINE);

        // prohibition name input
        TextField nameField = new TextField("Prohibition Name");
        int numOfProhibtionsForSubject = 0;
        try {
            numOfProhibtionsForSubject = g.getProhibitionsFor(selectedChildNode.getName()).size();
        } catch (PMException e) {
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }
        String initialName = "deny_" + selectedChildNode.getName() + "-" + (numOfProhibtionsForSubject + 1);
        nameField.setValue(initialName);
        form.add(nameField);

        // operations multi-selectors
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

        // containers select
        HashSet<String> targets = new HashSet<>();
        HashSet<Node> targetNodes = new HashSet<>();
        try {
            targetNodes.addAll(g.getNodes());
        } catch (PMException e) {
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }
        targetNodes.removeIf(curr -> !(curr.getType() == NodeType.OA || curr.getType() == NodeType.O));
        targetNodes.forEach((n) -> targets.add(n.getName()));

        MapInput<String, Boolean> containerField = new MapInput<>((new Select<String>()).getClass(), Checkbox.class,
                (keyField) -> {
                    if (keyField instanceof Select) {
                        Select<String> temp = (Select<String>) keyField;
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
                    } else {
                        MainView.notify("Not an instance of a TextField", MainView.NotificationType.ERROR);
                    }
                }, null,
                null, null
        );
        containerField.setLabel("Containers (Target, Complement)");
        Map<String, Boolean> temp = new HashMap<>();
        temp.put(selectedParentNode.getName(), false);
        containerField.setValue(temp);
        form.add(containerField);

        // intersection checkbox
        Checkbox intersectionFeild = new Checkbox("Intersection");
        VerticalLayout intersectionFeildLayout = new VerticalLayout(intersectionFeild);
        form.add(new VerticalLayout(intersectionFeildLayout));

        // title
        Button submit = new Button("Submit", event -> {
            String name = nameField.getValue();
            AccessRightSet ops = new AccessRightSet(rOpsField.getValue());
            ops.addAll(aOpsField.getValue());
            boolean intersection = intersectionFeild.getValue();
            try {
                Map<String, Boolean> containers = containerField.getValue();
                if (ops == null || ops.isEmpty()) {
                    MainView.notify("Access Rights are Required", MainView.NotificationType.DEFAULT);
                } else if (name == null || name.equals("")) {
                    nameField.focus();
                    MainView.notify("Name is Required", MainView.NotificationType.DEFAULT);
                } else if (containers.isEmpty()) {
                    MainView.notify("Containers are Required", MainView.NotificationType.DEFAULT);
                } else {
                    //TODO : Check prohibition type
                    ProhibitionSubject prohibitionSubject = new ProhibitionSubject(selectedChildNode.getName(), ProhibitionSubject.Type.USER_ATTRIBUTE);
                    g.addProhibition(nameField.getValue(), prohibitionSubject, containers, ops, intersection);
                    MainView.notify("Prohibition with name: " + nameField.getValue() + " has been created", MainView.NotificationType.SUCCESS);
                    childNode.updateNodeInfo();
                    parentNode.updateNodeInfo();
                    dialog.close();
                }
            } catch (Exception e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
        });
        HorizontalLayout titleLayout = TitleFactory.generate("Add Prohibition",
                "Denying " + selectedChildNode.getName(), submit);

        dialog.add(titleLayout, new Hr(), form);
        dialog.open();
    }

    private void explain(String node) {
        Dialog dialog = new Dialog();

        String explanation = g.getExplanation(node);

        VerticalLayout auditLayout = new VerticalLayout();

        auditLayout.setSizeFull();
        auditLayout.getStyle()
                .set("padding-bottom", "0px");
        String[] split = explanation.split("\n");
        if (split.length > 1) {
            for (String line : split) {
                Span lineSpan = new Span(line);
                int tabs = 0;
                while (line.startsWith("\t")) {
                    tabs++;
                    line = line.substring(1);
                }
                lineSpan.getStyle()
                        .set("margin", "0")
                        .set("padding-left", ((Integer) (tabs * 25)).toString() + "px")
                        .set("padding", "0");
                auditLayout.add(lineSpan);
            }
        } else {
            auditLayout.add(new Span(explanation));
        }

        // ----- Title Section -----
        Button button = new Button("Close", event -> {
            dialog.close();
        });
        HorizontalLayout titleLayout = TitleFactory.generate("Explanation", button);

        dialog.add(titleLayout, new Hr(), new HorizontalLayout(auditLayout));
        dialog.open();
    }

    private void resetGraph() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(Alignment.BASELINE);

        form.add(new Paragraph("Are You Sure?"));

        Button button = new Button("Reset", event -> {
            try {
                SingletonClient.resetAllPCs();
                g.reset();
                MainView.notify("Graph has been reset", MainView.NotificationType.SUCCESS);
                childNode.resetGrid();
                childNode.expandPolicies();
                parentNode.resetGrid();
                parentNode.expandPolicies();
            } catch (Exception e) {
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

        HorizontalLayout titleLayout = TitleFactory.generate("Reset Graph", "This will delete all Nodes");

        dialog.add(titleLayout, new Hr(), form);

        dialog.add(form);
        dialog.open();

    }
}
