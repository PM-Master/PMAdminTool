package gov.nist.csd.pm.admintool.app.testingApps;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.admintool.tests.AssertAssociation;
import gov.nist.csd.pm.admintool.tests.Test;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;

import java.util.*;

public class UnitTester extends VerticalLayout {
    private SingletonGraph g;
    private ComboBox<String> testSelect;
    private Test tempTest;

    public UnitTester () {
        setPadding(false);
        setMargin(false);
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        g = SingletonGraph.getInstance();
        testSelect = new ComboBox<>("Tests");
        testSelect.setItems("Assert Association", "Assert Assignment");
        tempTest = null;

        addTestSelectForm();
    }

    private void addTestSelectForm() {
        HorizontalLayout form = new HorizontalLayout();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);
        form.setWidthFull();
        form.setMargin(false);

        Button test = new Button("Run Test", event -> {
            if (tempTest != null) {
                if (tempTest.runTest()) {
                    notify("Test Passed!");
                } else{
                    notify("Test Failed!");
                }
            } else {
                notify("test is null");
            }
        });
        form.add(test);

        // actual combo box
        testSelect.setRequiredIndicatorVisible(true);
        testSelect.setPlaceholder("Select an option");
        testSelect.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
                notify("no ting selected");
            } else {
                if (!event.getSource().isEmpty()) {
                    switch (event.getValue()) {
                        case "Assert Association":
                            tempTest = new AssertAssociation();
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
                                    form.add(uaIDSelect);
                                    break;
                                case OPERATION:
                                    Select<String> opSelect = new Select<>("read", "write");
                                    opSelect.setLabel(key);
                                    opSelect.setPlaceholder("Select Operation");
                                    opSelect.addValueChangeListener(selectEvent -> {
                                        String selected = selectEvent.getValue();
                                        if (selected != null) {
                                            tempTest.setParamValue(key, selected);
                                        }
                                    });
                                    form.add(opSelect);
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
                                    form.add(textField);
                                    break;
                            }
                        }
                    }
                } else {
                    notify("No Test Selected!");
                }
            }
        });
        form.add(testSelect);

        add(form);
    }

    public void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
    }
}