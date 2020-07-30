package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.hierarchy.*;
import gov.nist.csd.pm.admintool.app.blips.*;
import gov.nist.csd.pm.admintool.app.customElements.MapInput;
import gov.nist.csd.pm.admintool.app.customElements.Toggle;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pip.prohibitions.model.Prohibition;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        buttonGroup = new GraphButtonGroup();

        childNode = new NodeLayout(true);
        childNode.setWidth("45%");
        childNode.getStyle().set("height","100vh");
        layout.add(childNode);

        parentNode = new NodeLayout(false);
        parentNode.setWidth("45%");
        parentNode.getStyle().set("height","100vh");
        layout.add(parentNode);

        layout.add(buttonGroup);
    }

    private class NodeLayout extends VerticalLayout {
        // general fields
        private TreeGrid<Node> grid;
        private Stack<Collection<Node>> prevNodes; // Contains the nodes for going 'back'
        private Stack<String> prevNodeNames; // Contains the String for going 'back'
        private Collection<Node> currNodes; // The current nodes in the grid

        // for title section
        private H2 titleText;
        private H3 currNodeName; // The current node whose children are being shown
        private Button backButton;
        private Toggle ouToggle;

        // for node info section
        private H3 name;
        private Div childrenList, parentList;   // for relations
        private Div outgoingAssociationList, incomingAssociationList; // for associations
        private Div outgoingProhibitionList, incomingProhibitionList; // for prohibitions


        private boolean isSource;

        public NodeLayout(boolean isSource){
            this.isSource = isSource;
            if (isSource) {
                getStyle().set("background", "lightblue");
            } else {
                getStyle().set("background", "lightcoral");
            }

            addTitleLayout();
            addGridLayout();
            addNodeInfoLayout();
        }

        private void addTitleLayout () {
            /// contains Title, Back Button, Current Parent Node

            // title layout config
            HorizontalLayout title = new HorizontalLayout();
            title.setAlignItems(Alignment.BASELINE);
            title.setJustifyContentMode(JustifyContentMode.START);
            title.setWidthFull();
            title.getStyle()
                    .set("overflow-y", "hidden")
                    .set("overflow-x", "scroll");
            add(title);

            // title text
            titleText = new H2();
            if (isSource) {
                titleText.setText("Source:");
            } else {
                titleText.setText("Destination:");
            }
            title.add(titleText);

            // current parent node whose children are being shown
            currNodeName = new H3("All Nodes");
            title.add(currNodeName);

            // back button
            backButton = new Button(new Icon(VaadinIcon.ARROW_BACKWARD));
            backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            backButton.addClickListener(evt -> {
                if (!prevNodes.empty()) {
                    currNodes = prevNodes.pop();
                    //grid.setItems(currNodes);
                    updateGrid(currNodes);
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

            // object/user selector
            ouToggle = new Toggle("All", "Users", "All", "Objects");
            ouToggle.addValueChangeListener(event -> {

                switch (event.getValue()) {
                    case "Users":
                        try {
                            currNodes = g.getNodes().stream()
                                    .filter(node_k -> node_k.getType() == NodeType.U).collect(Collectors.toList());
                            System.out.println("users : " + currNodes);
                            updateGrid(currNodes);
                            //grid.sort(Arrays.asList(new GridSortOrder<>(grid.getColumnByKey("name"), SortDirection.DESCENDING)));
                        } catch (PMException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Objects":
                        try {
                            currNodes = g.getNodes().stream()
                                    .filter(node_k -> node_k.getType() == NodeType.O).collect(Collectors.toList());
                            System.out.println("objects : " + currNodes);
                            updateGrid(currNodes);
                            //grid.sort(Arrays.asList(new GridSortOrder<>(grid.getColumnByKey("name"), SortDirection.DESCENDING)));

                        } catch (PMException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "All":
                        try {
                            currNodes = new ArrayList<>(g.getNodes());
                            System.out.println("all : " + currNodes);
                            refreshGraph();
                        } catch (PMException e) {
                            e.printStackTrace();
                        }
                }
                MainView.notify(event.getValue(), MainView.NotificationType.DEFAULT);
            });
            add(ouToggle);
        }
        private void addGridLayout () {
            prevNodes = new Stack<>(); // for the navigation system
            prevNodeNames = new Stack<>(); //  for the navigation system

            // grid config
            grid = new TreeGrid<>(Node.class);
            createContextMenu(); // adds the content-specific context menu

            refreshGraph();

            grid.getStyle()
                    .set("border-radius", "1px")
                    .set("user-select", "none");
            grid.removeColumnByKey("id");
            grid.removeColumnByKey("properties");
            grid.setColumnReorderingAllowed(true);
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
                if(n != null) {
                    try {
                        Set<String> children = g.getChildren(n.getName());
                        if (!children.isEmpty()) {
                            prevNodes.push(currNodes);
                            currNodes = g.getNodes().stream()
                                    .filter(node_k -> children.contains(node_k.getName())).collect(Collectors.toList());
                            //grid.setItems(currNodes);
                            updateGrid(currNodes);

                            prevNodeNames.push(currNodeName.getText());
                            currNodeName.setText(currNodeName.getText() + " > " + n.getName());
                            updateNodeInfoSection();

                            backButton.setEnabled(true);
                        } else {
                            MainView.notify("Node has no children", MainView.NotificationType.DEFAULT);
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
        }
        private void addNodeInfoLayout () {
            VerticalLayout nodeInfo = new VerticalLayout();
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
                    .set("overflow","scroll");
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
                    .set("overflow","scroll");
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
                    .set("overflow","scroll");
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
                    .set("overflow","scroll");
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
                    .set("overflow","scroll");

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

        private void updateNodeInfoSection() {
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

        private void updateAssignmentInfo(Node gridSelecNode) throws PMException {
            // assignments
            Iterator<String> childIter = g.getChildren(gridSelecNode.getName()).iterator();
            if (!childIter.hasNext()) {
                childrenList.add(new Paragraph("None"));
            } else {
                while (childIter.hasNext()) {
                    String child = childIter.next();
                    Node childNode = g.getNode(child);
                    childrenList.add(new NodeDataBlip(childNode, false));
                }
            }

            Iterator<String> parentIter = g.getParents(gridSelecNode.getName()).iterator();
            if (!parentIter.hasNext()) {
                parentList.add(new Paragraph("None"));
            } else {
                while (parentIter.hasNext()) {
                    String parent = parentIter.next();
                    Node parentNode = g.getNode(parent);
                    parentList.add(new NodeDataBlip(parentNode, true));
                }
            }
        }
        private void updateAssociationInfo(Node gridSelecNode) throws PMException {
            // associations
            if (gridSelecNode.getType() == NodeType.UA) {
                Map<String, OperationSet> outgoingMap = g.getSourceAssociations(gridSelecNode.getName());
                Iterator<String> outgoingKeySet = outgoingMap.keySet().iterator();
                if (!outgoingKeySet.hasNext()) {
                    outgoingAssociationList.add(new Paragraph("None"));
                } else {
                    while (outgoingKeySet.hasNext()) {
                        String name = outgoingKeySet.next();
                        Node node = g.getNode(name);
                        outgoingAssociationList.add(new AssociationBlip(node, true, outgoingMap.get(name)));
                    }
                }
            }

            if (gridSelecNode.getType() == NodeType.UA || gridSelecNode.getType() == NodeType.OA) {
                Map<String, OperationSet> incomingMap = g.getTargetAssociations(gridSelecNode.getName());
                Iterator<String> incomingKeySet = incomingMap.keySet().iterator();
                if (!incomingKeySet.hasNext()) {
                    incomingAssociationList.add(new Paragraph("None"));
                } else {
                    while (incomingKeySet.hasNext()) {
                        String name = incomingKeySet.next();
                        Node node = g.getNode(name);
                        incomingAssociationList.add(new AssociationBlip(node, false, incomingMap.get(name)));
                    }
                }
            }
        }
        private void updateProhibitionInfo(Node gridSelecNode) throws PMException {
            // prohibitions
            if (gridSelecNode.getType() == NodeType.UA || gridSelecNode.getType() == NodeType.U) {
                List<Prohibition> outgoingList = g.getProhibitionsFor(gridSelecNode.getName());
                Iterator<Prohibition> outgoingIterator = outgoingList.iterator();
                if (!outgoingIterator.hasNext()) {
                    outgoingProhibitionList.add(new Paragraph("None"));
                } else {
                    while (outgoingIterator.hasNext()) {
                        Prohibition prohibition = outgoingIterator.next();
                        outgoingProhibitionList.add(new ProhibitonBlip(prohibition));
                    }
                }
            }

//            if (gridSelecNode.getType() == NodeType.UA || gridSelecNode.getType() == NodeType.OA
//                    || gridSelecNode.getType() == NodeType.U || gridSelecNode.getType() == NodeType.O) {
//                List<Prohibition> incomingList = SingletonGraph.getPap().getProhibitionsPAP().getAll();
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
//                                Node subject = SingletonGraph.getPap().getGraphPAP().getNode(prohibition.getSubject());
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

        public void updateGrid(Collection<Node> all_nodes){
            // TODO: filter to only have nodes in the active PC's
            Set<SingletonGraph.PolicyClassWithActive> pcs = SingletonGraph.getActivePCs();
            Set<Node> nodes_to_remove = new HashSet<>();

            for (Node node : all_nodes) {
                for (SingletonGraph.PolicyClassWithActive policyClassWithActive : pcs) {
                    if (node.getType() == NodeType.PC) {
                        if (policyClassWithActive.getName().equalsIgnoreCase(node.getName())) {
                            if (!policyClassWithActive.isActive()) {
                                //only remove PC's
                                nodes_to_remove.add(node);
                            }
                        }
                    } else {
                        if (node.getProperties().get("namespace") != null) {
                            if (!policyClassWithActive.isActive()) {
                                if (node.getProperties().get("namespace").equalsIgnoreCase(policyClassWithActive.getName())) {
                                    //remove nodes UA & OA
                                    nodes_to_remove.add(node);
                                }
                            }
                        }
                        if (node.getProperties().get("pc") != null) {
                            if (!policyClassWithActive.isActive()) {
                                if (node.getProperties().get("pc").equalsIgnoreCase(policyClassWithActive.getName())) {
                                    //remove nodes pc properties
                                    nodes_to_remove.add(node);
                                }
                            }
                        }
                    }
                }
            }
            all_nodes.removeAll(nodes_to_remove);


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
                                return g.getChildren(node.get().getName()).size();
                            } else {
                                return all_nodes.size();
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
                        return g.getChildren(item.getName()).size() > 0;
                    } catch (PMException e) {
                        e.printStackTrace();
                        MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                        return false;
                    }
                }

                @Override
                protected Stream<Node> fetchChildrenFromBackEnd(HierarchicalQuery<Node, Void> query) {
                    Collection<Node> children = new HashSet<>();
                    try{
                        if (g == null) {
                            System.out.println("Singleton Graph is null");
                        } else if (query == null) {
                            System.out.println("query is null");
                        } else {
                            Optional<Node> node = query.getParentOptional();
                            if (node.isPresent()) {
                                Set<String> childrenNames = g.getChildren(query.getParent().getName());
                                for (String name: childrenNames) {
                                    children.add(g.getNode(name));
                                }
                            } else {
                                children.addAll(all_nodes);
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

        public void refreshGraph() {
            currNodes = new HashSet<>();
            try {
                Set<String> pcNames = g.getPolicies();
                for (String name: pcNames) {
                    currNodes.add(g.getNode(name));
                }
            } catch (PMException e) {
                e.printStackTrace();
            }
            updateGrid(currNodes);
            grid.deselectAll();
            selectedParentNode = null;
            buttonGroup.refreshNodeTexts();
            buttonGroup.refreshButtonStates();
            backButton.setEnabled(false);
        }

        private void createContextMenu() {
            GridContextMenu<Node> contextMenu = new GridContextMenu<>(grid);

            //contextMenu.addItem("Add Node", event -> addNode());
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
        private Button addNodeButton, addUserButton, addObjectButton,
                    addAssignmentButton, deleteAssignmentButton,
                    addAssociationButton, editAssociationButton, deleteAssociationButton,
                    addProhibitionButton, editProhibitionButton, deleteProhibitionButton,
                    resetButton;
        private H4 parentNodeText, childNodeText;
        private Component connectorSymbol;
        public GraphButtonGroup() {
            getStyle().set("background", "#DADADA") //#A0FFA0
                      .set("overflow-y", "scroll");
            setWidth("20%");
            getStyle().set("height","100vh");
            setAlignItems(Alignment.CENTER);
            setJustifyContentMode(JustifyContentMode.START);

            childNodeText = new H4("X");
            connectorSymbol = new H6(new Icon(VaadinIcon.ARROW_DOWN));
            parentNodeText = new H4("X");

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
                    childNode.updateNodeInfoSection();
                    parentNode.updateNodeInfoSection();
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


            // Prohibition Buttons
            addProhibitionButton = new Button("Add Prohibition", evt -> {
                addProhibition();
            });
            addProhibitionButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            addProhibitionButton.setEnabled(false);
            addProhibitionButton.setWidthFull();
            add(addProhibitionButton);

            editProhibitionButton = new Button("Edit Prohibition", evt -> {
                editProhibition();
            });
            editProhibitionButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            editProhibitionButton.setEnabled(false);
            editProhibitionButton.setWidthFull();
            add(editProhibitionButton);

            deleteProhibitionButton = new Button("Delete Prohibition", evt -> {
                deleteProhibition();
            });
            deleteProhibitionButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteProhibitionButton.setEnabled(false);
            deleteProhibitionButton.setWidthFull();
            add(deleteProhibitionButton);
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
                    addAssignmentButton.setEnabled(true);
                    deleteAssignmentButton.setEnabled(true);
                } else {
                    addAssignmentButton.setEnabled(false);
                    deleteAssignmentButton.setEnabled(false);
                }


                if ((childType == NodeType.UA) && (parentType == NodeType.UA || parentType == NodeType.OA || parentType == NodeType.O)) {
                    addAssociationButton.setEnabled(true);
                    editAssociationButton.setEnabled(true);
                    deleteAssociationButton.setEnabled(true);
                } else {
                    addAssociationButton.setEnabled(false);
                    editAssociationButton.setEnabled(false);
                    deleteAssociationButton.setEnabled(false);
                }


                if ((childType == NodeType.UA || childType == NodeType.U) && (parentType == NodeType.OA || parentType == NodeType.O)) {
                    addProhibitionButton.setEnabled(true);
                    editProhibitionButton.setEnabled(true);
                    deleteProhibitionButton.setEnabled(true);
                } else {
                    addProhibitionButton.setEnabled(false);
                    editProhibitionButton.setEnabled(false);
                    deleteProhibitionButton.setEnabled(false);
                }
            } else {
                addAssignmentButton.setEnabled(false);
                deleteAssignmentButton.setEnabled(false);
                addAssociationButton.setEnabled(false);
                editAssociationButton.setEnabled(false);
                deleteAssociationButton.setEnabled(false);
                addProhibitionButton.setEnabled(false);
                editProhibitionButton.setEnabled(false);
                deleteProhibitionButton.setEnabled(false);
            }
        }
    }

    private void addNode() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(Alignment.BASELINE);

//        NumberField idField = new NumberField("ID");
//        idField.setRequiredIndicatorVisible(true);
//        idField.setValue(rand.nextLong() * 1.0);
//        idField.setMin(1);
//        idField.setHasControls(true);
//        form.add(idField);

        TextField nameField = new TextField("Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setPlaceholder("Enter Name...");
        form.add(nameField);

        NodeType[] types = new NodeType[4];
        types[0] = NodeType.U;
        types[1] = NodeType.UA;
        types[2] = NodeType.O;
        types[3] = NodeType.OA;
        Select<NodeType> typeSelect = new Select<>(types);
        typeSelect.setRequiredIndicatorVisible(true);
        typeSelect.setLabel("Type");
        typeSelect.setPlaceholder("Select Type...");
        form.add(typeSelect);

        Collection<Node> nodesCol;
        try {
            nodesCol = new HashSet<>(g.getActiveNodes());
        } catch (PMException e) {
            nodesCol = new HashSet<>();
            e.printStackTrace();
        }

        Select<Node> parentSelect = new Select<>();
        typeSelect.addValueChangeListener(event -> {
            Collection<Node> nodeCollection;
            try {
                nodeCollection = new HashSet<>(g.getActiveNodes());
            } catch (PMException e) {
                nodeCollection = new HashSet<>();
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
            }
            parentSelect.setItems(finalNodeCollection);
        });
        parentSelect.setRequiredIndicatorVisible(true);
        parentSelect.setLabel("Parent");
        parentSelect.setPlaceholder("Select a parent node...");
        parentSelect.setItemLabelGenerator(Node::getName);
        parentSelect.setItems(nodesCol);

        form.add(parentSelect);

        TextArea propsFeild = new TextArea("Properties (key=value \\n...)");
        propsFeild.setPlaceholder("Enter Properties...");
        form.add(propsFeild);

        Button button = new Button("Submit", event -> {
//            Long id = idField.getValue().longValue();
            String name = nameField.getValue();
            NodeType type = typeSelect.getValue();
            Node parent = parentSelect.getValue();
            String propString = propsFeild.getValue();
            Map<String, String> props = new HashMap<>();
//            if (id == null) {
//                idField.focus();
//                MainView.notify("ID is Required");
//            } else
            if (name == null || name == "") {
                nameField.focus();
                MainView.notify("Name is Required", MainView.NotificationType.DEFAULT);
            } else if (type == null) {
                typeSelect.focus();
                MainView.notify("Type is Required", MainView.NotificationType.DEFAULT);
            } else {
                if (propString != null && !propString.equals("")) {
                    try {
                        for (String prop : propString.split("\n")) {
                            props.put(prop.split("=")[0], prop.split("=")[1]);
                        }
                    } catch (Exception e) {
                        MainView.notify("Incorrect Formatting of Properties", MainView.NotificationType.ERROR);
                        e.printStackTrace();
                    }
                }
                try {
                    g.createNode(name, type, props, parent.getName());
                    MainView.notify("Node with name: " + name + " created", MainView.NotificationType.SUCCESS);
                    childNode.refreshGraph();
                    parentNode.refreshGraph();
                    dialog.close();
                } catch (PMException e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
            }
        });
        form.add(button);

        dialog.add(form);
        dialog.open();
        nameField.focus();
    }

    private void addUser() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(Alignment.BASELINE);

        TextField nameField = new TextField("Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setPlaceholder("Enter Name...");
        form.add(nameField);

        Collection<Node> nodeCollection;
        try {
            nodeCollection = new HashSet<>(g.getActiveNodes());

        } catch (PMException e) {
            nodeCollection = new HashSet<>();
            e.printStackTrace();
        }

        nodeCollection.removeIf(curr -> !(curr.getType() == NodeType.UA || curr.getType() == NodeType.PC));
        Select<Node> parentSelect = new Select<>();
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

        TextArea propsFeild = new TextArea("Properties (key=value \\n...)");
        propsFeild.setPlaceholder("Enter Properties...");
        form.add(propsFeild);

        Button button = new Button("Submit", event -> {
            String name = nameField.getValue();
            Node parent = parentSelect.getValue();
            String propString = propsFeild.getValue();
            Map<String, String> props = new HashMap<>();
            if (name == null || name == "") {
                nameField.focus();
                MainView.notify("Name is Required", MainView.NotificationType.DEFAULT);
            } else if (parent == null) {
                parentSelect.focus();
                MainView.notify("Parent is Required", MainView.NotificationType.DEFAULT);
            } else {
                if (propString != null && !propString.equals("")) {
                    try {
                        for (String prop : propString.split("\n")) {
                            props.put(prop.split("=")[0], prop.split("=")[1]);
                        }
                    } catch (Exception e) {
                        MainView.notify("Incorrect Formatting of Properties", MainView.NotificationType.ERROR);
                        e.printStackTrace();
                    }
                }
                try {
                    //Node home = g.createNode(name + " Home", NodeType.OA, props, SingletonGraph.getSuperOAId());
                    //Node attr = g.createNode(name + " Attr", NodeType.UA, props, SingletonGraph.getSuperUAId());
                    // What are those nodes used for ?
                    g.createNode(name, NodeType.U, props, parent.getName());
                    MainView.notify("User with name: " + name + " has been created", MainView.NotificationType.SUCCESS);
                    childNode.refreshGraph();
                    parentNode.refreshGraph();
                    dialog.close();

                } catch (Exception e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
            }
        });
        if (nodeCollection.size() == 0) {
            button.setEnabled(false);
        }
        form.add(button);

        dialog.add(form);
        dialog.open();
        nameField.focus();
    }

    private void addObject() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(Alignment.BASELINE);

        TextField nameField = new TextField("Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setPlaceholder("Enter Name...");
        form.add(nameField);

        Collection<Node> nodeCollection;
        try {
            //filter nodes
            g.getNodes();
            nodeCollection = new HashSet<>(g.getActiveNodes());
        } catch (PMException e) {
            nodeCollection = new HashSet<>();
            e.printStackTrace();
        }
        nodeCollection.removeIf(curr -> curr.getType() != NodeType.OA);
        ComboBox<Node> parentSelect = new ComboBox<Node>("Parent", nodeCollection);
        if (nodeCollection.size() == 0) {
            parentSelect.setEnabled(false);
        }
        parentSelect.setRequiredIndicatorVisible(true);
        parentSelect.setPlaceholder("Select OA...");
        form.add(parentSelect);

        TextArea propsFeild = new TextArea("Properties (key=value \\n...)");
        propsFeild.setPlaceholder("Enter Properties...");
        form.add(propsFeild);

        Button button = new Button("Submit", event -> {

            String name = nameField.getValue();
            Node parent = parentSelect.getValue();
            String propString = propsFeild.getValue();
            Map<String, String> props = new HashMap<>();
            if (name == null || name == "") {
                nameField.focus();
                MainView.notify("Name is Required", MainView.NotificationType.DEFAULT);
            } else if (parent == null) {
                parentSelect.focus();
                MainView.notify("Parent is Required", MainView.NotificationType.DEFAULT);
            } else {
                if (propString != null && !propString.equals("")) {
                    try {
                        for (String prop : propString.split("\n")) {
                            props.put(prop.split("=")[0], prop.split("=")[1]);
                        }
                    } catch (Exception e) {
                        MainView.notify("Incorrect Formatting of Properties", MainView.NotificationType.ERROR);
                        e.printStackTrace();
                    }
                }
                try {
                    g.createNode(name, NodeType.O, props, parent.getName());
                    MainView.notify("Object with name: " + name + " has been created", MainView.NotificationType.SUCCESS);
                    childNode.refreshGraph();
                    parentNode.refreshGraph();
                    dialog.close();
                } catch (Exception e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
            }
        });
        if (nodeCollection.size() == 0) {
            button.setEnabled(false);
        }
        form.add(button);

        dialog.add(form);
        dialog.open();
        nameField.focus();
    }

    private void editNode(Node n) {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(Alignment.BASELINE);

        TextField nameField = new TextField("Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setValue(n.getName());
        nameField.setEnabled(false);
        form.add(nameField);

        TextArea propsFeild = new TextArea("Properties (key=value \\n...)");
        propsFeild.setPlaceholder("Enter Properties...");
        String pStr = n.getProperties().toString().replaceAll(", ", "\n");
        propsFeild.setValue(pStr.substring(1,pStr.length()-1));
        form.add(propsFeild);

        Button button = new Button("Submit", event -> {
            String name = nameField.getValue();
            String propString = propsFeild.getValue();
            Map<String, String> props = new HashMap<>();
            if (name == null || name == "") {
                nameField.focus();
                MainView.notify("Name is Required", MainView.NotificationType.DEFAULT);
            } else {
                if (propString != null) {
                    try {
                        for (String prop : propString.split("\n")) {
                            props.put(prop.split("=")[0], prop.split("=")[1]);
                        }
                    } catch (Exception e) {
                        MainView.notify("Incorrect Formatting of Properties", MainView.NotificationType.ERROR);
                        e.printStackTrace();
                    }
                }
                try {
                    g.updateNode(name, props);
                    MainView.notify("Node with name: " + name + " has been edited", MainView.NotificationType.SUCCESS);
                    childNode.refreshGraph();
                    parentNode.refreshGraph();
                    dialog.close();
                } catch (Exception e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
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
        form.setAlignItems(Alignment.BASELINE);

        form.add(new Paragraph("Are You Sure?"));

        Button button = new Button("Delete", event -> {
            try {
                String name = n.getName();
                g.deleteNode(name);
                MainView.notify("Node with name: " + name + " has been deleted", MainView.NotificationType.SUCCESS);
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
//                MainView.notify("You have to delete all assignment on that node first.");
                e.printStackTrace();
            }
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
                g.assign(child.getName(), parent.getName());
                MainView.notify(child.getName() + " assigned to " + parent.getName(), MainView.NotificationType.SUCCESS);
            } catch (PMException e) {
                e.printStackTrace();
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
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
                    g.deassign(child.getName(), parent.getName());
                    MainView.notify(child.getName() + " un-assigned from " + parent.getName(), MainView.NotificationType.SUCCESS);
                } catch (PMException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
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
            MainView.notify("Must choose both a parent and a child for de-assignment", MainView.NotificationType.DEFAULT);
        }
    }

    private void addAssociation() {
        Dialog dialog = new Dialog();
        dialog.setWidth("75vh");
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(Alignment.BASELINE);

        MultiselectComboBox<String> opsSelectRessource = new MultiselectComboBox<>();
        opsSelectRessource.setLabel("Operations");
        opsSelectRessource.setPlaceholder("Resources operations");
        try {
            opsSelectRessource.setItems(g.getResourceOpsWithStars());
        } catch (PMException e) {
            e.printStackTrace();
        }
        opsSelectRessource.setWidth("100%");

        MultiselectComboBox<String> opsSelectAdmin = new MultiselectComboBox<>();
        opsSelectAdmin.setLabel("Operations");
        opsSelectAdmin.setPlaceholder("Admin operations");
        try {
            opsSelectAdmin.setItems(g.getAdminOpsWithStars());
        } catch (PMException e) {
            e.printStackTrace();
        }
        opsSelectAdmin.setWidth("100%");

        form.add(opsSelectRessource);
        form.add(opsSelectAdmin);

        Button submit = new Button("Submit", event -> {

            List<String> opString = new ArrayList<>();
            opString.addAll(opsSelectRessource.getValue());
            opString.addAll(opsSelectAdmin.getValue());
            OperationSet ops = new OperationSet();
            if (opString == null || opString.equals("")) {
                MainView.notify("Operations are Required", MainView.NotificationType.DEFAULT);
            } else {
                try {
                    ops.addAll(opString);
                } catch (Exception e) {
                    MainView.notify("Incorrect Formatting of Operations", MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
                try {
                    g.associate(selectedChildNode.getName(), selectedParentNode.getName(), ops);
                    MainView.notify(selectedChildNode.getName() + " assigned to " + selectedParentNode.getName(), MainView.NotificationType.SUCCESS);
                    dialog.close();
                } catch (Exception e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
            }
        });
        submit.setWidth("20vh");
        form.add(submit);

        dialog.add(form);
        dialog.open();
    }

    private void editAssociation() {
        Dialog dialog = new Dialog();
        dialog.setWidth("75vh");
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(Alignment.BASELINE);

        MultiselectComboBox<String> opsSelectRessource = new MultiselectComboBox<>();
        opsSelectRessource.setLabel("Operations");
        opsSelectRessource.setPlaceholder("Resources operations");
        try {
            opsSelectRessource.setItems(g.getResourceOpsWithStars());
        } catch (PMException e) {
            e.printStackTrace();
        }
        opsSelectRessource.setWidth("100%");

        MultiselectComboBox<String> opsSelectAdmin = new MultiselectComboBox<>();
        opsSelectAdmin.setLabel("Admin");
        opsSelectAdmin.setPlaceholder("Admin operations");
        try {
            opsSelectAdmin.setItems(g.getAdminOpsWithStars());
        } catch (PMException e) {
            e.printStackTrace();
        }
        opsSelectAdmin.setWidth("100%");

        form.add(opsSelectRessource);
        form.add(opsSelectAdmin);
        try {
            if (selectedChildNode.getType() == NodeType.UA) {
                Map<String, OperationSet> sourceOps = g.getSourceAssociations(selectedChildNode.getName());
                Set<String> sourceToTargetOps = new HashSet<>();
                sourceOps.forEach((targetName, targetOps) -> {
                    if (targetName.equalsIgnoreCase(selectedParentNode.getName())) {
                        sourceToTargetOps.addAll(targetOps);
                    }
                });
                System.out.println("ops = " + sourceToTargetOps);
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

        Button submit = new Button("Submit", event -> {

            List<String> opString = new ArrayList<>();
            opString.addAll(opsSelectRessource.getValue());
            opString.addAll(opsSelectAdmin.getValue());
            OperationSet ops = new OperationSet();
            if (opString == null || opString.equals("")) {
                MainView.notify("Operations are Required", MainView.NotificationType.DEFAULT);
            } else {
                try {
                    ops.addAll(opString);
                } catch (Exception e) {
                    MainView.notify("Incorrect Formatting of Operations", MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
                try {
                    g.associate(selectedChildNode.getName(), selectedParentNode.getName(), ops);
                    MainView.notify("Association between " + selectedChildNode.getName() + " and " + selectedParentNode.getName() + " has been modified", MainView.NotificationType.SUCCESS);
                    dialog.close();
                } catch (Exception e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
            }
        });
        submit.setWidth("20vh");
        form.add(submit);

        dialog.add(form);
        dialog.open();
    }

    private void deleteAssociation() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(Alignment.BASELINE);

        form.add(new Paragraph("Are You Sure?"));

        Button button = new Button("Delete", event -> {
            try {
                g.dissociate(selectedChildNode.getName(), selectedParentNode.getName());
                MainView.notify("Association between " + selectedChildNode.getName() + " and " + selectedParentNode.getName() + " has been deleted", MainView.NotificationType.SUCCESS);
            } catch (PMException e) {
                System.out.println(e.getMessage());
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

        dialog.add(form);
        dialog.open();
    }

    private void addProhibition() {
        Dialog dialog = new Dialog();

        HorizontalLayout form = new HorizontalLayout();
        // form.setAlignItems(Alignment.BASELINE);

        TextField nameField = new TextField("Prohibition Name");
        int numOfProhibtionsForSubject = 0;
        try {
            numOfProhibtionsForSubject = g.getProhibitionsFor(selectedChildNode.getName()).size();
        } catch (PMException e) {
            e.printStackTrace();
        }
        String initialName = "deny_" + selectedChildNode.getName() + "-" + (numOfProhibtionsForSubject + 1);
        nameField.setValue(initialName);
        form.add(nameField);

        TextArea opsFeild = new TextArea("Operations (Op1, Op2, ...)");
        opsFeild.setPlaceholder("Enter Operations...");
        form.add(opsFeild);

        MapInput<String, Boolean> containerField = new MapInput<>(TextField.class, Checkbox.class);
        containerField.setLabel("Containers (Target, Complement)");
        containerField.setInputRowValues(selectedParentNode.getName(), false);
        form.add (containerField);

        Checkbox intersectionFeild = new Checkbox("Intersection");
        VerticalLayout intersectionFeildLayout = new VerticalLayout(intersectionFeild);
        form.add(intersectionFeildLayout);

        Button submit = new Button("Submit", event -> {
            String name = nameField.getValue();
            String opString = opsFeild.getValue();
            OperationSet ops = new OperationSet();
            boolean intersection = intersectionFeild.getValue();
            Map<String, Boolean> containers = containerField.getValue();
            if (opString == null || opString.equals("")) {
                opsFeild.focus();
                MainView.notify("Operations are Required", MainView.NotificationType.DEFAULT);
            } else if (name == null || name.equals("")) {
                nameField.focus();
                MainView.notify("Name is Required", MainView.NotificationType.DEFAULT);
            } else if (containers.isEmpty()) {
                MainView.notify("Containers are Required", MainView.NotificationType.DEFAULT);
            } else {
                try {
                    for (String op : opString.split(",")) {
                        ops.add(op.replaceAll(" ", ""));
                    }
                } catch (Exception e) {
                    MainView.notify("Incorrect Formatting of Operations", MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
                try {
                    g.addProhibition(nameField.getValue(), selectedChildNode.getName(), containers, ops, intersection);
                    MainView.notify("Prohibition with name: " + nameField.getValue() + " has been created", MainView.NotificationType.SUCCESS);
                    dialog.close();
                } catch (PMException e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
            }
        });
        VerticalLayout submitLayout = new VerticalLayout(submit);
        form.add(submitLayout);

        dialog.add(form);
        dialog.open();
        opsFeild.focus();
    }

    private void deleteProhibition() {
        // TODO: is currently broken
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);

        form.add(new Paragraph("Are You Sure?"));

        Button button = new Button("Delete", event -> {
            try {
                g.deleteProhibition(selectedChildNode.getName());
                MainView.notify("Prohibition with name: " + selectedChildNode.getName() + " has been deleted", MainView.NotificationType.SUCCESS);

//                System.out.println("Deleting prohibition between " + selectedChildNode.getName() + "-" + selectedChildNode.getType()+ " AND " + selectedParentNode.getName());
//                List<Prohibition> prohibtions = g.getProhibitionsFor(selectedChildNode.getName());
//                prohibtions.removeIf(prohibition -> !prohibition.getSubject().equals(selectedParentNode.getName()));
//                if (!prohibtions.isEmpty()) {
//                    for (Prohibition p: prohibtions) {
//                        g.deleteProhibition(p.getName());
//                    }
//                }
            } catch (PMException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            dialog.close();
        });
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        form.add(button);

        Button cancel = new Button("Cancel", event -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        form.add(cancel);

        dialog.add(form);
        dialog.open();
    }

    private void editProhibition() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);

        // getting previous values
        String sourceToTargetOpsString = "";
        boolean intersectionOldValue = false;
        try {
            Set<String> sourceToTargetOps = new HashSet<>();
            List<Prohibition> prohibtions = g.getProhibitionsFor(selectedParentNode.getName());
            for (Prohibition p: prohibtions) {
                if (p.getName().equals(selectedChildNode.getName())) {
                    sourceToTargetOps.addAll(p.getOperations());
                    intersectionOldValue = p.isIntersection();
                }
            }

            sourceToTargetOpsString = sourceToTargetOps.toString();
            sourceToTargetOpsString = sourceToTargetOpsString.substring(1, sourceToTargetOpsString.length() - 1);
        } catch (PMException e) {
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }

        TextArea opsFeild = new TextArea("Operations (Op1, Op2, ...)");
        opsFeild.setValue(sourceToTargetOpsString);
        opsFeild.setPlaceholder("Enter Operations...");
        form.add(opsFeild);

        Checkbox intersectionFeild = new Checkbox("Intersection");
        intersectionFeild.setValue(intersectionOldValue);
        form.add(intersectionFeild);


        Button submit = new Button("Submit", event -> {
            String opString = opsFeild.getValue();
            OperationSet ops = new OperationSet();
            boolean intersection = intersectionFeild.getValue();
            if (opString == null || opString.equals("")) {
                opsFeild.focus();
                MainView.notify("Operations are Required", MainView.NotificationType.DEFAULT);
            } else {
                try {
                    for (String op : opString.split(",")) {
                        ops.add(op.replaceAll(" ", ""));
                    }
                } catch (Exception e) {
                    MainView.notify("Incorrect Formatting of Operations", MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
                try {
//                    g.updateProhibition(selectedChildNode.getName(), selectedChildNode.getName(), selectedParentNode.getName(), ops, intersection);
//                    MainView.notify("Prohibition with name: " + );
                    dialog.close();
                } catch (Exception e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
            }
        });
        form.add(submit);

        dialog.add(form);
        dialog.open();
        opsFeild.focus();
    }

    private void resetGraph() {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(Alignment.BASELINE);

        form.add(new Paragraph("Are You Sure?"));

        Button button = new Button("Delete", event -> {
            try {
                g.reset();
                SingletonGraph.resetActivePCs();
                MainView.notify("Graph has been reset", MainView.NotificationType.SUCCESS);
                childNode.refreshGraph();
                parentNode.refreshGraph();
            } catch (PMException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            dialog.close();
        });
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        form.add(button);

        Button cancel = new Button("Cancel", event -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        form.add(cancel);

        dialog.add(form);
        dialog.open();

    }

}


