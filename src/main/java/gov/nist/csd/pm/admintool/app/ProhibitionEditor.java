package gov.nist.csd.pm.admintool.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import gov.nist.csd.pm.admintool.graph.SingletonGraph;

import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.prohibitions.model.Prohibition;

import java.util.*;

@Tag("prohibition-editor")
public class ProhibitionEditor extends VerticalLayout {
    private SingletonGraph g;
    private HorizontalLayout layout;
    private ButtonGroup buttonGroup;
    private ProhibitionViewer prohibitionViewer;
    private Prohibition selectedProhibition;

    public ProhibitionEditor() {
        g = SingletonGraph.getInstance();
        layout = new HorizontalLayout();
        layout.setFlexGrow(1.0);
        add(layout);
        setUpLayout();
    }

    private void setUpLayout() {
        setSizeFull();
        setPadding(true);

        prohibitionViewer = new ProhibitionViewer();
        prohibitionViewer.setWidth("80%");
        prohibitionViewer.getStyle().set("height","100vh");

        buttonGroup = new ButtonGroup();
        buttonGroup.setWidth("20%");
        buttonGroup.getStyle().set("height","100vh");

        layout.add(prohibitionViewer, buttonGroup);
    }

    private class ProhibitionViewer extends VerticalLayout {
        private Grid<Prohibition> grid;

        // for prohibition info section
        private H3 name;
        private Div childrenList, parentList;   // for relations
        private Div outgoingAssociationList, incomingAssociationList; // for associations
        private Div outgoingProhibitionList, incomingProhibitionList; // for prohibitions

        public ProhibitionViewer() {
            grid = new Grid<>(Prohibition.class);
            name = new H3("X");
            childrenList = new Div();
            parentList = new Div();
            outgoingAssociationList = new Div();
            incomingAssociationList = new Div();
            outgoingProhibitionList = new Div();
            incomingProhibitionList = new Div();

            setupProhibitionTableSection();
            setupProhibitionInfoSection();

        }

        public void setupProhibitionTableSection() {
            getStyle().set("background", "lightblue");
            add(new H2("Prohibition Editor:"));

            // grid config
            grid.getStyle()
                    .set("border-radius", "1px")
                    .set("user-select", "none");
            grid.removeAllColumns();
            grid.addColumn("name");
            grid.addColumn("subject");
            grid.addColumn("operations");
            grid.addColumn("containers");
            grid.addColumn("intersection");

            // Single Click Action: select node
            grid.addItemClickListener(evt -> {
//                selectedProhibition = grid.getSelectedItems().iterator().next();
//                selectedProhibition = evt.getItem();
//
//                buttonGroup.refreshButtonStates();
//                buttonGroup.refreshProhibitionText();
//
//                updateProhibitionInfoSection();
                MainView.notify("in item click listener");
            });

            createContextMenu(); // adds the content-specific context menu

            refreshGraph();

            add(grid);
        }

