package rosko.bojan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.HashMap;

/**
 * Created by rols on 4/26/17.
 */
public class SymbolCounter<T> {

    Logger logger = LogManager.getLogger(SymbolCounter.class);
    private HashMap<T, Integer> counters;

    SymbolCounter() {
        counters = new HashMap<>();
    }

    public void inc(T name) {
        int count = counters.containsKey(name)?counters.get(name):0;
        counters.put(name,count + 1);
        logger.debug("counter: " + name + " | value: " + count + 1);
    }

    public int get(T name) {
        int count = counters.containsKey(name)?counters.get(name):0;

        return count;
    }

    public void printAllCounts() {
        for (HashMap.Entry<T, Integer> par : counters.entrySet()) {
            logger.info(par.getKey() + " - " + par.getValue());
        }
    }
}
