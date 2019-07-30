package gov.nist.csd.pm.admintool.tests;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class Test {
    private Map<String, Element> params = new HashMap<>();
    private String name;

    public int getParamsLength() {
        return params.keySet().size();
    }

    protected Map<String, Element> getParams() {
        return params;
    }

    public Map<String, Type> getParamNameAndType() {
        HashMap<String, Type> ret = new HashMap<>();
        for (String key: params.keySet()) {
            ret.put(key, params.get(key).type);
        }
        return ret;
    }

    protected Element getElement(String paramName) {
        return params.get(paramName);
    }

    protected void addParam(String paramName, Type paramType) {
        switch (paramType) {
            case NODETYPE:
                params.put(paramName, new Element<Node>(paramType));
                break;
            case OPERATION:
                params.put(paramName, new Element<String>(paramType));
                break;
            case STRING:
                params.put(paramName, new Element<String>(paramType));
                break;
        }
    }

    public void setParamValue (String paramName, Object paramValue) {
        Element element = params.get(paramName);
        if (element != null) {
            element.setValue(paramValue);
        } else {
            // todo: throw exception
            System.out.println("no such element with param name: " + paramName);
        }
    }


    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }


    public abstract boolean runTest();

    @Override
    public String toString() {
        String ret = "";
        ret += name;
        ret += "(";
        String[] keys = params.keySet().toArray(new String[params.size()]);
        for (int i = 0; i < params.size(); i++) {
            ret += keys[i] + ": ";
            Object val = params.get(keys[i]).getValue();
            if (val instanceof Node) {
                ret += ((Node) val).getName();
            } else {
                ret += val;
            }
            if (i != params.size() - 1) {
                ret += ", ";
            }
        }
        ret += ")";
        return ret;
    }

    public enum Type {
        NODETYPE, OPERATION, STRING;
    }

    protected class Element<K> {
        Type type;
        K value;
        public Element(Type type) {
            this.type = type;
            value = null;
        }

        public K getValue() {
            return value;
        }

        public void setValue(K value) {
            this.value = value;
        }
    }
}