        public void setupProhibitionInfoSection () {
            /// PROHIBITION INFO START ///
            VerticalLayout nodeInfo = new VerticalLayout();
            nodeInfo.setWidthFull();
            nodeInfo.setHeight("30%");
            nodeInfo.getStyle()
                    .set("background", "white")
                    .set("border-radius", "2px")
                    .set("border", "1px solid lightgrey")
                    .set("padding", "10px")
                    .set("line-height", "1px")
                    .set("text-align", "center")
                    .set("overflow-y", "scroll")
                    .set("overflow-x", "hidden");



            name.setWidthFull();
            nodeInfo.add(name);

            nodeInfo.add(new Hr());


            ///// section with assignments
            Paragraph assignmentsText = new Paragraph("Assignments:");
            assignmentsText.setWidthFull();
            assignmentsText.getStyle().set("font-weight", "bold");
            nodeInfo.add(assignmentsText);

            HorizontalLayout assignments = new HorizontalLayout();
            assignments.setMargin(true);
            assignments.getStyle().set("margin-top", "0");
            assignments.getStyle().set("margin-bottom", "0");
            assignments.setWidthFull();

            // children layout
            VerticalLayout children = new VerticalLayout();
            children.setSizeFull();
            children.setMargin(true);
            children.getStyle().set("margin-top", "0");
            children.getStyle().set("margin-bottom", "0");

            children.add(new Paragraph("Children:"));


            childrenList.setSizeFull();
            childrenList.getStyle()
                    .set("margin-top", "0")
                    .set("margin-bottom", "0")
                    .set("overflow","scroll");


            children.add(childrenList);


            // parent layout
            VerticalLayout parents = new VerticalLayout();
            parents.setMargin(true);
            parents.setSizeFull();
            parents.getStyle().set("margin-top", "0");
            parents.getStyle().set("margin-bottom", "0");

            parents.add(new Paragraph("Parents: "));


            parentList.setSizeFull();
            parentList.getStyle()
                    .set("margin-top", "0")
                    .set("margin-bottom", "0")
                    .set("overflow","scroll");
//                    .set("background","green");

            parents.add(parentList);

            // adding it all together
            assignments.add(children);
            assignments.add(parents);

            nodeInfo.add(assignments);
            ///// end section with assignments



            nodeInfo.add(new Hr());



            ///// section with associations
            Paragraph associationsText = new Paragraph("Associations:");
            associationsText.setWidthFull();
            associationsText.getStyle().set("font-weight", "bold");
            nodeInfo.add(associationsText);

            HorizontalLayout associations = new HorizontalLayout();
            associations.setMargin(true);
            associations.getStyle().set("margin-top", "0");
            associations.getStyle().set("margin-bottom", "0");
            associations.setWidthFull();

            // outgoing layout
            VerticalLayout outgoing = new VerticalLayout();
            outgoing.setSizeFull();
            outgoing.setMargin(true);
            outgoing.getStyle().set("margin-top", "0");
            outgoing.getStyle().set("margin-bottom", "0");

            outgoing.add(new Paragraph("Outgoing:"));


            outgoingAssociationList.setSizeFull();
            outgoingAssociationList.getStyle()
                    .set("margin-top", "0")
                    .set("margin-bottom", "0")
                    .set("overflow","scroll");

            outgoing.add(outgoingAssociationList);


            // incoming layout
            VerticalLayout incoming = new VerticalLayout();
            incoming.setMargin(true);
            incoming.setSizeFull();
            incoming.getStyle().set("margin-top", "0");
            incoming.getStyle().set("margin-bottom", "0");

            incoming.add(new Paragraph("Incoming: "));


            incomingAssociationList.setSizeFull();
            incomingAssociationList.getStyle()
                    .set("margin-top", "0")
                    .set("margin-bottom", "0")
                    .set("overflow","scroll");

            incoming.add(incomingAssociationList);

            // adding it all together
            associations.add(outgoing);
            associations.add(incoming);

            nodeInfo.add(associations);
            ///// end section of associations


            nodeInfo.add(new Hr());

            ///// section with prohibitions
            Paragraph ProhibitonsText = new Paragraph("Prohibitions:");
            ProhibitonsText.setWidthFull();
            ProhibitonsText.getStyle().set("font-weight", "bold");
            nodeInfo.add(ProhibitonsText);

            HorizontalLayout prohibitions = new HorizontalLayout();
            prohibitions.setMargin(true);
            prohibitions.getStyle().set("margin-top", "0");
            prohibitions.getStyle().set("margin-bottom", "0");
            prohibitions.setWidthFull();

            // outgoing layout
            VerticalLayout outgoingProhibitions = new VerticalLayout();
            outgoingProhibitions.setSizeFull();
            outgoingProhibitions.setMargin(true);
            outgoingProhibitions.getStyle().set("margin-top", "0");
            outgoingProhibitions.getStyle().set("margin-bottom", "0");

            outgoingProhibitions.add(new Paragraph("Outgoing:"));


            outgoingProhibitionList.setSizeFull();
            outgoingProhibitionList.getStyle()
                    .set("margin-top", "0")
                    .set("margin-bottom", "0")
                    .set("overflow","scroll");

            outgoingProhibitions.add(outgoingProhibitionList);


            // incoming layout
            VerticalLayout incomingProhibitions = new VerticalLayout();
            incomingProhibitions.setMargin(true);
            incomingProhibitions.setSizeFull();
            incomingProhibitions.getStyle().set("margin-top", "0");
            incomingProhibitions.getStyle().set("margin-bottom", "0");

            incomingProhibitions.add(new Paragraph("Incoming: "));


            incomingProhibitionList.setSizeFull();
            incomingProhibitionList.getStyle()
                    .set("margin-top", "0")
                    .set("margin-bottom", "0")
                    .set("overflow","scroll");

            incomingProhibitions.add(incomingProhibitionList);

            // adding it all together
            prohibitions.add(outgoingProhibitions);
            prohibitions.add(incomingProhibitions);

            nodeInfo.add(prohibitions);
            ////// end section with prohibitions


            add(nodeInfo);
            /// PROHIBITION INFO END ///
        }

