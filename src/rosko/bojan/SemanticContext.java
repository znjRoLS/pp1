package rosko.bojan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.awt.Symbol;

import static rosko.bojan.SemanticContext.SemanticSymbol.*;

/**
 * Created by rols on 4/26/17.
 */
public class SemanticContext {

    public enum CountType {
        GLOBAL_VAR,
        MAIN_VAR,
        GLOBAL_CONST,
        GLOBAL_ARRAY,
        GLOBAL_METHOD,
        CLASS_STATIC_METHOD,
        STATEMENT_BLOCK,
        MAIN_METHOD_CALL,
        FORMAL_ARGUMENT,
        CLASS,
        CLASS_METHOD,
        CLASS_VAR
    }

    public SymbolCounter<String> symbolByNameCounter = new SymbolCounter<String>();
    public SymbolCounter<CountType> symbolCounter = new SymbolCounter<CountType>();


    public enum SemanticSymbol {
        CONST,
        VAR,
        ARRAY,
        METHOD,
        METHOD_EXIT,
        CLASS,
        CLASS_EXIT,
        FORMAL_PARAMETER,
        STATEMENT_BLOCK,
        STATEMENT_BLOCK_EXIT,
        METHOD_CALL
    }

    String currClass;
    String currMethod;
    int statementBlockLevel;
    boolean isCurrMethodStatic;
    Logger logger = LogManager.getLogger(SemanticContext.class);

    SemanticContext() {
        currClass = null;
        currMethod = null;
        isCurrMethodStatic = false;
        statementBlockLevel = 0;
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
            symbolByNameCounter.inc(getContext() + "const");
            if(currMethod != null || currClass != null) {
                System.err.println("error?");
            }
            symbolCounter.inc(CountType.GLOBAL_CONST);
        }
        if (type == VAR) {
            symbolByNameCounter.inc(getContext() +"var");
            if (currClass != null && currMethod != null) {
                // what here?
            } else if (currClass != null) {
                symbolCounter.inc(CountType.CLASS_VAR);
            } else if (currMethod != null) {
                // what here?
                if (currMethod == "main") {
                    symbolCounter.inc(CountType.MAIN_VAR);
                }
            } else {
                symbolCounter.inc(CountType.GLOBAL_VAR);
            }
        }
        if (type == ARRAY) {
            if (currMethod == null && currClass == null) {
                symbolCounter.inc(CountType.GLOBAL_ARRAY);
            }
            symbolByNameCounter.inc(getContext() + "array");
        }
        if (type == METHOD) {
            if (currClass != null) {
                if (isCurrMethodStatic) {
                    symbolCounter.inc(CountType.CLASS_STATIC_METHOD);
                } else {
                    symbolCounter.inc(CountType.CLASS_METHOD);
                }
            } else {
                symbolCounter.inc(CountType.GLOBAL_METHOD);
            }
            symbolByNameCounter.inc(getContext() + "method");
            enterMethod(name);
        }
        if (type == METHOD_EXIT) {
            exitMethod();
        }
        if (type == CLASS) {
            symbolCounter.inc(CountType.CLASS);
            symbolByNameCounter.inc(getContext() + "class");
            enterClass(name);
        }
        if (type == CLASS_EXIT) {
            exitClass();
        }
        if (type == FORMAL_PARAMETER) {
            symbolCounter.inc(CountType.FORMAL_ARGUMENT);
            symbolByNameCounter.inc(getContext() + "formal");
        }
        if (type == STATEMENT_BLOCK) {
            statementBlockLevel ++;
            symbolCounter.inc(CountType.STATEMENT_BLOCK);
        }
        if (type == STATEMENT_BLOCK_EXIT) {
            statementBlockLevel--;
        }
        if (type == METHOD_CALL) {
            if (currMethod == "main") {
                symbolCounter.inc(CountType.MAIN_METHOD_CALL);
            }
        }
    }

    public String getContext() {
        String res = "";
        for(int i = 0 ; i < statementBlockLevel; i ++) {
            res = "{} | " + res;
        }

        if (currMethod != null) {
            res = currMethod + " | " + res;
        }
        if (currClass != null) {
            res = currClass + " | " + res;
        }

        return res;
    }

    private void report_info(String msg) {
        logger.info(msg);
    }

}
