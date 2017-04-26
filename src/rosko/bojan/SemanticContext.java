package rosko.bojan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static rosko.bojan.SemanticContext.SemanticSymbol.*;

/**
 * Created by rols on 4/26/17.
 */
public class SemanticContext {

    public SymbolCounter symCnt = new SymbolCounter();

    public enum SemanticSymbol {
        CONST,
        VAR,
        ARRAY,
        METHOD,
        METHOD_EXIT,
        CLASS,
        CLASS_EXIT,
    }

    String currClass;
    String currMethod;
    boolean isCurrMethodStatic;
    Logger logger = LogManager.getLogger(SemanticContext.class);

    SemanticContext() {
        currClass = null;
        currMethod = null;
        isCurrMethodStatic = false;
    }

    public void setCurrMethodStatic() {
        isCurrMethodStatic = true;
    }

    public void enterClass(String name) {
        report_info("Entered clas: " + currClass);
        currClass = name;
    }

    public void exitClass() {
        report_info("Exited class: " + currClass);
        currClass = null;
    }

    public void enterMethod(String name) {
        report_info("Entered method: " + currMethod);
        currMethod = name;
        isCurrMethodStatic = false;
    }

    public void exitMethod() {
        report_info("Exited method: " + currMethod);
        currMethod = null;
    }

    public String getCurrMethod() {
        return currMethod;
    }

    public String getCurrClass() {
        return currClass;
    }

    public void foundSymbol(SemanticSymbol type, String name) {

        report_info("foundsymbol " + type + " - " + name);

        if (type == CONST) {
            symCnt.inc(getContext() + "const");
        }
        if (type == VAR) {
            symCnt.inc(getContext() +"var");
        }
        if (type == ARRAY) {
            symCnt.inc(getContext() + "array");
        }
        if (type == METHOD) {
            symCnt.inc(getContext() + "method");
            enterMethod(name);
        }
        if (type == METHOD_EXIT) {
            exitMethod();
        }
        if (type == CLASS) {
            symCnt.inc(getContext() + "class");
            enterClass(name);
        }
        if (type == CLASS_EXIT) {
            exitClass();
        }
    }

    public String getContext() {
        String res = "";
        if (currMethod != null) {
            res = currMethod + "|" + res;
        }
        if (currClass != null) {
            res = currClass + "|" + res;
        }
        return res;
    }

    private void report_info(String msg) {
        logger.info(msg);
    }

}
