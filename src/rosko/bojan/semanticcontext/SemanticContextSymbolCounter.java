package rosko.bojan.semanticcontext;

/**
 * Created by rols on 5/5/17.
 */
public class SemanticContextSymbolCounter {

    // for communication with counter
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

    private SemanticContext ctx;

    SymbolCounter<String> symbolByNameCounter;
    SymbolCounter<CountType> symbolCounter;

    public SemanticContextSymbolCounter() {
        symbolByNameCounter = new SymbolCounter<String>();
        symbolCounter = new SymbolCounter<CountType>();
    }

    public SemanticContextSymbolCounter(SemanticContext ctx) {
        this();
        this.ctx = ctx;
    }

    public void updateCounters(SemanticContext.SemanticSymbol type, SemanticParameters parameters) {
        if (type == SemanticContext.SemanticSymbol.CONST) {
            symbolByNameCounter.inc(getCounterContext() + "const");
            symbolCounter.inc(CountType.GLOBAL_CONST);
        }
        if (type == SemanticContext.SemanticSymbol.VAR) {
            symbolByNameCounter.inc(getCounterContext() +"var");
            if (ctx.currClassName != null && ctx.currMethodName != null) {
                // what here?
            } else if (ctx.currClassName != null) {
                symbolCounter.inc(CountType.CLASS_VAR);
            } else if (ctx.currMethodName != null) {

                if (ctx.currMethodName == "main") {
                    symbolCounter.inc(CountType.MAIN_VAR);
                }
            } else {
                symbolCounter.inc(CountType.GLOBAL_VAR);
            }
        }
        if (type == SemanticContext.SemanticSymbol.ARRAY) {
            if (ctx.currMethodName == null && ctx.currClassName == null) {
                symbolCounter.inc(CountType.GLOBAL_ARRAY);
            }
            symbolByNameCounter.inc(getCounterContext() + "array");
        }
        if (type == SemanticContext.SemanticSymbol.METHOD) {
            if (ctx.currClassName != null) {
                if (ctx.isCurrMethodStatic) {
                    symbolCounter.inc(CountType.CLASS_STATIC_METHOD);
                } else {
                    symbolCounter.inc(CountType.CLASS_METHOD);
                }
            } else {
                symbolCounter.inc(CountType.GLOBAL_METHOD);
            }
            symbolByNameCounter.inc(getCounterContext() + "method");
        }
        if (type == SemanticContext.SemanticSymbol.CLASS) {
            symbolCounter.inc(CountType.CLASS);
            symbolByNameCounter.inc(getCounterContext() + "class");
        }
        if (type == SemanticContext.SemanticSymbol.FORMAL_PARAMETER) {
            symbolCounter.inc(CountType.FORMAL_ARGUMENT);
            symbolByNameCounter.inc(getCounterContext() + "formal");
        }
        if (type == SemanticContext.SemanticSymbol.STATEMENT_BLOCK) {
            symbolCounter.inc(CountType.STATEMENT_BLOCK);
        }
        if (type == SemanticContext.SemanticSymbol.METHOD_CALL) {
            if (ctx.currMethodName == "main") {
                symbolCounter.inc(CountType.MAIN_METHOD_CALL);
            }
        }
    }


    private String getCounterContext() {
        String res = "";
        for(int i = 0 ; i < ctx.statementBlockLevel; i ++) {
            res = "{} | " + res;
        }

        if (ctx.currMethodName != null) {
            res = ctx.currMethodName + " | " + res;
        }
        if (ctx.currClassName != null) {
            res = ctx.currClassName + " | " + res;
        }

        return res;
    }
}
