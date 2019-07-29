package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.admintool.spt.parser.SptRuleParser;
import gov.nist.csd.pm.exceptions.PMException;


@Tag("SPTEditor")
public class SPTEditor extends VerticalLayout {
    public SingletonGraph g;
    private HorizontalLayout layout;
    private SPTInput editorLayout;
    private JSONExport JSONLayout;

    public SPTEditor() {
        g = SingletonGraph.getInstance();
        layout = new HorizontalLayout();
        layout.setFlexGrow(1.0);
//        layout.setJustifyContentMode(JustifyContentMode.CENTER);
        add(layout);
        setUpLayout();
    }

    private void setUpLayout() {
        setSizeFull();
        setPadding(true);

        editorLayout = new SPTInput();
        editorLayout.setWidth("44%");
        editorLayout.getStyle().set("height","100vh");
        layout.add(editorLayout);


        Button convert = new Button("-JSON>");
        convert.setHeight("99vh");

        convert.addClickListener((click) -> {
            try {
                g.reset();
                JSONLayout.setValue(exportToJSON(editorLayout.getValue()));
                notify("converted to json");
            } catch (PMException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        });

        layout.add(convert);



        JSONLayout = new JSONExport();
        JSONLayout.setWidth("44%");
        JSONLayout.getStyle().set("height","100vh");
        layout.add(JSONLayout);
    }

    public String exportToJSON (String sptRule) {
        try {
            SptRuleParser ruleParser = new SptRuleParser(sptRule);
            String sResult = ruleParser.parse();
            String json = "";
//            String json = GraphSerializer.toJson(g);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private class SPTInput extends VerticalLayout {
        private TextArea inputSpt;

        public SPTInput () {
            getStyle().set("background", "lightblue");
            setAlignItems(Alignment.STRETCH);
            inputSpt = new TextArea();
            inputSpt.getStyle()
                    .set("border-radius", "3px")
                    .set("padding-top", "3px")
                    .set("padding-bottom", "3px");
            inputSpt.setValue("script s1\n" +
                    "rule1 \n" +
                    "\twhen user is Doctor in Staff in policy Role\n" +
                    "\tallow user \"File read\", \"File write\" \n" +
                    "\ton object attribute Patients in Ward in Hospital\n");
            inputSpt.setHeight("100vh");
            add(inputSpt);
        }

        public String getValue() {
            return inputSpt.getValue();
        }
    }

    private class JSONExport extends VerticalLayout {
        private TextArea outputJson;

        public JSONExport () {
            getStyle().set("background", "lightcoral");
            setAlignItems(Alignment.STRETCH);
            outputJson = new TextArea();
            outputJson.getStyle()
                    .set("border-radius", "5px")
                    .set("padding-top", "3px")
                    .set("padding-bottom", "3px");
//            outputJson.setEnabled(false);
            outputJson.setHeight("100vh");
            add(outputJson);
        }

        public void setValue(String value) {
            outputJson.setValue(value);
        }
    }

    public void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
    }
}
