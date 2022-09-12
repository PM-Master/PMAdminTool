package gov.nist.csd.pm.admintool.app.testingApps;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import gov.nist.csd.pm.admintool.actions.Action;
import gov.nist.csd.pm.admintool.actions.SingletonActiveActions;
import gov.nist.csd.pm.admintool.actions.events.AssignEvent;
import gov.nist.csd.pm.admintool.actions.events.DeassignEvent;
import gov.nist.csd.pm.admintool.actions.events.DeassignFromEvent;
import gov.nist.csd.pm.admintool.actions.events.Event;
import gov.nist.csd.pm.admintool.actions.tests.AssertAssignment;
import gov.nist.csd.pm.admintool.actions.tests.AssertAssociation;
import gov.nist.csd.pm.admintool.actions.tests.CheckPermission;
import gov.nist.csd.pm.admintool.actions.tests.Test;
import gov.nist.csd.pm.admintool.app.MainView;
import gov.nist.csd.pm.admintool.graph.SingletonClient;
import gov.nist.csd.pm.policy.exceptions.PMException;
import gov.nist.csd.pm.policy.model.graph.nodes.Node;

import java.util.Map;

public class UnitTester extends VerticalLayout {
    private SingletonClient g;
    private SingletonActiveActions actions;
    private ComboBox<String> testSelect;
    private Action tempTest;
    private Accordion results;
    private HorizontalLayout params;

    public UnitTester () {
        setPadding(false);
        setMargin(false);
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        params = new HorizontalLayout();

        g = SingletonClient.getInstance();
        actions = SingletonActiveActions.getInstance();
        results = new Accordion();
        refreshListOfTests();

        testSelect = new ComboBox<>("Tests");
        testSelect.setItems("Assert Association", "Assert Assignment", "Check Permission",
                "Assign Event", "Deassign Event", "Deassign From Event");
        tempTest = null;

        addTestSelectForm();
        addListOfTests();
    }

    private void addTestSelectForm() {
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);
        form.setWidthFull();
        form.setMargin(false);

        params.setAlignItems(FlexComponent.Alignment.BASELINE);
        params.setMargin(false);

        Button test = new Button("+", event -> {
            if (tempTest != null) {
                actions.add(tempTest);
                refreshComponent();
            } else {
                MainView.notify("No Test", MainView.NotificationType.DEFAULT);
            }
        });
        form.add(test);

        // actual combo box
        testSelect.setRequiredIndicatorVisible(true);
        testSelect.setPlaceholder("Select an option");
        testSelect.addValueChangeListener(event -> {
            if (!event.getSource().isEmpty()) {
                switch (event.getValue()) {
                    case "Assert Association":
                        params.removeAll();
                        tempTest = new AssertAssociation();
                        break;
                    case "Assert Assignment":
                        params.removeAll();
                        tempTest = new AssertAssignment();
                        break;
                    case "Check Permission":
                        params.removeAll();
                        tempTest = new CheckPermission();
                        break;
                    case "Assign Event":
                        params.removeAll();
                        tempTest = new AssignEvent();
                        break;
                    case "Deassign Event":
                        params.removeAll();
                        tempTest = new DeassignEvent();
                        break;
                    case "Deassign From Event":
                        params.removeAll();
                        tempTest = new DeassignFromEvent();
                        break;
                }
                if (tempTest != null) {
                    Map<String, Test.Type> info = tempTest.getParamNameAndType();
                    for (String key : info.keySet()) {
                        switch (info.get(key)) {
                            case NODETYPE:
                                // todo: get only a certain type of node
                                ComboBox<Node> uaIDSelect = new ComboBox<>("");
                                try {
                                    uaIDSelect.setItems(g.getNodes());
                                } catch (PMException e) {
                                    e.printStackTrace();
                                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                                }
                                uaIDSelect.setLabel(key);
                                uaIDSelect.setPlaceholder("Select Node");
                                uaIDSelect.addValueChangeListener(selectEvent -> {
                                    Node selected = selectEvent.getValue();
                                    if (selected != null) {
                                        tempTest.setParamValue(key, selected);
                                    }
                                });
                                params.add(uaIDSelect);
                                break;
                            case OPERATION:
                                // todo: get a list of all of the operations
                                ComboBox<String> opSelect = new ComboBox<>("","read", "write", "create object", "delete object");
                                opSelect.setLabel(key);
                                opSelect.setPlaceholder("Select Operation");
                                opSelect.addValueChangeListener(selectEvent -> {
                                    String selected = selectEvent.getValue();
                                    if (selected != null) {
                                        tempTest.setParamValue(key, selected);
                                    }
                                });
                                params.add(opSelect);
                                break;
                            case STRING:
                                TextField textField = new TextField();
                                textField.setLabel(key);
                                textField.setPlaceholder("Select String");
                                textField.addValueChangeListener(textEvent -> {
                                    String selected = textEvent.getValue();
                                    if (selected != null) {
                                        tempTest.setParamValue(key, selected);
                                    }
                                });
                                params.add(textField);
                                break;
                        }
                    }
                }
            }
        });

        form.add(testSelect);
        form.add(params);

        add(form);
    }

    private void addListOfTests() {
        results.setWidthFull();
        results.getElement().getStyle()
                .set("overflow-y", "scroll");
        add(results);

        refreshListOfTests();
    }

    private void refreshListOfTests() {
        results.getChildren().forEach(c -> {
            results.remove(c);
        });
        for (Action action: actions) {
            String audit = action.explain();
            VerticalLayout auditLayout = new VerticalLayout();
            auditLayout.setSizeFull();
            auditLayout.getStyle()
                    .set("padding-bottom", "0px");
            String[] split = audit.split("\n");
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
                auditLayout.add(new Span(audit));
            }
            AccordionPanel regularPannel = results.add(action.toString(), auditLayout);
            if (action instanceof Test) {
                if (action.run()) { // passed
                    regularPannel.getElement().getStyle().set("background", "#BEFFB5"); // passed
                } else { // failed
                    regularPannel.getElement().getStyle().set("background", "#FFBFB5"); // failed
                }
            } else if (action instanceof Event){
                if (action.run()) { // successful execution
                    regularPannel.getElement().getStyle().set("background", "#B5BFFF"); // successful execution
                } else { // unsuccessful execution
                    regularPannel.getElement().getStyle().set("background", "#ECECEC"); // unsuccessful execution
                }
            }
            regularPannel.addThemeVariants(DetailsVariant.FILLED, DetailsVariant.REVERSE);
            results.close();
        }
    }

    public void refreshComponent() {
        tempTest = null;
        testSelect.setValue(null);
        params.removeAll();
        refreshListOfTests();
    }
}
