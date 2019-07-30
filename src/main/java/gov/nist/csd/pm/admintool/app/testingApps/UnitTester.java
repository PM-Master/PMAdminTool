package gov.nist.csd.pm.admintool.app.testingApps;

import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.admintool.tests.AssertAssignment;
import gov.nist.csd.pm.admintool.tests.AssertAssociation;
import gov.nist.csd.pm.admintool.tests.SingletonActiveTests;
import gov.nist.csd.pm.admintool.tests.Test;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;

import java.util.*;

public class UnitTester extends VerticalLayout {
    private SingletonGraph g;
    private SingletonActiveTests tests;
    private ComboBox<String> testSelect;
    private Test tempTest;
    private Accordion results;
    private HorizontalLayout params;

    public UnitTester () {
        setPadding(false);
        setMargin(false);
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        params = new HorizontalLayout();

        g = SingletonGraph.getInstance();
        tests = SingletonActiveTests.getInstance();
        results = new Accordion();
        refreshListOfTests();

        testSelect = new ComboBox<>("Tests");
        testSelect.setItems("Assert Association", "Assert Assignment");
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
                tests.add(tempTest);
                refreshComponent();
            } else {
                notify("test is null");
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
                }
                if (tempTest != null) {
                    Map<String, Test.Type> info = tempTest.getParamNameAndType();
                    for (String key : info.keySet()) {
                        switch (info.get(key)) {
                            case NODETYPE:
                                Select<Node> uaIDSelect = new Select<>();
                                try {
                                    uaIDSelect.setItems(g.getNodes());
                                } catch (PMException e) {
                                    e.printStackTrace();
                                    notify(e.getMessage());
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
                                ComboBox<String> opSelect = new ComboBox<>("","read", "write");
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
                .set("background", "#a0ffa0")
                .set("overflow-y", "scroll");
        add(results);

        refreshListOfTests();
    }

    private void refreshListOfTests() {
        results.getChildren().forEach(c -> {
            results.remove(c);
        });
        for (Test test: tests) {
            if (test.runTest()) {
                AccordionPanel disabledPannel = results.add(test.toString(), new Span("Never See This"));
                disabledPannel.getElement().getStyle()
                        .set("background", "lightblue");
                disabledPannel.addThemeVariants(DetailsVariant.FILLED);
                disabledPannel.setEnabled(false);
            } else {
                AccordionPanel regularPannel = results.add(test.toString(), new Span("Audit Goes Here"));
                regularPannel.getElement().getStyle()
                        .set("background", "lightcoral");
                regularPannel.addThemeVariants(DetailsVariant.FILLED);
            }
            results.close();
        }
    }

    public void refreshComponent() {
        tempTest = null;
        testSelect.setValue(null);
        params.removeAll();
        refreshListOfTests();
    }

    public void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
    }
}