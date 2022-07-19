package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import gov.nist.csd.pm.admintool.graph.SingletonClient;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.obligations.evr.EVRException;
import gov.nist.csd.pm.pip.obligations.model.Obligation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static gov.nist.csd.pm.operations.Operations.*;

@Tag("obligation-editor")
public class ObligationEditor extends VerticalLayout {
    private SingletonClient g;
    private HorizontalLayout layout;
    private YamlImporter yamlImporter;
    private ObligationViewer obligationViewer;

    public ObligationEditor() throws PMException {
        g = SingletonClient.getInstance();

        // check permissions
        if (!g.checkPermissions("super_oa", GET_OBLIGATION, UPDATE_OBLIGATION, DELETE_OBLIGATION, ENABLE_OBLIGATION))
            throw new PMException("Current user ('" + g.getCurrentContext() + "') does not have adequate permissions to use obligation editor");


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

        yamlImporter = new YamlImporter();
        yamlImporter.setWidth("35%");
        yamlImporter.getStyle().set("height","100vh");
        layout.add(yamlImporter);
    }

    private class YamlImporter extends VerticalLayout {
        public YamlImporter() {
            getStyle().set("background", "lightcoral");
            setAlignItems(Alignment.STRETCH);

            Button addObligationButton = new Button("Add Obligation");
            addObligationButton.addClickListener((clickEvent) -> {
                addObligation(true, "", "");
            });
            addObligationButton.setWidthFull();
            add(addObligationButton);

            MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setHeightFull();
            upload.addSucceededListener(event -> {
                try {
                    String source = readInputStream(buffer.getInputStream(event.getFileName()));
                    String fileName = event.getFileName();
                    if (fileName.endsWith(".yml")) {
                        fileName = fileName.substring(0, fileName.length() - 4);
                    }
                    addObligation(true, fileName, source);
                    obligationViewer.refreshGrid();
                } catch (Exception e) {
                    MainView.notify(event.getFileName() + " failed to parse", MainView.NotificationType.ERROR);
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
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            obligationGrid.setItems(currObls);
        }

        private void createContextMenu() {
            GridContextMenu<Obligation> contextMenu = new GridContextMenu<>(obligationGrid);

//            contextMenu.addItem("Add", event -> addObligation(true, "", ""));
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
                    viewSource(obli);
                });
            });
        }
    }

    private void addObligation(boolean enabledValue, String labelValue, String sourceValue) {
        Dialog dialog = new Dialog();
        dialog.setHeight("90vh");
        dialog.setWidth("100vh");

        VerticalLayout form = new VerticalLayout();
        form.setSizeFull();
        form.setAlignItems(FlexComponent.Alignment.BASELINE);

        TextArea sourceField = new TextArea("YAML Code: (type four spaces instead of using tab)");
        sourceField.setPlaceholder("Type YAML code here:");
        sourceField.setValue(sourceValue);
        sourceField.setWidthFull();
        sourceField.setHeight("97%");
        sourceField.setMaxHeight("97%");
        sourceField.getStyle().set("font-family", "monospace");
        form.add(sourceField);

        Checkbox enabledField = new Checkbox("Enabled");
        enabledField.setValue(enabledValue);
        enabledField.setWidth("20%");

        TextField labelField = new TextField();
        labelField.setPlaceholder("Label...");
        labelField.setValue(labelValue);
        labelField.setWidth("60%");

        Button importButton = new Button("Parse YAML", event -> {
            boolean enabled = enabledField.getValue();
            String label = labelField.getValue();
            String source = sourceField.getValue();
            if (label == null || label.equals("")) {
                labelField.focus();
                MainView.notify("Must Choose a label");
            } if (source == null || source.equals("")) {
                sourceField.focus();
                MainView.notify("Must have a source");
            } else {
                try {
                    Obligation obligation = g.parseObligationYaml(source);
                    obligation.setEnabled(enabled);
                    obligation.setLabel(label);
                    g.addObl(obligation);
                    MainView.notify("Successfully imported obligation!", MainView.NotificationType.SUCCESS);
                    sourceField.clear();
                    dialog.close();
                    obligationViewer.refreshGrid();
                } catch (EVRException e) {
                    e.printStackTrace();
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                } catch (PMException e) {
                    e.printStackTrace();
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                }
            }
        });
        importButton.setWidth("20%");

        HorizontalLayout horizontalLayout = new HorizontalLayout(enabledField, labelField, importButton);
        horizontalLayout.setPadding(false);
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(Alignment.CENTER);
        form.add(horizontalLayout);

        dialog.add(form);
        dialog.open();
        sourceField.focus();
    }

    private void toggleObligation(Obligation obligation) {
        try {
            obligation.setEnabled(!obligation.isEnabled());
            g.updateObl(obligation.getLabel(), obligation);

            obligationViewer.refreshGrid();
        } catch (PMException e) {
            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
            e.printStackTrace();
        }
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
                MainView.notify("Label is Required", MainView.NotificationType.DEFAULT);
            } else {
                try {
                    String oldLabel = obli.getLabel();
                    obli.setLabel(label);
                    g.updateObl(oldLabel, obli);

                    obligationViewer.refreshGrid();
                } catch (PMException e) {
                    MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                    e.printStackTrace();
                }
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
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
            obligationViewer.refreshGrid();
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

    private void viewSource(Obligation obli) {
        Dialog dialog = new Dialog();
        dialog.setHeight("90vh");
        dialog.setWidth("100vh");

        String obligationSource = obli.getSource();

        VerticalLayout sourceLayout = new VerticalLayout();
        sourceLayout.setWidthFull();
        sourceLayout.setHeight("95%");
        sourceLayout.getStyle()
                .set("padding-bottom", "0px")
                .set("overflow-y", "scroll")
                .set("border", "1px #E5E4E2")
                .set("border-radius", "3px")
                .set("background", "#E5E4E2")
                .set("padding", "30px")
                .set("font-family", "Courier, monospace");;
        String[] split = obligationSource.split("\n");
        if (split.length > 1) {
            for (String line : split) {
                int spaces = 0;
                while (line.startsWith(" ")) {
                    spaces++;
                    line = line.substring(1);
                }

                Span lineSpan = new Span(line);
                lineSpan.getStyle()
                        .set("margin", "0")
                        .set("padding-left", ((Integer) (spaces * 10)).toString() + "px")
                        .set("padding", "0");
                sourceLayout.add(lineSpan);
            }
        } else {
            sourceLayout.add(new Span(obligationSource));
        }
        dialog.add(sourceLayout);

        Button okButton = new Button("Continue");
        okButton.setWidthFull();
        okButton.addClickListener(clickEvent -> dialog.close());
        okButton.addClickShortcut(Key.ENTER);
        dialog.add(okButton);

        dialog.open();
    }

    private String readInputStream(InputStream in) {
        //Creating a Scanner object
        Scanner sc = new Scanner(in);

        //Reading line by line from scanner to StringBuffer
        StringBuffer sb = new StringBuffer();
        while(sc.hasNext()){
            sb.append(sc.nextLine());
            sb.append("\n");
        }
        return sb.toString();
    }
}
