package gov.nist.csd.pm.admintool.app.testingApps;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.ValueProvider;
import gov.nist.csd.pm.admintool.app.MainView;
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

import java.util.*;

public class POSTester extends VerticalLayout {
    private Select<Node> userSelect;
    private Grid<Node> grid;
    private Node user;
    private SingletonGraph g;
    private static Random rand = new Random();


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
                MainView.notify("User is required!");
            } else {
                user = selectedUser;
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
        contextMenu.addItem("Explain", event -> {
            event.getItem().ifPresent(node -> {
                explain(user, node);
            });
        });
        ///// end of context menu /////

        grid.setItems(new HashSet<>());
        grid.setColumnReorderingAllowed(true);
        grid.removeColumnByKey("id");
        grid.removeColumnByKey("properties");
        grid.getColumnByKey("name")
                .setFlexGrow(2)
                .setResizable(true);

        ValueProvider<Node, String> permissionsValueProvider = (node) -> getPermissions(user, node).toString();
        grid.addColumn(permissionsValueProvider)
                .setHeader("Permissions")
                .setKey("permissions");
        grid.getColumnByKey("permissions")
                .setFlexGrow(2);

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
            permissions = g.getAnalyticsService(new UserContext(user.getName())).getPermissions(node.getName());
        } catch (PMException e) {
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
            System.out.println(e.getMessage());

            e.printStackTrace();
        }
        Iterator<Node> nodeIterator = nodeCollection.iterator();
        while (nodeIterator.hasNext()) {
            Node curr = nodeIterator.next();
            if (!(curr.getType() == NodeType.U || curr.getType() == NodeType.UA) || curr.getProperties().get("namespace") == "super") {
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
                UserContext userContext = new UserContext(user.getName(), rand.toString());
                currNodes = g.getGraphService(userContext).getNodes();
            } catch (PMException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            grid.setItems(currNodes);
        } else {
            MainView.notify("Select a User");
        }
    }

    private void editNode(Node n) {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);


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
            String name = nameField.getValue();
            String propString = propsFeild.getValue();
            Map<String, String> props = new HashMap<>();
            if (name == null || name == "") {
                nameField.focus();
                MainView.notify("Name is Required");
            } else {
                if (propString != null) {
                    try {
                        for (String prop : propString.split("\n")) {
                            props.put(prop.split("=")[0], prop.split("=")[1]);
                        }
                    } catch (Exception e) {
                        MainView.notify("Incorrect Formatting of Properties");
                        e.printStackTrace();
                    }
                }
                try {
                    g.updateNode(name, props);
                    updateGraph();
                    dialog.close();
                } catch (Exception e) {
                    MainView.notify(e.getMessage());
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
                g.deleteNode(n.getName());
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

    private void explain(Node user, Node node) {
        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);


        AnalyticsService analyticsService = g.getAnalyticsService(new UserContext(user.getName()));
        String explanation;
        Explain explain = null;

        try {
            explain = analyticsService.explain(user.getName(), node.getName());
        } catch (PMException e) {
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
                    ret += " " + pathOps;
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
}
