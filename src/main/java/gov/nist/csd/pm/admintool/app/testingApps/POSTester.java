package gov.nist.csd.pm.admintool.app.testingApps;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.function.ValueProvider;
import gov.nist.csd.pm.admintool.app.MainView;
import gov.nist.csd.pm.admintool.app.TitleFactory;
import gov.nist.csd.pm.admintool.app.customElements.MapInput;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.pdp.audit.model.Explain;
import gov.nist.csd.pm.pdp.audit.model.Path;
import gov.nist.csd.pm.pdp.audit.model.PolicyClass;
import gov.nist.csd.pm.pdp.services.AnalyticsService;
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.*;
import java.util.stream.Stream;

public class POSTester extends VerticalLayout {
    private Node user;
    private SingletonGraph g;
    private Set<Node> currNodes;
    private static Random rand = new Random();

    private HorizontalLayout optionsForm;
    private Select<Node> userSelect;
    private Button submitButton, delegateButton;
    private TreeGrid<Node> grid;


    public POSTester () {
        H2 importTitle = new H2("POS:");
        importTitle.getStyle().set("margin-bottom","0");
        add(importTitle);

        setWidthFull();
        setHeightFull();
//        setAlignItems(Alignment.CENTER);
        setAlignItems(Alignment.STRETCH);
        setJustifyContentMode(JustifyContentMode.START);

        getStyle().set("background", "lightblue");

        currNodes= null;
        user = null;

        g = SingletonGraph.getInstance();
        userSelect = new Select<>();
        grid = new TreeGrid<>(Node.class);

        addOptionsSelectForm();
        addGrid();
    }

    private void addOptionsSelectForm() {
        optionsForm = new HorizontalLayout();
        optionsForm.setAlignItems(FlexComponent.Alignment.BASELINE);
        optionsForm.setWidthFull();
        optionsForm.setMargin(false);

        // select box
        setUserSelect();
        userSelect.setRequiredIndicatorVisible(true);
        userSelect.setLabel("Choose User");
        userSelect.setPlaceholder("Select an option");
        userSelect.setEmptySelectionCaption("Select an option");
        userSelect.setEmptySelectionAllowed(true);
        userSelect.setItemEnabledProvider(Objects::nonNull);
        userSelect.addComponents(null, new Hr());
        userSelect.addValueChangeListener(evt -> {
            user = evt.getValue();
            updateGraph();
            submitButton.setEnabled(evt.getValue() != null);
            delegateButton.setEnabled(evt.getValue() != null);
        });
        optionsForm.add(userSelect);

        // submit button
        submitButton = new Button("Update POS", event -> {
            if (user == null) {
                MainView.notify("User is required!", MainView.NotificationType.DEFAULT);
            } else {
                updateGraph();
            }
        });
        submitButton.setEnabled(false);
        optionsForm.add(submitButton);

        // delegate button
        delegateButton = new Button("Delegate", event -> {
            delegate(user, null);
        });
        delegateButton.setEnabled(false);
        optionsForm.add(delegateButton);

        add(optionsForm);
    }

    private void addGrid() {
        // grid config
        grid.getStyle()
                .set("border-radius", "2px")
                .set("user-select", "none");

        ///// context menu /////
        GridContextMenu<Node> contextMenu = new GridContextMenu<>(grid);
        contextMenu.addItem("Explain", event -> {
            event.getItem().ifPresent(node -> {
                explain(user, node);
            });
        });

        contextMenu.addItem("Delegate", event -> {
            event.getItem().ifPresent(node -> {
                delegate(user, node);
            });
        });
        ///// end of context menu /////

        grid.setColumnReorderingAllowed(true);
        grid.removeColumnByKey("id");
        grid.removeColumnByKey("properties");
        grid.setHierarchyColumn("name");
        grid.getColumnByKey("name")
                .setFlexGrow(2)
                .setResizable(true);

        ValueProvider<Node, String> permissionsValueProvider = (node) -> getPermissions(user, node).toString();
        grid.addColumn(permissionsValueProvider)
                .setHeader("Permissions")
                .setKey("permissions");
        grid.getColumnByKey("permissions")
                .setFlexGrow(2)
                .setResizable(true);

        grid.removeColumnByKey("type");
        grid.addColumn(Node::getType)
                .setHeader("Type")
                .setKey("type");
        grid.getColumnByKey("type")
                .setTextAlign(ColumnTextAlign.END)
                .setWidth("20%");

        add(grid);
    }

