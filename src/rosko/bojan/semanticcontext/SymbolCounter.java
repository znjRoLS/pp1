package rosko.bojan.semanticcontext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

/**
 * Created by rols on 4/26/17.
 */
public class SymbolCounter<T> {

    private HashMap<T, Integer> counters;

    SymbolCounter() {
        counters = new HashMap<>();
    }

    public void inc(T name) {
        int count = counters.containsKey(name)?counters.get(name):0;
        counters.put(name,count + 1);
    }

    public int get(T name) {
        int count = counters.containsKey(name)?counters.get(name):0;

        return count;
    }

    public String printAllCounts() {
        StringBuilder sb = new StringBuilder();
        for (HashMap.Entry<T, Integer> par : counters.entrySet()) {
            sb.append(par.getKey() + " - " + par.getValue() + "\n");
        }
        return sb.toString();
    }
}
