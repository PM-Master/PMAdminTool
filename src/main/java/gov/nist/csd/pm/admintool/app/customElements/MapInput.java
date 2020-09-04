package gov.nist.csd.pm.admintool.app.customElements;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gov.nist.csd.pm.admintool.app.MainView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapInput<K, V> extends VerticalLayout {
    private Set<Row> rows = new HashSet<>();

    private Class keyField, valueField;
    private FieldConfig keyFieldConfig, valueFieldConfig;
    private FieldRetriever<K> keyFieldRetriever;
    private FieldRetriever<V> valueFieldRetriever;
    private Div label;
    private Div rowsSection;

    public MapInput(Class keyField, Class valueField,
                    FieldConfig keyFieldConfig, FieldConfig valueFieldConfig,
                    FieldRetriever keyFieldRetriever, FieldRetriever valueFieldRetriever) {
        this.keyField = keyField;
        this.valueField = valueField;

        this.keyFieldConfig = keyFieldConfig;
        this.valueFieldConfig = valueFieldConfig;

        this.keyFieldRetriever = keyFieldRetriever;
        this.valueFieldRetriever = valueFieldRetriever;

        label = new Div();
        label.getStyle().set("font-size", "13px");
        add(label);

        rowsSection = new Div();
        rowsSection.getStyle().set("margin", "0").set("padding", "0");
        add(rowsSection);

        getStyle().remove("width");

        addRow(null, null);
    }


    private void addRow (K key, V value) {
        Row newRow = new Row(key, value);
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
            if (row.getKey() != null)
                ret.put(row.getKey(), row.getValue());
        }
        return ret;
    }

    public void setValue (Map<K, V> fieldValue) {
        rows.clear();
        rowsSection.removeAll();

        for (K key: fieldValue.keySet()) {
            addRow(key, fieldValue.get(key));
        }
    }

    private class Row extends HorizontalLayout {
        private AbstractField keyFieldInstance, valueFieldInstance;

        private Row(K key, V value) {
            setAlignItems(Alignment.CENTER);

            if (rows.isEmpty()) {
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
                    if (keyFieldConfig != null)
                        keyFieldConfig.config(keyFieldInstance);
                    if (key != null)
                        keyFieldInstance.setValue(key);
                    add(keyFieldInstance);
                }

                Object valueFieldObject = valueField.newInstance();
                if (valueFieldObject instanceof AbstractField) {
                    valueFieldInstance = (AbstractField) valueFieldObject;
                    if (valueFieldConfig != null)
                        valueFieldConfig.config(valueFieldInstance);
                    if (value != null)
                        valueFieldInstance.setValue(value);
                    add(valueFieldInstance);
                }
            } catch (InstantiationException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
                e.printStackTrace();
            }
        }

        public Component getKeyFieldInstance() { return keyFieldInstance; }
        public Component getValueFieldInstance() { return valueFieldInstance; }

        public K getKey() {
            if (keyFieldRetriever != null)
                return (K) keyFieldRetriever.retrieve(keyFieldInstance);
            else
                return (K) keyFieldInstance.getValue();
        }
        public V getValue() {
            if (valueFieldRetriever != null)
                return (V) valueFieldRetriever.retrieve(valueFieldInstance);
            else
                return (V) valueFieldInstance.getValue();
        }
        public void setKey(K key) {  keyFieldInstance.setValue(key); }
        public void setValue(V value) { valueFieldInstance.setValue(value); }
    }

    @FunctionalInterface
    public interface FieldConfig {
        void config(AbstractField field);
    }

    @FunctionalInterface
    public interface FieldRetriever<T> {
        T retrieve(AbstractField field);
    }
}
