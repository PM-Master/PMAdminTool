package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.GraphSerializer;

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
        importLayout.getStyle().set("height","100vh");
        layout.add(exportLayout);
    }

    public void updateGraph (String json) {
        try {
            g = SingletonGraph.updateGraph((SingletonGraph)GraphSerializer.fromJson(g, json));
            add(new Paragraph(g.getNode(-1).getName()));
        } catch (PMException e) {
            e.printStackTrace();
        }
    }

    private class ImportLayout extends VerticalLayout {
        public ImportLayout () {
            setAlignItems(Alignment.STRETCH);
            TextArea inputJson = new TextArea();
            inputJson.setValue("{\n" +
                    "    \"nodes\":[\n" +
                    "        {\n" +
                    "            \"id\":-1,\n" +
                    "            \"name\":\"Super PC\",\n" +
                    "            \"type\":\"PC\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"assignments\":[\n" +
                    "    ],\n" +
                    "    \"associations\":[\n" +
                    "    ]\n" +
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
            setAlignItems(Alignment.STRETCH);
            TextArea inputJson = new TextArea();
            inputJson.setEnabled(false);
            inputJson.setHeight("90vh");

            Button importButton = new Button("Export JSON", click -> {
                try {
                    inputJson.setValue(GraphSerializer.toJson(g));
                } catch (PMException e) {
                    e.printStackTrace();
                    ImportExport.this.notify(e.getMessage());
                }
            });
            importButton.setHeight("10%");
            add(inputJson);
            add(importButton);
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
