package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.graph.GraphSerializer;

@Tag("import-export")
public class ImportExport extends VerticalLayout {
    private SingletonGraph g;
    private HorizontalLayout layout;
    private ImportLayout importLayout;
    private ExportLayout exportLayout;
//    private UserContext userCtx;

    public ImportExport() {
        g = SingletonGraph.getInstance();
//        userCtx = new UserContext(, -1);
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
        try {
            g = g.updateGraph(GraphSerializer.fromJson(g.getPAP().getGraphPAP(), json));
        } catch (PMException e) {
            e.printStackTrace();
        }
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
                    "      \"id\": -1,\n" +
                    "      \"name\": \"Super PC\",\n" +
                    "      \"type\": \"PC\",\n" +
                    "      \"properties\": {}\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 1,\n" +
                    "      \"name\": \"Bob Home\",\n" +
                    "      \"type\": \"OA\",\n" +
                    "      \"properties\": {}\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 2,\n" +
                    "      \"name\": \"Bob Attr\",\n" +
                    "      \"type\": \"UA\",\n" +
                    "      \"properties\": {}\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 3,\n" +
                    "      \"name\": \"Bob\",\n" +
                    "      \"type\": \"U\",\n" +
                    "      \"properties\": {}\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 4,\n" +
                    "      \"name\": \"Doc\",\n" +
                    "      \"type\": \"O\",\n" +
                    "      \"properties\": {}\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"assignments\": [\n" +
                    "    {\n" +
                    "      \"sourceID\": 1,\n" +
                    "      \"targetID\": -1\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"sourceID\": 3,\n" +
                    "      \"targetID\": 2\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"sourceID\": 4,\n" +
                    "      \"targetID\": 1\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"sourceID\": 2,\n" +
                    "      \"targetID\": -1\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"associations\": []\n" +
                    "}");
            inputJson.setHeight("90vh");

            Button importButton = new Button("Import JSON", click -> {
                updateGraph(inputJson.getValue());
            });
            importButton.setHeight("10%");
            add(inputJson);
            add(importButton);
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
                try {
                    exportJson.setValue(GraphSerializer.toJson(g.getPAP().getGraphPAP()));
                } catch (PMException e) {
                    e.printStackTrace();
                    ImportExport.this.notify(e.getMessage());
                }
            });
            exportButton.setHeight("10%");
            add(exportJson);
            add(exportButton);
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