    public Set<String> getPermissions(Node user, Node node) {
        Set<String> permissions = new HashSet<>();
        permissions.add("None");

        try {
            permissions = g.getPDP().getAnalyticsService(new UserContext(user.getName())).getPermissions(node.getName());
        } catch (PMException e) {
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }

        return permissions;
    }

    public Node[] getUsers() {
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
//            if (!(curr.getType() == NodeType.U || curr.getType() == NodeType.UA) || curr.getProperties().get("namespace") == "super") {
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
            currNodes = new HashSet<>();
            try {
                UserContext userContext = new UserContext(user.getName(), rand.toString());
                currNodes = g.getPDP().getGraphService(userContext).getNodes();
//                currNodes = g.getPDP().getAnalyticsService(userContext).getPos(userContext);
            } catch (PMException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }

            updateGrid(currNodes);
            grid.deselectAll();
        } else {
            MainView.notify("Select a User", MainView.NotificationType.DEFAULT);
        }
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
                        if (node.isPresent() && user != null) {
                            UserContext userContext = new UserContext(user.getName(), rand.toString());
                            return g.getPDP().getGraphService(userContext).getChildren(node.get().getName()).size();
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
                    UserContext userContext = new UserContext(user.getName(), rand.toString());
                    return g.getPDP().getGraphService(userContext).getChildren(item.getName()).size() > 0;
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
                        if (node.isPresent() && user != null) {
                            UserContext userContext = new UserContext(user.getName(), rand.toString());
                            Set<String> childrenNames = g.getPDP().getGraphService(userContext).getChildren(query.getParent().getName());
                            System.out.println("getting children nodes");
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

    private void explain(Node user, Node node) {
        Dialog dialog = new Dialog();
        dialog.setHeight("75%");
        dialog.getElement().getStyle()
                .set("overflow-y", "scroll");
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);


        AnalyticsService analyticsService = g.getPDP().getAnalyticsService(new UserContext(user.getName()));
        String explanation;
        Explain explain = null;

        try {
            explain = analyticsService.explain(user.getName(), node.getName());
        } catch (PMException e) {
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }

        if (explain != null) {
            String ret = "";
            // Explain returns two things:
            //  1. The permissions the user has on the target
            //  2. A breakdown of permissions per policy class and paths in each policy class
            ret +=  "'" + user.getName() + "' has the following permissions on the target '" + node.getName() + "': \n";
            Set<String> permissions = explain.getPermissions();
            for (String perm: permissions) {
                ret += "\t- " + perm + "\n";
            }
            ret += "\n";


            // policyClasses maps the name of a policy class node to a Policy Class object
            // a policy class object contains the permissions the user has on the target node
            //   in that policy class
            ret += "The following section shows a more detailed permission breakdown from the perspective of each policy class:\n";
            Map<String, PolicyClass> policyClasses = explain.getPolicyClasses();
            int i = 1;
            for (String pcName : policyClasses.keySet()) {
                ret += "\t" + i + ". '" + pcName + "':\n";
                PolicyClass policyClass = policyClasses.get(pcName);

                // the operations available to the user on the target under this policy class
                Set<String> operations = policyClass.getOperations();
                ret += "\t\t- Permissions (Given by this PC):\n";
                for (String op: operations) {
                    ret += "\t\t\t- " + op + "\n";
                }
                // the paths from the user to the target
                // A Path object contains the path and the permissions the path provides
                // the path is just a list of nodes starting at the user and ending at the target node
                // example: u1 -> ua1 -> oa1 -> o1 [read]
                //   the association ua1 -> oa1 has the permission [read]
                ret += "\t\t- Paths (How each permission is found):\n";
                Set<Path> paths = policyClass.getPaths();
                for (Path path : paths) {
                    ret += "\t\t\t";
                    // this is just a list of nodes -> [u1, ua1, oa1, o1]
                    List<Node> nodes = path.getNodes();
                    for (Node n: nodes) {
                        ret += "'" + n.getName() + "'";
                        if (!nodes.get(nodes.size()-1).equals(n)) { // not final node
                            ret += " > ";
                        }
                    }

                    // this is the operations in the association between ua1 and oa1
                    Set<String> pathOps = path.getOperations();
                    ret += ":\n\t\t\t\t" + pathOps;
                    // This is the string representation of the path (i.e. "u1-ua1-oa1-o1 ops=[r, w]")
                    String pathString = path.toString();
                    ret += "\n";
                }
                i++;
            }

            explanation = ret;
        } else {
            explanation = "Returned Audit was null";
        }

        //explanation = explain.toString();

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
        form.add(auditLayout);


        Button ok = new Button("Ok", event -> dialog.close());
        ok.setHeightFull();
        form.add(ok);

        dialog.add(form);
        dialog.open();
    }

    // todo: read through the doc to see what delegation operations are allowed.
    private void delegate(Node user, Node selectedNode) {
        if (user == null) {
            MainView.notify("User is required!", MainView.NotificationType.DEFAULT);
        } else {
            // if the user has not submitted once before delegating
            if (currNodes == null) {
                updateGraph();
            }

            // actual UI
            Dialog dialog = new Dialog();
            HorizontalLayout form = new HorizontalLayout();

            // parent select
            Select<Node> sourceNodeSelect = new Select<>();
            sourceNodeSelect.setRequiredIndicatorVisible(true);
            sourceNodeSelect.setLabel("Source");
            sourceNodeSelect.setPlaceholder("Select a Source Node...");
            sourceNodeSelect.setItemLabelGenerator(Node::getName);
            sourceNodeSelect.setItems(currNodes.stream().filter((n) -> n.getType() == NodeType.U || n.getType() == NodeType.UA));
            if (selectedNode != null) {
                sourceNodeSelect.setValue(selectedNode);
            }
            form.add(sourceNodeSelect);

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

            // objects multi-selectors
            MultiselectComboBox<Node> destinationNodeMultiSelect = new MultiselectComboBox<>();
            destinationNodeMultiSelect.setRequiredIndicatorVisible(true);
            destinationNodeMultiSelect.setLabel("Destinations");
            destinationNodeMultiSelect.setPlaceholder("Select Destination Nodes...");
            destinationNodeMultiSelect.setItemLabelGenerator(Node::getName);
            destinationNodeMultiSelect.setItems(currNodes.stream().filter((n) -> n.getType() == NodeType.O || n.getType() == NodeType.OA));
            form.add(destinationNodeMultiSelect);

            // ----- Title Section -----
            Button submitButton = new Button("Submit", event -> {
                Node source = sourceNodeSelect.getValue();
                OperationSet ops = new OperationSet(rOpsField.getValue());
                ops.addAll(aOpsField.getValue());
                Set<Node> destinations = destinationNodeMultiSelect.getSelectedItems();

                try {
                    if (source == null) {
                        sourceNodeSelect.focus();
                        MainView.notify("Source is Required", MainView.NotificationType.DEFAULT);
                    } else if (ops == null || ops.isEmpty()) {
                        MainView.notify("Operations are Required", MainView.NotificationType.DEFAULT);
                    } else if (destinations == null || destinations.isEmpty()) {
                        MainView.notify("Destinations are Required", MainView.NotificationType.DEFAULT);
                    } else {
                        // use selected user context to make association
                        UserContext selectedUserContext = new UserContext(user.getName());
                        for (Node dest: destinations) {
                            g.getPDP().getGraphService(selectedUserContext).associate(source.getName(), dest.getName(), ops);
                            MainView.notify(user.getName() + " delegated user, " + source.getName()
                                    + ", with " + ops + " operations " + " on object, " + dest.getName() + ".", MainView.NotificationType.SUCCESS);
                        }
                        dialog.close();
                    }
                } catch (Exception e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
            });
            HorizontalLayout titleLayout = TitleFactory.generate("Delegate", submitButton);

            dialog.add(titleLayout, new Hr(), form);
            dialog.open();
            sourceNodeSelect.focus();


        }
    }
}
