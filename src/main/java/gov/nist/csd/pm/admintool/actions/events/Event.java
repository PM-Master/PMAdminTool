package gov.nist.csd.pm.admintool.actions.events;
import gov.nist.csd.pm.admintool.actions.Action;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;

public abstract class Event extends Action {
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
}