        private void updateProhibitionInfoSection() {
            childrenList.removeAll();
            parentList.removeAll();

            outgoingAssociationList.removeAll();
            incomingAssociationList.removeAll();

            outgoingProhibitionList.removeAll();
            incomingProhibitionList.removeAll();

            if (selectedProhibition != null) {
//                try {
//                    name.setText(selectedProhibition.getName() + " (" + selectedProhibition.getType().toString() + ")");
//
//                    //TODO: find a more expandable way to do this
//
//                    // assignments
//                    Iterator<String> childIter = SingletonGraph.getPap().getGraphPAP().getChildren(gridSelecNode.getName()).iterator();
//                    if (!childIter.hasNext()) {
//                        childrenList.add(new Paragraph("None"));
//                    } else {
//                        while (childIter.hasNext()) {
//                            String child = childIter.next();
//                            Node childParent = SingletonGraph.getPap().getGraphPAP().getNode(child);
//                            childrenList.add(new NodeDataBlip(childParent.getName(), childParent.getType()));
////                            children.setText(children.getText() + "{" + id + ": " + g.getNode(id).getName() + "},");
//                        }
//                    }
//
//                    Iterator<String> parentIter = SingletonGraph.getPap().getGraphPAP().getParents(gridSelecNode.getName()).iterator();
//                    if (!parentIter.hasNext()) {
//                        parentList.add(new Paragraph("None"));
//                    } else {
//                        while (parentIter.hasNext()) {
//                            String parent = parentIter.next();
//                            Node parentNode = SingletonGraph.getPap().getGraphPAP().getNode(parent);
//                            parentList.add(new NodeDataBlip(parentNode.getName(), parentNode.getType()));
////                            parents.setText(parents.getText() + "{" + id + ": " + g.getNode(id).getName() + "},");
//                        }
//                    }
//
//                    // associations
//                    if (gridSelecNode.getType() == NodeType.UA) {
//                        Map<String, OperationSet> outgoingMap = SingletonGraph.getPap().getGraphPAP().getSourceAssociations(gridSelecNode.getName());
//                        Iterator<String> outgoingKeySet = outgoingMap.keySet().iterator();
//                        if (!outgoingKeySet.hasNext()) {
//                            outgoingAssociationList.add(new Paragraph("None"));
//                        } else {
//                            while (outgoingKeySet.hasNext()) {
//                                String name = outgoingKeySet.next();
//                                Node node = SingletonGraph.getPap().getGraphPAP().getNode(name);
//                                outgoingAssociationList.add(new AssociationBlip(name, node.getType(), true, outgoingMap.get(name)));
//                            }
//                        }
//                    }
//
//                    if (gridSelecNode.getType() == NodeType.UA || gridSelecNode.getType() == NodeType.OA) {
//                        Map<String, OperationSet> incomingMap = SingletonGraph.getPap().getGraphPAP().getTargetAssociations(gridSelecNode.getName());
//                        Iterator<String> incomingKeySet = incomingMap.keySet().iterator();
//                        if (!incomingKeySet.hasNext()) {
//                            incomingAssociationList.add(new Paragraph("None"));
//                        } else {
//                            while (incomingKeySet.hasNext()) {
//                                String name = incomingKeySet.next();
//                                Node node = SingletonGraph.getPap().getGraphPAP().getNode(name);
//                                incomingAssociationList.add(new AssociationBlip(name, node.getType(), false, incomingMap.get(name)));
//                            }
//                        }
//                    }
//
//                    // prohibitions
//                    if (gridSelecNode.getType() == NodeType.UA || gridSelecNode.getType() == NodeType.U) {
//                        List<Prohibition> outgoingList = SingletonGraph.getPap().getProhibitionsPAP().getProhibitionsFor(gridSelecNode.getName());
//                        Iterator<Prohibition> outgoingIterator = outgoingList.iterator();
//                        if (!outgoingIterator.hasNext()) {
//                            outgoingProhibitionList.add(new Paragraph("None"));
//                        } else {
//                            while (outgoingIterator.hasNext()) {
//                                Prohibition prohibition = outgoingIterator.next();
//                                Iterator<String> containerKeySetIterator = prohibition.getContainers().keySet().iterator();
//                                while (containerKeySetIterator.hasNext()) {
//                                    String targetName = containerKeySetIterator.next();
//                                    boolean isComplement = prohibition.getContainers().get(targetName);
//                                    Node target = SingletonGraph.getPap().getGraphPAP().getNode(targetName);
//                                    outgoingProhibitionList.add(
//                                            new ProhibitonBlip(
//                                                    gridSelecNode,
//                                                    target,
//                                                    isComplement,
//                                                    prohibition.getOperations(),
//                                                    prohibition.isIntersection(),
//                                                    true
//                                            )
//                                    );
//                                }
//                            }
//                        }
//                    }
//
//                    if (gridSelecNode.getType() == NodeType.UA || gridSelecNode.getType() == NodeType.OA
//                            || gridSelecNode.getType() == NodeType.U || gridSelecNode.getType() == NodeType.O) {
//                        List<Prohibition> incomingList = SingletonGraph.getPap().getProhibitionsPAP().getAll();
//                        incomingList.removeIf(prohibition -> !prohibition.getContainers().keySet().contains(gridSelecNode.getName()));
//                        Iterator<Prohibition> incomingIterator = incomingList.iterator();
//                        if (!incomingIterator.hasNext()) {
//                            incomingProhibitionList.add(new Paragraph("None"));
//                        } else {
//                            while (incomingIterator.hasNext()) {
//                                Prohibition prohibition = incomingIterator.next();
//                                Iterator<String> containerKeySetIterator = prohibition.getContainers().keySet().iterator();
//                                while (containerKeySetIterator.hasNext()) {
//                                    String targetName = containerKeySetIterator.next();
//                                    boolean isComplement = prohibition.getContainers().get(targetName);
//                                    if (targetName.equals(gridSelecNode.getName())) {
//                                        Node subject = SingletonGraph.getPap().getGraphPAP().getNode(prohibition.getSubject());
//                                        incomingProhibitionList.add(
//                                                new ProhibitonBlip(
//                                                        subject,
//                                                        gridSelecNode,
//                                                        isComplement,
//                                                        prohibition.getOperations(),
//                                                        prohibition.isIntersection(),
//                                                        false
//                                                )
//                                        );
//                                    }
//                                }
//                            }
//                        }
//                    }
//                } catch (PMException e) {
//                    e.printStackTrace();
//                    MainView.notify(e.getMessage());
//                }
            } else {
                name.setText("X");

                childrenList.add(new Paragraph("None"));
                parentList.add(new Paragraph("None"));

                outgoingAssociationList.add(new Paragraph("None"));
                incomingAssociationList.add(new Paragraph("None"));

                outgoingProhibitionList.add(new Paragraph("None"));
                incomingProhibitionList.add(new Paragraph("None"));
            }
        }

