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
    public Boolean mysqlBool;

    public Boolean getMysqlBool() {
        return mysqlBool;
    }

    public void setMysqlBool(Boolean mysqlBool) {
        this.mysqlBool = mysqlBool;
    }

    public Settings() {
        setFlexGrow(1.0);
        setSizeFull();
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

            RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
            radioGroup.setLabel("What kind of database do you want to use ?");
            radioGroup.setItems("In-Memory", "MySQL");
            radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
            if (g.getMysql()) {
                radioGroup.setValue("MySQL");
            } else {
                radioGroup.setValue("In-Memory");
            }
            radioGroup.addValueChangeListener(event -> {
                    Dialog dialog = new Dialog();
                    HorizontalLayout form = new HorizontalLayout();
                    form.setAlignItems(FlexComponent.Alignment.BASELINE);
                    dialog.add(new Label("WARNING: "));
                    dialog.add(new Paragraph("\n"));
                    dialog.add(new Paragraph("Switching database will reset your in-memory data. Are you sure ?"));
                    Button button = new Button("Yes", eventSwitch -> {
                        mysqlBool = event.getValue().equalsIgnoreCase("MySQL");
                        radioGroup.setValue(event.getValue());
                        setMysqlBool(mysqlBool);
                        SingletonGraph g = SingletonGraph.getInstance();
                        g.updateGraph(mysqlBool);
                        dialog.close();
                        UI.getCurrent().getPage().reload();
                    });
                    button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                    form.add(button);

                Button cancel = new Button("Cancel", eventCancel -> {
                    //radioGroup.setValue(event.getOldValue());
                    dialog.close();

                    //radioGroup.clear();
                });
                cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
                form.add(cancel);
                dialog.add(form);
                dialog.open();
            });
            add(radioGroup);
        }
    }

}
