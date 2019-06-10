package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.admintool.spt.parser.SptRuleParser;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.GraphSerializer;

@Tag("SPTEditor")
public class SPTEditor extends VerticalLayout {
    private SingletonGraph g;
    private HorizontalLayout layout;
    private ImportLayout editorLayout;
    private ExportLayout JSONLayout;

    public SPTEditor() {
        g = SingletonGraph.getInstance();
        layout = new HorizontalLayout();
        layout.setFlexGrow(1.0);
        add(layout);
        setUpLayout();
    }

    private void setUpLayout() {
        setSizeFull();
        setPadding(true);

        editorLayout = new ImportLayout();
        editorLayout.setWidth("50%");
        editorLayout.getStyle().set("height","100vh");
        layout.add(editorLayout);

        JSONLayout = new ExportLayout();
        JSONLayout.setWidth("50%");
        JSONLayout.getStyle().set("height","100vh");
        layout.add(JSONLayout);
    }

    public void exportToJSON (String sptRule) {
        try {
            SptRuleParser ruleParser = new SptRuleParser(sptRule);
            String json = ruleParser.semopGeneratePM();
            add(new Paragraph(json));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class ImportLayout extends VerticalLayout {
        public ImportLayout () {
            setAlignItems(Alignment.STRETCH);
            TextArea inputJson = new TextArea();
            inputJson.setValue("script s1\n" +
                    "rule1 \n" +
                    "\twhen user is Doctor in Staff in policy Role\n" +
                    "\tallow user \"File read\", \"File write\" \n" +
                    "\ton object attribute Patients in Ward in Hospital\n");
            inputJson.setHeight("90vh");

            Button importButton = new Button("Export to JSON", click -> {
                exportToJSON(inputJson.getValue());
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
                    SPTEditor.this.notify(e.getMessage());
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
