package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Tag("import-export")
public class ImportExport extends VerticalLayout {
    private SingletonGraph g;
    private HorizontalLayout layout;
    private ImportLayout importLayout;
    private ExportLayout exportLayout;

    public ImportExport() {
        g = SingletonGraph.getInstance();
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
