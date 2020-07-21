package gov.nist.csd.pm.admintool.app.customElements;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.*;

public class MapInput<K, V> extends VerticalLayout {
    private Set<Row> rows = new HashSet<>();

    private Class keyField, valueField;
    private Div label;
    private Div rowsSection;

    public MapInput(Class keyField, Class valueField) {

        this.keyField = keyField;
        this.valueField = valueField;

        label = new Div();
        label.getStyle().set("font-size", "13px");
        add(label);

        rowsSection = new Div();
        rowsSection.getStyle().set("margin", "0").set("padding", "0");
        add(rowsSection);

        getStyle().remove("width");

        addInputRow();
    }

    private void addInputRow() {
        Row newRow = new Row(null, null, true);
        rows.add(newRow);
        rowsSection.add(newRow);
    }

    private void addRow (K key, V value) {
        Row newRow = new Row(key, value, false);
        rows.add(newRow);
        rowsSection.add(newRow);
    }

    public void setInputRowValues(K key, V value) {
        Row first = rows.iterator().next();
        first.setKey(key);
        first.setValue(value);
    }

    public void deleteRow (K key) {
        Row found = null;
        for (Row row : rows) {
            if (row.getKey() == key) {
                found = row;
            }
        }

        if (found != null) {
            rows.remove(found);
            rowsSection.remove(found);
        }
    }

    public void setLabel (String label) {
        this.label.setText(label);
    }

    public Map<K, V> getValue () {
        Map<K, V> ret = new HashMap<>();
        for (Row row : rows) {
            ret.put(row.getKey(), row.getValue());
        }
        return ret;
    }

    public static void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
    }



    private class Row extends HorizontalLayout {
        private AbstractField keyFieldInstance, valueFieldInstance;

        private Row(K key, V value, boolean input) {
            setAlignItems(Alignment.CENTER);

            if (input) {
                Button addButton = new Button(new Icon(VaadinIcon.PLUS));
                addButton.addClickListener(buttonClickEvent -> addRow(null, null));
                add(addButton);
            } else {
                Button deleteButton = new Button(new Icon(VaadinIcon.MINUS));
                deleteButton.addClickListener(buttonClickEvent -> deleteRow(getKey()));
                add(deleteButton);
            }

            try {
                Object keyFieldObject = keyField.newInstance();
                if (keyFieldObject instanceof AbstractField) {
                    keyFieldInstance = (AbstractField) keyFieldObject;
                    if (key != null)
                        keyFieldInstance.setValue(key);
                    add(keyFieldInstance);
                }
                Object valueFieldObject = valueField.newInstance();
                if (valueFieldObject instanceof AbstractField) {
                    valueFieldInstance = (AbstractField) valueFieldObject;
                    if (value != null)
                        valueFieldInstance.setValue(value);
                    add(valueFieldInstance);
                }
            } catch (InstantiationException e) {
                MapInput.notify(e.getMessage());
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                MapInput.notify(e.getMessage());
                e.printStackTrace();
            }
        }

        public Component getKeyFieldInstance() { return keyFieldInstance; }
        public Component getValueFieldInstance() { return valueFieldInstance; }

        public K getKey() { return (K) keyFieldInstance.getValue(); }
        public V getValue() { return (V) valueFieldInstance.getValue(); }
        public void setKey(K key) {  keyFieldInstance.setValue(key); }
        public void setValue(V value) { valueFieldInstance.setValue(value); }
    }
}
