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
import gov.nist.csd.pm.pip.graph.GraphSerializer;


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
        layout.setJustifyContentMode(JustifyContentMode.CENTER);
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


        Button execute = new Button("Execute>", evt -> {
            executeRule(editorLayout.getValue());

        });
        execute.setHeight("99vh");

        Button convert = new Button("Export to JSON>", evt -> {
        });
        convert.setHeight("99vh");
        convert.addClickListener((click) -> {
            try {
            String json = GraphSerializer.toJson(g.getPAP().getGraphPAP());
                JSONLayout.setValue(json);
            } catch (PMException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        });

        layout.add(execute);
        layout.add(convert);

        JSONLayout = new JSONExport();
        JSONLayout.setWidth("44%");
        JSONLayout.getStyle().set("height","100vh");
        layout.add(JSONLayout);
    }

    public void executeRule (String sptRule) {
        try {
            SptRuleParser ruleParser = new SptRuleParser(sptRule);
            ruleParser.parse();
            notify("Graph updated");
        } catch (Exception e) {
            e.printStackTrace();
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
                    "rule2 \n" +
                    "allow teller->staff: rbac; ask ua value: branch\n" +
                            "        to \"create object\", \"delete object\" \n" +
                            "        in branchAccounts->accounts: rbac; ask oa value: branch\n" +
                            "when ua_value = oa_value\n");
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
