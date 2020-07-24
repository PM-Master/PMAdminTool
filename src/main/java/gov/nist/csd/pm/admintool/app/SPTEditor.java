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
import gov.nist.csd.pm.admintool.spt.parser.SptRuleParser;


@Tag("SPTEditor")
    public class SPTEditor extends VerticalLayout {
        public SingletonGraph g;
        private VerticalLayout layout;
        private SPTInput editorLayout;
        private RuleAnalysis ruleAnalysis;

        public SPTEditor() {
            g = SingletonGraph.getInstance();
            layout = new VerticalLayout();
            layout.setFlexGrow(1.0);
            layout.setJustifyContentMode(JustifyContentMode.CENTER);
            add(layout);
            setUpLayout();
        }

    private void setUpLayout() {
        setSizeFull();
        setPadding(true);

        editorLayout = new SPTInput();
        editorLayout.setWidth("99%");
        editorLayout.setHeight("90%");
        layout.add(editorLayout);

        Button execute = new Button("Execute", evt -> {
            executeRule(editorLayout.getValue());
        });
        execute.setHeight("20%");
        execute.setWidth("99%");
        layout.add(execute);

//        Button convert = new Button("Export to JSON>", evt -> {
//        });
//        convert.setHeight("99vh");
//        convert.addClickListener((click) -> {
//            try {
//            String json = GraphSerializer.toJson(g.getPAP().getGraphPAP());
//                JSONLayout.setValue(json);
//            } catch (PMException e) {
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//            }
//        });
//
//        layout.add(convert);

//        ruleAnalysis = new RuleAnalysis();
//        ruleAnalysis.setWidth("44%");
//        ruleAnalysis.getStyle().set("height","100vh");
//        layout.add(ruleAnalysis);
    }

    public void executeRule (String sptRule) {
        try {
            SptRuleParser ruleParser = new SptRuleParser(sptRule);
            ruleParser.parse();
            MainView.notify("Graph updated", MainView.NotificationType.SUCCESS);
            analyseScript();
        } catch (Exception e) {
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    public String analyseScript() {
        // This method should go through current rule and then all previous rules in the same script
        // 1. Find purpose in each to list UAs->OPS->OAs
        // 2. Test - create a dummy user under UAs
        // 3. Check if user really has given permission on each OA in the list.
        // 4. For unfulfilled purpose, recommend assignments/associations with guided text/dropdowns/buttons

        return "Test";
    }

    private class SPTInput extends VerticalLayout {
        private TextArea inputSpt;

        public SPTInput () {
            getStyle().set("background", "lightblue");
            setAlignItems(Alignment.STRETCH);

            add(new H2("SPT Input:"));

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
            inputSpt.setHeight("75vh");
            add(inputSpt);
        }

        public String getValue() {
            return inputSpt.getValue();
        }
    }

    private class RuleAnalysis extends VerticalLayout {
        private TextArea outputJson;

        public RuleAnalysis () {
            getStyle().set("background", "lightcoral");
            setAlignItems(Alignment.STRETCH);

            add(new H2("JSON Output:"));

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
}