        public void updateGrid() {
            List<Prohibition> prohibitions = null;
            try {
                prohibitions = g.getAllProhibitions();
            } catch (PMException e) {
                e.printStackTrace();
            }
            grid.setItems(prohibitions);
        }

        public void refreshGraph() {
            grid.deselectAll();
            updateGrid();
            selectedProhibition = null;
            if (buttonGroup != null) {
                buttonGroup.refreshProhibitionText();
                buttonGroup.refreshButtonStates();
            }
        }

        private void createContextMenu() {
            GridContextMenu<Prohibition> contextMenu = new GridContextMenu<>(grid);

            //contextMenu.addItem("Add Node", event -> addNode());
            contextMenu.addItem("Edit Node", event -> {
                event.getItem().ifPresent(prohibition -> {
                    editProhibition(prohibition);
                });
            });
            contextMenu.addItem("Delete Node", event -> {
                event.getItem().ifPresent(prohibition -> {
                    deleteProhibition(prohibition);
                });
            });


        }
    }

    private class ButtonGroup extends VerticalLayout {
        private Button addProhibitionButton, editProhibitionButton, deleteProhibitionButton;
        private H4 selectedProhibitionText;
        public ButtonGroup() {
            getStyle().set("background", "#DADADA") //#A0FFA0
                    .set("overflow-y", "scroll");
            setAlignItems(Alignment.CENTER);
            setJustifyContentMode(JustifyContentMode.START);

            selectedProhibitionText = new H4("X");

            add(new Paragraph("\n"));
            add(selectedProhibitionText);
            add(new Paragraph("\n"), new Paragraph("\n"));

            createButtons();
        }

