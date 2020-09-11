package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;

@Tag("settings")
public class Settings extends VerticalLayout {

    public SingletonGraph g;
    private SettingsViewer settingsViewer;
    public static boolean hidePolicy;
    public boolean mysqlBool;

    public boolean getMysqlBool() {
        return mysqlBool;
    }
    public void setMysqlBool(boolean mysqlBool) {
        this.mysqlBool = mysqlBool;
    }

    public boolean isHidePolicy() {
        return hidePolicy;
    }
    public void setHidePolicy(boolean hidePolicy) {
        this.hidePolicy = hidePolicy;
    }

    public Settings() {
        setFlexGrow(1.0);
        setSizeFull();
        setMargin(false);
        setPadding(false);
        g = SingletonGraph.getInstance();
        setUpLayout();
    }

    private void setUpLayout() {
        settingsViewer = new SettingsViewer();
        settingsViewer.setWidthFull();
        settingsViewer.getStyle().set("height","35vh");
        add(settingsViewer);
    }

    private class SettingsViewer extends VerticalLayout {
        public SettingsViewer() {
            getStyle().set("background", "lightblue");
            setAlignItems(Alignment.STRETCH);
            add(new H2("Settings:"));

            HorizontalLayout layout = new HorizontalLayout();
            add(layout);

            RadioButtonGroup<String> databaseRadio = new RadioButtonGroup<>();
            databaseRadio.setLabel("What kind of database do you want to use ?");
            databaseRadio.setItems("In-Memory", "MySQL");
            databaseRadio.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
            if (g.getMysql()) {
                databaseRadio.setValue("MySQL");
            } else {
                databaseRadio.setValue("In-Memory");
            }
            databaseRadio.addValueChangeListener(event -> {
                    Dialog dialog = new Dialog();
                    HorizontalLayout form = new HorizontalLayout();
                    form.setAlignItems(FlexComponent.Alignment.BASELINE);
                    dialog.add(new Label("WARNING: "));
                    dialog.add(new Paragraph("\n"));
                    dialog.add(new Paragraph("Switching database will reset your in-memory data. Are you sure ?"));
                    Button button = new Button("Yes", eventSwitch -> {
                        mysqlBool = event.getValue().equalsIgnoreCase("MySQL");
                        databaseRadio.setValue(event.getValue());
                        setMysqlBool(mysqlBool);
                        SingletonGraph g = SingletonGraph.getInstance();
                        g.updateGraph(mysqlBool);
                        dialog.close();
                        UI.getCurrent().getPage().reload();
                    });
                    button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                    form.add(button);

                Button cancel = new Button("Cancel", eventCancel -> {
                    dialog.close();
                });
                cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
                form.add(cancel);
                dialog.add(form);
                dialog.open();
            });

            layout.add(databaseRadio);

            RadioButtonGroup<String> toggleHidePolicy = new RadioButtonGroup<>();
            toggleHidePolicy.setLabel("Do you want to hide the super policy configuration ?");
            toggleHidePolicy.setItems("Hide", "Show");
            toggleHidePolicy.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

            if (hidePolicy) {
                toggleHidePolicy.setValue("Hide");
            } else {
                toggleHidePolicy.setValue("Show");
            }

            toggleHidePolicy.addValueChangeListener(event -> {
                    setHidePolicy(event.getValue().equalsIgnoreCase("Hide"));
                    UI.getCurrent().getPage().reload();
                    });
            layout.add(toggleHidePolicy);

//            CheckboxGroup<SingletonGraph.PolicyClassWithActive> checkboxGroup = new CheckboxGroup<>();
//            checkboxGroup.setLabel("Which Policy Classes do you want to be active?");
//            Set<SingletonGraph.PolicyClassWithActive> policies = g.getActivePCs();
//            checkboxGroup.setItems(policies);
//            policies.removeIf((pc) -> !pc.isActive());
//            checkboxGroup.setValue(policies);
//
//            checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
//
//            Div value = new Div();
//            value.setText("Select a value");
//            checkboxGroup.addValueChangeListener(event -> {
//                if (event.getValue() == null) {
//                    value.setText("No option selected");
//                } else {
//                    value.setText("Selected: " + event.getValue());
//                }
//            });
//
//            add (checkboxGroup);
        }
    }

}
