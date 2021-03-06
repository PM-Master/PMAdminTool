package gov.nist.csd.pm.admintool.app.testingApps;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import gov.nist.csd.pm.admintool.app.MainView;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;

import java.util.*;

public class ACLTester extends VerticalLayout {
    private Select<Node> attrSelect;
    private Grid<NodeAndPermissions> grid;
    private Node attr;
    private SingletonGraph g;

    public ACLTester () {
        setPadding(false);
        setMargin(false);
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        g = SingletonGraph.getInstance();
        attrSelect = new Select<>();
        grid = new Grid<>(NodeAndPermissions.class);
        attr = null;

        addAttrSelectForm();
        addGrid();
    }

    private void addAttrSelectForm() {
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);
        form.setWidthFull();
        form.setMargin(false);

        // actual select box
        setAttrSelect();
        attrSelect.setRequiredIndicatorVisible(true);
        attrSelect.setLabel("Choose Attribute");
        attrSelect.setPlaceholder("Select an attribute");
        attrSelect.setEmptySelectionCaption("Select an attribute");
        attrSelect.setEmptySelectionAllowed(true);
        attrSelect.setItemEnabledProvider(Objects::nonNull);

        attrSelect.addComponents(null, new Hr());
        form.add(attrSelect);

        // actual submit button
        Button submit = new Button("Update ACL", event -> {
            Node selectedUser = attrSelect.getValue();
            if (selectedUser == null) {
                MainView.notify("Attribute is required!", MainView.NotificationType.DEFAULT);
            } else {
                attr = selectedUser;
                updateGraph();
            }
        });
        form.add(submit);

        add(form);
    }

    private void addGrid() {
        // grid config
        grid.getStyle()
                .set("border-radius", "2px");

        grid.setItems(new HashSet<>());
        grid.setColumnReorderingAllowed(true);
        grid.getColumns().forEach(col -> {
            col.setFlexGrow(1);
        });

        //grid.removeColumnByKey("ID");
        grid.removeColumnByKey("properties");

        add(grid);
    }

    public Node[] getAttrs() {
        Collection<Node> nodeCollection;
        try {
            nodeCollection = new HashSet<>(g.getNodes());
        } catch (PMException e) {
            nodeCollection = new HashSet<>();
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }
        Iterator<Node> nodeIterator = nodeCollection.iterator();
        while (nodeIterator.hasNext()) {
            Node curr = nodeIterator.next();
            if (!(curr.getType() == NodeType.UA || curr.getType() == NodeType.OA) || curr.getProperties().get("namespace") == "super") {
                nodeIterator.remove();
            }
        }
        Node[] nodes = nodeCollection.toArray(new Node[nodeCollection.size()]);
        return nodes;
    }

    public void setAttrSelect() {
        attrSelect.setItems(getAttrs());
    }


    // todo: make sure this works for cascading UAs (should inherit permissions of upper UA's)
    public void updateGraph() {
        if (attr != null) {
            Set<NodeAndPermissions> currNodes = new HashSet<>();
            try {
                Map<String, OperationSet> targetAssociations = g.getTargetAssociations(attr.getName());
                for (String id: targetAssociations.keySet()) {
                    updateGraphRecursiveHelper(g.getNode(id), targetAssociations.get(id), targetAssociations, currNodes);
//                    currNodes.add(new NodeAndPermissions(g.getNode(id), targetAssociations.get(id)));
//                    for (Node n: g.getChildren(id)) {
//                        currNodes.add(new NodeAndPermissions(n, targetAssociations.get(id)));
//                    }
                }
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            grid.setItems(currNodes);
        } else {
            MainView.notify("Select an Attribute", MainView.NotificationType.DEFAULT);
        }
    }

    private void updateGraphRecursiveHelper(Node n, Set<String> perms, Map<String, OperationSet> targetAssociations, Set<NodeAndPermissions> nodes) throws PMException{
        NodeAndPermissions currNodeAndPermissions = new NodeAndPermissions(n, perms);
        if (!nodes.contains(currNodeAndPermissions)) {
            nodes.add(currNodeAndPermissions);
            for (String child: g.getChildren(currNodeAndPermissions.getName())) {
                HashSet<String> childPerms = new HashSet<>();
                childPerms.addAll(perms);
                Set<String> fromAssoc = targetAssociations.get(child);
                if (fromAssoc != null) {
                    childPerms.addAll(fromAssoc);
                }
                updateGraphRecursiveHelper(g.getNode(child), childPerms, targetAssociations, nodes);
            }
        }
    }

    public class NodeAndPermissions extends Node {
        private Set<String> permissions;

        public NodeAndPermissions(Node node, Set<String> perms) {
            super (node.getId(), node.getName(), node.getType(), node.getProperties());
            permissions = perms;
        }

        public Set<String> getPermissions() {
            return permissions;
        }
    }
}
