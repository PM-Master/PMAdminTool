package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.graph.MemGraph;
import gov.nist.csd.pm.pip.graph.mysql.MySQLGraph;

@Tag("import-export")
public class ImportExport extends VerticalLayout {
    private SingletonGraph g;
    private HorizontalLayout layout;
    private ImportLayout importLayout;
    private ExportLayout exportLayout;
//    private UserContext userCtx;

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

            add(new H2("Import:"));

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


            inputJson.setHeight("90vh");

            Button importButton = new Button("Import JSON", click -> {
                //todo toggle which graph to import export with settings value
                System.out.println("g : " + g.getMysql());
                if (g.getMysql()) {
                    //MySQLGraph graph = (MySQLGraph) SingletonGraph.getInstance().getPAP().getGraphPAP();
                    try {
                        SingletonGraph.getInstance().getPAP().getGraphPAP().fromJson(inputJson.getValue());
                        notify("The Json has been imported");
                        UI.getCurrent().getPage().reload();
                    } catch (PMException e) {
                        e.printStackTrace();
                        notify(e.getMessage());
                    }
                } else {
                    try {
                        SingletonGraph.getInstance().getPAP().getGraphPAP().fromJson(inputJson.getValue());
                        notify("The Json has been imported");
                        UI.getCurrent().getPage().reload();
                    } catch (PMException e) {
                        notify("error : " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                //updateGraph(inputJson.getValue());
            });
            importButton.setHeight("10%");
            add(inputJson);
            add(importButton);
        }

        public void notify(String message){
            Notification notif = new Notification(message, 3000);
            notif.open();
        }
    }

    private class ExportLayout extends VerticalLayout {
        public ExportLayout () {
            getStyle().set("background", "lightcoral");
            setAlignItems(Alignment.STRETCH);

            add(new H2("Export:"));

            TextArea exportJson = new TextArea();
//            exportJson.setEnabled(false);
            exportJson.setHeight("90vh");

            Button exportButton = new Button("Export JSON", click -> {
                System.out.println("g: " + g.getMysql());
                if (g.getMysql()) {
                    try {
                        MySQLGraph graph = (MySQLGraph) SingletonGraph.getInstance().getPAP().getGraphPAP();
                        exportJson.setValue(graph.toJson());
                        notify("The graph has been exported into a JSON");
                    } catch (PMException e) {
                        e.printStackTrace();
                        notify("error : " + e.getMessage());
                    }
                } else {
                    try {
                        MemGraph graph = (MemGraph) SingletonGraph.getInstance().getPAP().getGraphPAP();
                        exportJson.setValue(graph.toJson());
                        notify("The graph has been exported into a JSON");
                    } catch (PMException e) {
                        e.printStackTrace();
                        notify("error : " + e.getMessage());
                    }
                }
            });
            exportButton.setHeight("10%");
            add(exportJson);
            add(exportButton);
        }

        public void notify(String message){
            Notification notif = new Notification(message, 3000);
            notif.open();
        }
    }

    public void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
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
