package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pdp.services.GraphService;
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.obligations.evr.EVRException;
import gov.nist.csd.pm.pip.obligations.evr.EVRParser;
import gov.nist.csd.pm.pip.obligations.model.Obligation;

import java.io.ByteArrayInputStream;
import java.util.*;

@Tag("obligation-editor")
public class ObligationEditor extends VerticalLayout {
    private SingletonGraph g;
    private HorizontalLayout layout;
    private YamlEditor yamlEditor;
    private ObligationViewer obligationViewer;

    public ObligationEditor() {
        g = SingletonGraph.getInstance();
        layout = new HorizontalLayout();
        layout.setFlexGrow(1.0);
        add(layout);
        setUpLayout();
    }

    private void setUpLayout() {
        setSizeFull();
        setPadding(true);

        obligationViewer = new ObligationViewer();
        obligationViewer.setWidth("65%");
        obligationViewer.getStyle().set("height","100vh");
        layout.add(obligationViewer);

        yamlEditor = new YamlEditor();
        yamlEditor.setWidth("35%");
        yamlEditor.getStyle().set("height","100vh");
        layout.add(yamlEditor);
    }

    private class YamlEditor extends VerticalLayout {
        public YamlEditor () {
            getStyle().set("background", "lightcoral");
            setAlignItems(Alignment.STRETCH);

//            TextArea inputJson = new TextArea();
//            inputJson.setHeight("90%");
//
//            Button importButton = new Button("Export YAML", click -> {
//                try {
//                    g.addObl(EVRParser.parse(new ByteArrayInputStream(inputJson.getValue().getBytes())));
//                    inputJson.clear();
//                    obligationViewer.refreshGrid();
//                } catch (EVRException e) {
//                    e.printStackTrace();
//                    System.out.println(e.getMessage());
//                } catch (PMException e) {
//                    e.printStackTrace();
//                    System.out.println(e.getMessage());
//                }
//            });
//            importButton.setHeight("10%");
//            add(inputJson);
//            add(importButton);



            MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setHeight("100%");
            upload.addSucceededListener(event -> {
                try {
                    UserContext userContext = new UserContext("-1", "-1");
                    g.addObl(EVRParser.parse(userContext.getUser(), buffer.getInputStream(event.getFileName())));
//                    inputJson.clear();
                    obligationViewer.refreshGrid();
                } catch (Exception e) {
                    ObligationEditor.notify(event.getFileName() + " failed to parse");
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            });

            add(upload);
        }
    }

    private class ObligationViewer extends VerticalLayout {
        private Grid<Obligation> obligationGrid;

        public ObligationViewer () {
            getStyle().set("background", "lightblue");
            setAlignItems(Alignment.STRETCH);

            add(new H2("Obligation Editor:"));

            obligationGrid = new Grid<>(Obligation.class);
            obligationGrid.getStyle()
                    .set("border-radius", "2px");
            obligationGrid.setColumnReorderingAllowed(true);
            obligationGrid.getColumns().forEach(col -> {
                col.setFlexGrow(1);
            });
            obligationGrid.removeColumnByKey("source");
            obligationGrid.removeColumnByKey("rules");
            add(obligationGrid);
            createContextMenu();

            refreshGrid();
        }

        public void refreshGrid() {
            List<Obligation> currObls = new ArrayList<>();
            try {
                currObls = g.getAllObls();
            } catch (PMException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
            obligationGrid.setItems(currObls);
        }

        private void createContextMenu() {
            GridContextMenu<Obligation> contextMenu = new GridContextMenu<>(obligationGrid);

            contextMenu.addItem("Add", event -> addObligation());
            contextMenu.addItem("Toggle", event -> {
                event.getItem().ifPresent(obli -> {
                    toggleObligation(obli);
                    refreshGrid();
                });
            });
            contextMenu.addItem("Edit Label", event -> {
                event.getItem().ifPresent(obli -> {
                    editLabel(obli);
                });
            });
            contextMenu.addItem("Delete", event -> {
                event.getItem().ifPresent(obli -> {
                    deleteObligation(obli);
                });
            });
            contextMenu.addItem("View Source", event -> {
                event.getItem().ifPresent(obli -> {
                    ObligationEditor.notify("View Obligaiton Source Method");
                });
            });
        }

        private void addObligation() {
            Dialog dialog = new Dialog();
            dialog.setHeight("90vh");
            dialog.setWidth("100vh");
            VerticalLayout form = new VerticalLayout();
            form.setSizeFull();
            form.setAlignItems(FlexComponent.Alignment.BASELINE);

            TextArea inputYaml = new TextArea();
            inputYaml.setPlaceholder("Type YAML code here:");
            inputYaml.setSizeFull();
            form.add(inputYaml);

            Button importButton = new Button("Parse YAML", event -> {
                try {
                    /*
                    YAML script :
                    label: homes/inboxes/outboxes
                    rules:
                        - label: test
                        event:
                        subject:
                        operations:
                            - assign to
                            target:
                            policyElements:
                                - name: users
                                type: UA
                                response:
                                actions:
                                    - function:
                                        name: create_dac_user
                                        args:
                                            - function:
                                            name: child_of_assign
                     */


                    UserContext userContext = new UserContext("-1", "-1");
                    g.addObl(EVRParser.parse(userContext.getUser(), new ByteArrayInputStream(inputYaml.getValue().getBytes())));
                    ObligationEditor.notify("Successfully imported obligation!");
                    inputYaml.clear();
                    dialog.close();
                    obligationViewer.refreshGrid();
                } catch (EVRException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                } catch (PMException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            });
            importButton.setWidthFull();
            form.add(importButton);

            dialog.add(form);
            dialog.open();
            inputYaml.focus();
        }

        private void toggleObligation(Obligation obligation) {
            obligation.setEnabled(!obligation.isEnabled());
        }

        private void editLabel(Obligation obli) {
            Dialog dialog = new Dialog();
            HorizontalLayout form = new HorizontalLayout();
            form.setAlignItems(FlexComponent.Alignment.BASELINE);

            TextField labelField = new TextField("Label");
            labelField.setRequiredIndicatorVisible(true);
            labelField.setPlaceholder("Enter Label...");
            form.add(labelField);

            Button button = new Button("Submit", event -> {
                String label = labelField.getValue();
                if (label == null || label == "") {
                    labelField.focus();
                    ObligationEditor.notify("Label is Required");
                } else {
                    obli.setLabel(label);
                    refreshGrid();
                    dialog.close();
                }
            });
            form.add(button);

            dialog.add(form);
            dialog.open();
            labelField.focus();
        }

        private void deleteObligation(Obligation n) {
            Dialog dialog = new Dialog();
            HorizontalLayout form = new HorizontalLayout();
            form.setAlignItems(FlexComponent.Alignment.BASELINE);

            form.add(new Paragraph("Are You Sure?"));

            Button button = new Button("Delete", event -> {
                try {
                    g.deleteObl(n.getLabel());
                } catch (PMException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                refreshGrid();
                dialog.close();
            });
            button.addThemeVariants(ButtonVariant.LUMO_ERROR);
            form.add(button);

            Button cancel = new Button("Cancel", event -> {
                dialog.close();
            });
            cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            form.add(cancel);

            dialog.add(form);
            dialog.open();
        }
    }

    private static void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
    }
}
