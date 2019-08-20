package gov.nist.csd.pm.admintool.app.testingApps;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;

import java.io.Serializable;
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
                notify("Attribute is required!");
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

        grid.removeColumnByKey("ID");
        grid.removeColumnByKey("properties");

        add(grid);
    }

    public Node[] getAttrs() {
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
            if (!(curr.getType() == NodeType.UA || curr.getType() == NodeType.OA) || curr.getProperties().get("namespace") == "super") {
                nodeIterator.remove();
            }
        }
        Node nodes[] = nodeCollection.toArray(new Node[nodeCollection.size()]);
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
                Map<Long, Set<String>> targetAssociations = g.getTargetAssociations(attr.getID());
                for (long id: targetAssociations.keySet()) {
                    updateGraphRecursiveHelper(g.getNode(id), targetAssociations.get(id), targetAssociations, currNodes);
//                    currNodes.add(new NodeAndPermissions(g.getNode(id), targetAssociations.get(id)));
//                    for (Node n: g.getChildren(id)) {
//                        currNodes.add(new NodeAndPermissions(n, targetAssociations.get(id)));
//                    }
                }
            } catch (PMException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            grid.setItems(currNodes);
        } else {
            notify("Select an Attribute");
        }
    }

    private void updateGraphRecursiveHelper(Node n, Set<String> perms, Map<Long, Set<String>> targetAssociations, Set<NodeAndPermissions> nodes) throws PMException{
        NodeAndPermissions currNodeAndPermissions = new NodeAndPermissions(n, perms);
        if (!nodes.contains(currNodeAndPermissions)) {
            nodes.add(currNodeAndPermissions);
            for (Node child: g.getChildren(currNodeAndPermissions.getID())) {
                HashSet<String> childPerms = new HashSet<>();
                childPerms.addAll(perms);
                Set<String> fromAssoc = targetAssociations.get(child.getID());
                if (fromAssoc != null) {
                    childPerms.addAll(fromAssoc);
                }
                updateGraphRecursiveHelper(child, childPerms, targetAssociations, nodes);
            }
        }
    }

    public void notify(String message) {
        Notification notif = new Notification(message, 3000);
        notif.open();
    }

    public class NodeAndPermissions extends Node {
        private Set<String> permissions;

        public NodeAndPermissions(Node node, Set<String> perms) {
            super (node.getID(), node.getName(), node.getType(), node.getProperties());
            permissions = perms;
        }

        public Set<String> getPermissions() {
            return permissions;
        }
    }
}