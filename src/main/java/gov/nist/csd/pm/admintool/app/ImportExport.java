package gov.nist.csd.pm.admintool.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.pip.graph.Graph;
import gov.nist.csd.pm.pip.graph.MemGraph;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static gov.nist.csd.pm.operations.Operations.*;

@Tag("import-export")
public class ImportExport extends VerticalLayout {
    private SingletonGraph g;
    private HorizontalLayout layout;
    private ImportLayout importLayout;
    private ExportLayout exportLayout;

    public ImportExport() throws PMException {
        g = SingletonGraph.getInstance();

        // check permission
        if (!g.checkPermissions("super_pc_rep", TO_JSON, FROM_JSON))
            throw new PMException("Current user ('" + g.getCurrentContext() + "') does not have adequate permissions to use import export");

        layout = new HorizontalLayout();
        layout.setFlexGrow(1.0);
        add(layout);
        setUpLayout();
    }

    private void setUpLayout() {
        setSizeFull();
        setPadding(true);

        importLayout = new ImportLayout();
        importLayout.setWidth("50%");
        importLayout.getStyle().set("height","100vh");
        layout.add(importLayout);

        exportLayout = new ExportLayout();
        exportLayout.setWidth("50%");
        exportLayout.getStyle().set("height","100vh");
        layout.add(exportLayout);
    }
    //            add(new Paragraph(g.getGraphService().getNode(userCtx,-1).getName()));
    // todo: make sure this works
    public void updateGraph (String json) {
        System.out.println("Importing following JSON now ........................... ");
        System.out.println(json);

/*        try {
            //g = g.updateGraph(GraphSerializer.fromJson(g.getPAP().getGraphPAP(), json));
        } catch (PMException e) {
            e.printStackTrace();
        }*/
    }

    private class ImportLayout extends VerticalLayout {
        public ImportLayout () {
            getStyle().set("background", "lightblue");
            setAlignItems(Alignment.STRETCH);
            H2 importTitle = new H2("Import:");
            importTitle.getStyle().set("margin-bottom","0");
            add(importTitle);

            TextArea inputJson = new TextArea();
            inputJson.setValue("{\n" +
                    "  \"nodes\": [\n" +
                    "    {\n" +
                    "      \"name\": \"Super PC\",\n" +
                    "      \"type\": \"PC\",\n" +
                    "      \"properties\": {}\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"Bob Home\",\n" +
                    "      \"type\": \"OA\",\n" +
                    "      \"properties\": {}\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"Bob Attr\",\n" +
                    "      \"type\": \"UA\",\n" +
                    "      \"properties\": {}\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"Bob\",\n" +
                    "      \"type\": \"U\",\n" +
                    "      \"properties\": {}\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"Doc\",\n" +
                    "      \"type\": \"O\",\n" +
                    "      \"properties\": {}\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"assignments\": [\n" +
                    "    [\n" +
                    "      \"Bob Home\",\n" +
                    "      \"Super PC\"\n" +
                    "    ],\n" +
                    "    [\n" +
                    "      \"Bob\",\n" +
                    "      \"Bob Attr\"\n" +
                    "    ]\n," +
                    "    [\n" +
                    "      \"Doc\",\n" +
                    "      \"Bob Home\"\n" +
                    "    ],\n" +
                    "    [\n" +
                    "      \"Bob Attr\",\n" +
                    "      \"Super PC\"\n" +
                    "    ]\n" +
                    "  ],\n" +
                    "  \"associations\": []\n" +
                    "}");

            inputJson.setHeight("80vh");
            Button importButton = new Button("Import JSON", click -> {
                try {
                    g.fromJson(inputJson.getValue());
                    Set<SingletonGraph.PolicyClassWithActive> activesPc = SingletonGraph.getActivePCs();
                    Set<String> nodeNames = activesPc.stream().map(e -> e.getName()).collect(Collectors.toSet());
                    Set<SingletonGraph.PolicyClassWithActive> activesPcCopy = new HashSet<>();
                    for (Node node : g.getNodes()) {
                        if (node.getType() == NodeType.PC) {
                            //compare the pc in the graph to the activePCs
                            if (!nodeNames.contains(node.getName())) {
                                SingletonGraph.PolicyClassWithActive newPc = new SingletonGraph.PolicyClassWithActive(node);
                                activesPcCopy.add(newPc);
                            }
                        }
                    }
                    activesPc.addAll(activesPcCopy);
                    MainView.notify("The Json has been imported", MainView.NotificationType.SUCCESS);
                    //UI.getCurrent().getPage().reload();
                } catch (PMException e) {
                    e.printStackTrace();
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                }
                //updateGraph(inputJson.getValue());
            });
            importButton.setHeight("5%");
            add(inputJson);
            add(importButton);
        }
    }

