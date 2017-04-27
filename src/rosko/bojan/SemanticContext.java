package rosko.bojan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;


import java.util.HashMap;

import static rosko.bojan.SemanticContext.SemanticSymbol.*;

/**
 * Created by rols on 4/26/17.
 */
public class SemanticContext {

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


    // for communication from parser
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
        METHOD_CALL,
        STATIC,
        TYPE,
        PROGRAM,
        PROGRAM_EXIT,
        DESIGNATOR,
        TYPE_CLASS
    }

    private static HashMap<String,Integer> objectType = new HashMap<String,Integer>(){
        {
            put("int", Struct.Int);
            put("char", Struct.Char);
            put("bool", Struct.Bool);
            put("void", Struct.None);
            put("class", Struct.Class);
        }
    };

    public SymbolCounter<String> symbolByNameCounter;
    public SymbolCounter<CountType> symbolCounter;

    private String currClassName;
    private String currMethodName;
    private Obj currMethod;
    private Obj currClass;
    private Obj programObj;
    private Struct currClassStruct;
    private Obj lastConstDeclared;
    private int statementBlockLevel;
    private boolean isCurrMethodStatic;
    private int currentDeclarationType;
    private Logger logger = LogManager.getLogger(SemanticContext.class);

    SemanticContext() {
        currClassName = null;
        currMethodName = null;
        isCurrMethodStatic = false;
        statementBlockLevel = 0;
        symbolByNameCounter = new SymbolCounter<String>();
        symbolCounter = new SymbolCounter<CountType>();
        lastConstDeclared = null;
        currMethod = null;
        currClass = null;
    }

    public void dumpTable() {
        Tab.dump();
    }

    private void setCurrMethodStatic() {
        isCurrMethodStatic = true;
    }

    public String getCurrMethod() {
        return currMethodName;
    }

    public String getCurrClass() {
        return currClassName;
    }

    private void setConstIntValue(int value) {
        if (lastConstDeclared == null) {
            report_error("what now?");
        }
        lastConstDeclared.setAdr(value);
        lastConstDeclared = null;
    }

    public void setConstValue(int value) {
        if (currentDeclarationType != Struct.Int) {
            report_error("what now?");
        }
        setConstIntValue(value);
    }

    public void setConstValue(char value) {
        if (currentDeclarationType != Struct.Char) {
            report_error("what now?");
        }
        setConstIntValue((int)value);

    }

    public void setConstValue(boolean value) {
        if (currentDeclarationType != Struct.Bool) {
            report_error("what now?");
        }
        setConstIntValue(value?1:0);
    }

    public void foundSymbol(SemanticSymbol type, String name) {
        report_info("foundsymbol " + type + " - " + name);

        updateCounters(type, name);
        updateSymbolTable(type, name);
        updateContext(type, name);
    }



    public void updateSymbolTable(SemanticSymbol type, String name) {
        if (type == CONST) {
            lastConstDeclared = Tab.insert(Obj.Con, name, new Struct(currentDeclarationType));
        }
        if (type == VAR) {
            Tab.insert(Obj.Var, name, new Struct(currentDeclarationType));
        }
        if (type == ARRAY) {
            Struct arrType = new Struct(Struct.Array);
            arrType.setElementType(new Struct(currentDeclarationType));
            Tab.insert(Obj.Var, name, arrType);
        }
        if (type == FORMAL_PARAMETER) {
            Tab.insert(Obj.Var, name, new Struct(currentDeclarationType));
        }
        if (type == METHOD) {
            currMethod = Tab.insert(Obj.Meth, name, new Struct(currentDeclarationType));
            Tab.openScope();
        }
        if (type == METHOD_EXIT) {
            Tab.chainLocalSymbols(currMethod);
            Tab.closeScope();
        }
        if (type == CLASS) {
            currClassStruct = new Struct(Struct.Class);
            currClass = Tab.insert(Obj.Type, name, currClassStruct);
            Tab.openScope();
        }
        if (type == CLASS_EXIT) {
            Tab.chainLocalSymbols(currClassStruct);
            Tab.closeScope();
        }
        if (type == PROGRAM) {
            programObj = Tab.insert(Obj.Prog, name, Tab.noType);
            Tab.openScope();
        }
        if (type == PROGRAM_EXIT) {
            Tab.chainLocalSymbols(programObj);
            Tab.closeScope();
        }
        if (type == DESIGNATOR) {
            Obj node = Tab.find(name);
            if (node == Tab.noObj) {
                report_error("what now?");
            }
            else {

            }
        }
        if (type == TYPE) {
            Obj node = Tab.find(name);
            if (node == Tab.noObj) {
                report_error("Type not declared: " + name);
            } else if (node.getKind() != Obj.Type) {
                report_error("Token doesn't represent type: " + name);
            }
        }
        if (type == TYPE_CLASS) {
            Obj node = Tab.find(name);
            if (node == Tab.noObj) {
                report_error("Type not declared: " + name);
            } else if (node.getKind() != Obj.Type) {
                report_error("Token doesn't represent type: " + name);
            } else if (node.getType().getKind() != Struct.Class) {
                report_error("Token not of class type: " + name);
            }
        }
    }
    public void updateContext(SemanticSymbol type, String name) {
        if (type == METHOD) {
            report_info("Entered method: " + currMethodName);
            currMethodName = name;
            isCurrMethodStatic = false;
        }
        if (type == METHOD_EXIT) {
            report_info("Exited method: " + currMethodName);
            currMethodName = null;
            currMethod = null;
        }
        if (type == CLASS) {
            report_info("Entered clas: " + currClassName);
            currClassName = name;
        }
        if (type == CLASS_EXIT) {
            report_info("Exited class: " + currClassName);
            currClassName = null;
            currClass = null;
        }
        if (type == STATEMENT_BLOCK) {
            statementBlockLevel ++;
        }
        if (type == STATEMENT_BLOCK_EXIT) {
            statementBlockLevel--;
        }
        if (type == STATIC) {
            setCurrMethodStatic();
        }
        if (type == TYPE) {
            if (objectType.containsKey(name)) {
                currentDeclarationType = objectType.get(name);
            } else {
                currentDeclarationType = objectType.get("class");
            }
        }
    }


    public void updateCounters(SemanticSymbol type, String name) {
        if (type == CONST) {
            symbolByNameCounter.inc(getContext() + "const");
            if(currMethodName != null || currClassName != null) {
                report_error("error?");
            }
            symbolCounter.inc(CountType.GLOBAL_CONST);
        }
        if (type == VAR) {
            symbolByNameCounter.inc(getContext() +"var");
            if (currClassName != null && currMethodName != null) {
                // what here?
            } else if (currClassName != null) {
                symbolCounter.inc(CountType.CLASS_VAR);
            } else if (currMethodName != null) {
                // what here?
                if (currMethodName == "main") {
                    symbolCounter.inc(CountType.MAIN_VAR);
                }
            } else {
                symbolCounter.inc(CountType.GLOBAL_VAR);
            }
        }
        if (type == ARRAY) {
            if (currMethodName == null && currClassName == null) {
                symbolCounter.inc(CountType.GLOBAL_ARRAY);
            }
            symbolByNameCounter.inc(getContext() + "array");
        }
        if (type == METHOD) {
            if (currClassName != null) {
                if (isCurrMethodStatic) {
                    symbolCounter.inc(CountType.CLASS_STATIC_METHOD);
                } else {
                    symbolCounter.inc(CountType.CLASS_METHOD);
                }
            } else {
                symbolCounter.inc(CountType.GLOBAL_METHOD);
            }
            symbolByNameCounter.inc(getContext() + "method");
        }
        if (type == CLASS) {
            symbolCounter.inc(CountType.CLASS);
            symbolByNameCounter.inc(getContext() + "class");
        }
        if (type == FORMAL_PARAMETER) {
            symbolCounter.inc(CountType.FORMAL_ARGUMENT);
            symbolByNameCounter.inc(getContext() + "formal");
        }
        if (type == STATEMENT_BLOCK) {
            symbolCounter.inc(CountType.STATEMENT_BLOCK);
        }
        if (type == METHOD_CALL) {
            if (currMethodName == "main") {
                symbolCounter.inc(CountType.MAIN_METHOD_CALL);
            }
        }
    }



    public String getContext() {
        String res = "";
        for(int i = 0 ; i < statementBlockLevel; i ++) {
            res = "{} | " + res;
        }

        if (currMethodName != null) {
            res = currMethodName + " | " + res;
        }
        if (currClassName != null) {
            res = currClassName + " | " + res;
        }

        return res;
    }

    private void report_info(String msg) {
        logger.info(msg);
    }
    
    private void report_error(String msg) {
        logger.error(msg);
    }

}