        private void createButtons() {
            // Prohibition Buttons
            addProhibitionButton = new Button("Add Prohibition", evt -> {
                addProhibition();
            });
            addProhibitionButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            addProhibitionButton.setEnabled(true);
            addProhibitionButton.setWidthFull();
            add(addProhibitionButton);

            editProhibitionButton = new Button("Edit Prohibition", evt -> {
                editProhibition(selectedProhibition);
            });
            editProhibitionButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            editProhibitionButton.setEnabled(true);
            editProhibitionButton.setWidthFull();
            add(editProhibitionButton);

            deleteProhibitionButton = new Button("Delete Prohibition", evt -> {
                deleteProhibition(selectedProhibition);
            });
            deleteProhibitionButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteProhibitionButton.setEnabled(true);
            deleteProhibitionButton.setWidthFull();
            add(deleteProhibitionButton);
            add(new Paragraph("\n"));
        }

        public void refreshProhibitionText() {
            if (selectedProhibition != null) {
                selectedProhibitionText.setText(selectedProhibition.getName());
            } else {
                selectedProhibitionText.setText("X");
            }
        }

        public void refreshButtonStates() {
            if (selectedProhibitionText != null) {
                editProhibitionButton.setEnabled(true);
                deleteProhibitionButton.setEnabled(true);
            } else {
                editProhibitionButton.setEnabled(true);
                deleteProhibitionButton.setEnabled(true);
            }
        }
    }

    private void addProhibition() {
//        Dialog dialog = new Dialog();
//
//        HorizontalLayout form = new HorizontalLayout();
//        // form.setAlignItems(Alignment.BASELINE);
//
//        TextField nameField = new TextField("Prohibition Name");
//        form.add(nameField);
//
//        TextField subjectName = new TextField("Subject Name");
//        form.add(subjectName);
//
//        TextArea opsFeild = new TextArea("Operations (Op1, Op2, ...)");
//        opsFeild.setPlaceholder("Enter Operations...");
//        form.add(opsFeild);
//
//        MapInput<String, Boolean> containerField = new MapInput<>(TextField.class, Checkbox.class);
//        containerField.setLabel("Containers (Target, Complement)");
//        form.add (containerField);
//
//        Checkbox intersectionFeild = new Checkbox("Intersection");
//        VerticalLayout intersectionFeildLayout = new VerticalLayout(intersectionFeild);
//        form.add(intersectionFeildLayout);
//
//        Button submit = new Button("Submit", event -> {
//            String name = nameField.getValue();
//            String opString = opsFeild.getValue();
//            OperationSet ops = new OperationSet();
//            boolean intersection = intersectionFeild.getValue();
//            Map<String, Boolean> containers = containerField.getValue();
//            if (opString == null || opString.equals("")) {
//                opsFeild.focus();
//                MainView.notify("Operations are Required");
//            } else if (name == null || name.equals("")) {
//                nameField.focus();
//                MainView.notify("Name is Required");
//            } else if (containers.isEmpty()) {
//                MainView.notify("Containers are Required");
//            } else {
//                try {
//                    for (String op : opString.split(",")) {
//                        ops.add(op.replaceAll(" ", ""));
//                    }
//                } catch (Exception e) {
//                    MainView.notify("Incorrect Formatting of Operations");
//                    e.printStackTrace();
//                }
//                try {
//                    g.addProhibition(nameField.getValue(), subjectName.getValue(), containers, ops, intersection);
//                    dialog.close();
//                } catch (PMException e) {
//                    MainView.notify(e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//        });
//        VerticalLayout submitLayout = new VerticalLayout(submit);
//        form.add(submitLayout);
//
//        dialog.add(form);
//        dialog.open();
//        opsFeild.focus();
    }

    private void editProhibition(Prohibition prohibition) {}

    private void deleteProhibition(Prohibition prohibition) {}

}