    private class ExportLayout extends VerticalLayout {
        public ExportLayout () {
            getStyle().set("background", "lightcoral");
            setAlignItems(Alignment.STRETCH);
            H2 exportTitle = new H2("Export:");
            exportTitle.getStyle().set("margin-bottom", "0");
            add(exportTitle);

            TextArea exportJson = new TextArea();
//            exportJson.setEnabled(false);
            exportJson.setHeight("90vh");
            exportJson.setMaxHeight("90vh");
            //exportJson.getStyle().set("resize", "none");


            Button exportButton = new Button("Export JSON", click -> {
                try {
                    //exportJson.setValue(SingletonGraph.getInstance().getGraphService(userCtx).toJson());
                    exportJson.setValue(g.toJson());
                    //exportJson.setValue(SingletonGraph.getInstance().getPAP().getGraphPAP().toJson());
                    MainView.notify("The graph has been exported into a JSON", MainView.NotificationType.SUCCESS);
                } catch (PMException e) {
                    e.printStackTrace();
                    MainView.notify("error : " + e.getMessage(), MainView.NotificationType.ERROR);
                }
            });
            exportButton.setHeight("5%");
            add(exportJson);
            add(exportButton);
        }
    }

    public static String toFullJson (SingletonGraph graph) throws PMException{
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Collection<Node> nodes = graph.getNodes();
        HashSet<String[]> jsonAssignments = new HashSet<>();
        HashSet<JsonAssociation> jsonAssociations = new HashSet<>();
        for (Node node : nodes) {
            try {
                Set<String> parents = graph.getParents(node.getName());

                for (String parent : parents) {
                    jsonAssignments.add(new String[]{node.getName(), parent});
                }
            } catch (PMException e) {
                System.err.println("To Full Json Error: " + e.getMessage());
            }

            try {
                Map<String, OperationSet> associations = graph.getSourceAssociations(node.getName());
                for (String target : associations.keySet()) {
                    OperationSet ops = associations.get(target);
                    Node targetNode = graph.getNode(target);

                    jsonAssociations.add(new JsonAssociation(node.getName(), targetNode.getName(), ops));
                }
            } catch (PMException e) {
                System.err.println("To Full Json Error: " + e.getMessage());
            }
        }

        return gson.toJson(new JsonGraph(nodes, jsonAssignments, jsonAssociations));
    }

    private static class JsonGraph {
        Collection<Node> nodes;
        Set<String[]>  assignments;
        Set<JsonAssociation> associations;

        JsonGraph(Collection<Node> nodes, Set<String[]> assignments, Set<JsonAssociation> associations) {
            this.nodes = nodes;
            this.assignments = assignments;
            this.associations = associations;
        }

        Collection<Node> getNodes() {
            return nodes;
        }

        Set<String[]> getAssignments() {
            return assignments;
        }

        Set<JsonAssociation> getAssociations() {
            return associations;
        }
    }

    private static class JsonAssociation {
        String source;
        String target;
        Set<String> operations;

        public JsonAssociation(String source, String target, Set<String> operations) {
            this.source = source;
            this.target = target;
            this.operations = operations;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public Set<String> getOperations() {
            return operations;
        }
    }
}

//{
//   "nodes":[
//      {
//         "id":-1,
//         "name":"Super PC",
//         "type":"PC"
//      },
//      {
//         "id":-2,
//         "name":"PM",
//         "type":"OA",
//         "properties": [
//            {
//                "key": "namespace",
//                "value": "connector"
//            }
//         ]
//      },
//      {
//         "id":-3,
//         "name":"Super",
//         "type":"UA"
//      },
//      {
//         "id":-4,
//         "name":"super",
//         "type":"U",
//         "properties": [
//            {
//                "key": "password",
//                "value": "super"
//            }
//         ]
//      }
//   ],
//   "assignments":[
//      {
//         "child":-2,
//         "parent":-1
//      },
//      {
//         "child":-3,
//         "parent":-1
//      },
//      {
//         "child":-4,
//         "parent":-3
//      }
//   ],
//   "associations":[
//      {
//         "ua":-3,
//         "target":-2,
//         "ops":[
//            "*"
//         ],
//         "isInherit":true
//      }
//   ]
//}
