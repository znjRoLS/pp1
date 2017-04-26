package rosko.bojan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.HashMap;

/**
 * Created by rols on 4/26/17.
 */
public class SymbolCounter {

    Logger logger = LogManager.getLogger(SymbolCounter.class);
    private HashMap<String, Integer> counters;

    SymbolCounter() {
        counters = new HashMap<>();
    }

    public void inc(String name) {
        int count = counters.containsKey(name)?counters.get(name):0;
        counters.put(name,count + 1);
        logger.debug("counter: " + name + " | value: " + count + 1);
    }

    public int get(String name) {
        int count = counters.containsKey(name)?counters.get(name):0;

        return count;
    }

    public void printAllCounts() {
        for (HashMap.Entry<String, Integer> par : counters.entrySet()) {
            logger.info(par.getKey() + " - " + par.getValue());
        }
    }
}
