package rosko.bojan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

import static rosko.bojan.Parser.printExpr;
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
        CONST_VAL,
        CONST_FACTOR,
        VAR,
        ARRAY,
        METHOD,
        METHOD_START,
        METHOD_EXIT,
        CLASS,
        CLASS_EXIT,
        FORMAL_PARAMETER,
        FORMAL_PARAMETER_ARRAY,
        STATEMENT_BLOCK,
        STATEMENT_BLOCK_EXIT,
        METHOD_CALL,
        METHOD_CALL_FACTOR,
        STATIC,
        TYPE,
        PROGRAM,
        PROGRAM_EXIT,
        DESIGNATOR,
        DESIGNATOR_ASSIGN,
        DESIGNATOR_FACTOR,
        TYPE_CLASS,
        RETURN,
        RELOP,
        IFSTART,
        IFEND,
        ELSESTART,
        ELSEEND,
        PRINT,
        EXPRESSION,
        SINGLE_EXPRESSION,
        ERROR_RECOVERED
    }

    public enum ErrorType {
        GLOBAL_VAR,
        LOCAL_VAR,
        EXPRESSION_ASSIGN,
    }

    private HashMap<SemanticSymbol, String[]> symbolDeclarations =
            new HashMap<SemanticSymbol, String[]>() {
                {
                    put(CONST, new String[]{"name"}); // name of const
                    put(CONST_VAL, new String[]{"value", "type"}); // type of literal and its value
                    put(CONST_FACTOR, new String[]{"value", "type"}); // type of literal and its value
                    put(VAR, new String[]{"name"}); // var name
                    put(ARRAY, new String[]{"name"}); // array name
                    put(PROGRAM, new String[]{"name"}); // program name
                    put(PROGRAM_EXIT, new String[]{});
                    put(METHOD, new String[]{"name"}); // method name
                    put(METHOD_START, new String[]{"value"}); // value is number of parameters
                    put(METHOD_EXIT, new String[]{});
                    put(METHOD_CALL, new String[]{"name"}); // function that is called
                    put(METHOD_CALL_FACTOR, new String[]{"name"}); // function that is called
                    put(FORMAL_PARAMETER, new String[]{"type", "name"}); // type and name of parameter
                    put(FORMAL_PARAMETER_ARRAY, new String[]{"type", "name"}); // type and name of parameter
                    put(RETURN, new String[]{"expression"}); // return expression
                    put(STATIC, new String[]{});
                    put(CLASS, new String[]{"name"}); // class name
                    put(CLASS_EXIT, new String[]{});
                    put(TYPE, new String[]{"name"}); // type name
                    put(TYPE_CLASS, new String[]{"name"}); // type name
                    put(STATEMENT_BLOCK, new String[]{});
                    put(STATEMENT_BLOCK_EXIT, new String[]{});
                    put(DESIGNATOR, new String[]{"name"}); // designator name
                    put(DESIGNATOR_ASSIGN, new String[]{"name", "expression"}); // designator name and expression assigned
                    put(DESIGNATOR_FACTOR, new String[]{"name"}); // designator name
                    put(RELOP, new String[]{"expression", "expression2", "value"}); // value is relop instruction, expressions to compare
                    put(IFSTART, new String[]{});
                    put(IFEND, new String[]{});
                    put(ELSESTART, new String[]{});
                    put(ELSEEND, new String[]{});
                    put(PRINT, new String[]{"expression"}); // expression that is printed
                    put(EXPRESSION, new String[]{"expression", "expression2", "type", "value"}); // expressions that are found and type it should be, value is operator code
                    put(SINGLE_EXPRESSION, new String[]{"expression", "type"}); // expression found, type it should be
                    put(ERROR_RECOVERED, new String[]{});
                }
            };

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
    private boolean returnFound;
    private Logger logger = LogManager.getLogger(SemanticContext.class);
    private Parser parser;
    private int adrFalseJump, adrTrueJump;
    public boolean errorDetected;
    private boolean errorState;

    SemanticContext(Parser parser) {
        currClassName = null;
        currMethodName = null;
        isCurrMethodStatic = false;
        statementBlockLevel = 0;
        symbolByNameCounter = new SymbolCounter<String>();
        symbolCounter = new SymbolCounter<CountType>();
        lastConstDeclared = null;
        currMethod = null;
        currClass = null;
        returnFound = false;
        this.parser = parser;
        errorDetected = false;
        errorState = false;
    }

    public void errorDetected() {
        errorDetected = true;
        errorState = true;
    }

    public void init() {
        Tab.init();

        Obj voidObj = Tab.insert(Obj.Type, "void", new Struct(Struct.None));
        Obj boolObj = Tab.insert(Obj.Type, "bool", new Struct(Struct.Bool));

        voidObj.setAdr(-1);
        boolObj.setAdr(-1);
        voidObj.setLevel(-1);
        boolObj.setLevel(-1);
    }

    public static class SemanticParameters{
        public ExpressionToken expression, expression2;
        public Integer value;
        public String type;
        public String name;

        public SemanticParameters(ExpressionToken expression, ExpressionToken expression2, Integer value, String type, String name) {
            this.expression = expression;
            this.expression2 = expression2;
            this.value = value;
            this.type = type;
            this.name = name;
        }

        public SemanticParameters() {
            this(null, null, null,null,null);
        }

        public SemanticParameters setExpression(ExpressionToken expression) {
            this.expression = expression;
            return this;
        }

        public SemanticParameters setExpression2(ExpressionToken expression2) {
            this.expression2 = expression2;
            return this;
        }

        public SemanticParameters setValue(Integer value) {
            this.value = value;
            return this;
        }

        public SemanticParameters setValue(ErrorType value) {
            return setValue(value.ordinal());
        }

        public SemanticParameters setType(String type) {
            this.type = type;
            return this;
        }

        public SemanticParameters setName(String name) {
            this.name = name;
            return this;
        }

        public String toString() {
            return "expression: " + printExpr(expression) + " | " +
                    "expression2: " + printExpr(expression2) + " | " +
                    "value: " + value + " | " +
                    "type: " + type + " | " +
                    "name: " + name;
        }
    }


    private void setCurrMethodStatic() {
        isCurrMethodStatic = true;
    }

    public void foundSymbol(SemanticSymbol type, SemanticParameters parameters) {
        report_debug("foundsymbol " + type + " - with params " + parameters);
        report_info("foundsymbol " + type);

        for (String parameter: symbolDeclarations.get(type)) {
            try {
                if (parameters.getClass().getField(parameter) == null) {
                    report_error("You must declare parameter " + parameter + " for symbol " + type);
                    return;
                }
            } catch (NoSuchFieldException e) {
                report_error("No such field in parameters: " + parameter);
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                report_error(sw.toString());
                return;
            }
        }

        processError(type, parameters);
        updateCounters(type, parameters);
        checkSemantics(type, parameters);
        updateContext(type, parameters);
        generateCode(type, parameters);

    }

    public void processError(SemanticSymbol type, SemanticParameters parameters) {
        if (type == ERROR_RECOVERED) {
            errorDetected = true;
            if (ErrorType.values()[parameters.value] == ErrorType.GLOBAL_VAR) {
                if (currMethod != null) {
                    parameters.value = ErrorType.LOCAL_VAR.ordinal();
                }
            }
            report_error("Successfully recovered from error: " + ErrorType.values()[parameters.value]);
        }
    }

    public void checkSemantics(SemanticSymbol type, SemanticParameters parameters) {
        switch(type) {

            case CONST: {
                break;
            }
            case CONST_VAL: {
                if (currentDeclarationType != objectType.get(parameters.type)) {
                    report_error("what now?");
                }
                if (lastConstDeclared == null) {
                    report_error("what now?");
                }
            }
            case CONST_FACTOR: {
                break;
            }
            case VAR: {
                break;
            }
            case ARRAY: {
                break;
            }

            case PROGRAM: {
                break;
            }
            case PROGRAM_EXIT: {
                break;
            }

            case METHOD: {
                report_info("Method found: " + parameters.name);
                break;
            }
            case METHOD_START: {
                report_info("Entered method: " + currMethodName);
                break;
            }
            case METHOD_EXIT: {
                report_info("Exited method: " + currMethodName);
                if (!returnFound && currMethod.getType() != Tab.noType) {
                    report_error("Non void method must have return statement!");
                }
                break;
            }
            case METHOD_CALL: {
                report_info("Function call: " + parameters.name);
                Obj function = Tab.find(parameters.name);
                if (function.getKind() != Obj.Meth) {
                    report_error("Not a function!");
                }
                if (function.getType() == Tab.noType) {
                    report_error("Function doesn't return value!");
                }
                break;
            }
            case METHOD_CALL_FACTOR: {
                report_info("Function expression call: " + parameters.name);
                Obj function = Tab.find(parameters.name);
                if (function.getKind() != Obj.Meth) {
                    report_error("Not a function!");
                }
                if (function.getType() == Tab.noType) {
                    report_error("Function doesn't return value!");
                }
                break;
            }
            case FORMAL_PARAMETER: {
                break;
            }
            case FORMAL_PARAMETER_ARRAY: {
                break;
            }
            case RETURN: {
                if (returnFound) {
                    report_error("Method cannot have more than one return statement!");
                }
                if (!(parameters.expression.objType ==  currMethod.getType() ||
                        parameters.expression.objType.getKind() == currMethod.getType().getKind()) ) {
                    report_error("Method declaration and return expression are not of same type!");
                }
                break;
            }
            case STATIC: {
                break;
            }

            case CLASS: {
                report_info("Entered class: " + currClassName);
                break;
            }
            case CLASS_EXIT: {
                report_info("Exited class: " + currClassName);

                break;
            }

            case TYPE: {
                Obj node = Tab.find(parameters.name);
                if (node == Tab.noObj) {
                    report_error("Type not declared: " + parameters.name);
                } else if (node.getKind() != Obj.Type) {
                    report_error("Token doesn't represent type: " + parameters.name);
                }
                break;
            }
            case TYPE_CLASS: {
                Obj node = Tab.find(parameters.name);
                if (node == Tab.noObj) {
                    report_error("Type not declared: " + parameters.name);
                } else if (node.getKind() != Obj.Type) {
                    report_error("Token doesn't represent type: " + parameters.name);
                } else if (node.getType().getKind() != Struct.Class) {
                    report_error("Token not of class type: " + parameters.name);
                }
                break;
            }

            case STATEMENT_BLOCK: {
                break;
            }
            case STATEMENT_BLOCK_EXIT: {
                break;
            }

            case DESIGNATOR: {
                report_info("Found designator: " + parameters.name);
                Obj node = Tab.find(parameters.name);
                if (node == Tab.noObj) {
                    report_error("Identifier name not declared: " + parameters.name);
                }
                if (node.getKind() == Obj.Type) {
                    report_error("Identifier name is a type: " + parameters.name);
                }
                if (node.getKind() == Obj.Meth) {
                    report_error("Identifier name is a method: " + parameters.name);
                }
                if (node.getKind() == Obj.Prog) {
                    report_error("Identifier name is program name: " + parameters.name);
                }
                break;
            }
            case DESIGNATOR_ASSIGN: {
                report_info("Found assign statement");
                Obj design = Tab.find(parameters.name);
                if (parameters.expression.objType.getKind() != design.getType().getKind()) {
                    //report_info("expr " + printExpr(expr) + "| design " + printObj(design));
                    //report_info(expr.objType.getKind() + " " + design.getType().getKind());
                    report_error("Not assignable!");
                }
                break;
            }
            case DESIGNATOR_FACTOR: {
                report_info("Found designator factor: " + parameters.name);
                Obj node = Tab.find(parameters.name);
                if (node == Tab.noObj) {
                    report_error("Identifier name not declared: " + parameters.name);
                }
                if (node.getKind() == Obj.Type) {
                    report_error("Identifier name is a type: " + parameters.name);
                }
                if (node.getKind() == Obj.Meth) {
                    report_error("Identifier name is a method: " + parameters.name);
                }
                if (node.getKind() == Obj.Prog) {
                    report_error("Identifier name is program name: " + parameters.name);
                }
                break;
            }

            case RELOP: {
                if (!parameters.expression.compatible(parameters.expression2)) {
                    report_error("Expressions not compatible! " +
                            printExpr(parameters.expression) +
                            " - " +
                            printExpr(parameters.expression2));
                }
                break;
            }

            case IFSTART: {
                break;
            }
            case IFEND: {
                break;
            }
            case ELSESTART: {
                break;
            }
            case ELSEEND: {
                break;
            }

            case PRINT: {
                report_debug(printExpr(parameters.expression));
                if (parameters.expression.objType.getKind() != Struct.Int
                        && parameters.expression.objType.getKind() != Struct.Char) {
                    report_error("Print expression neither int nor char!");
                }
                break;
            }

            case EXPRESSION: {
                if (parameters.expression.objType.getKind() != objectType.get(parameters.type)) {
                    report_error("Expression not of expected type! " + printExpr(parameters.expression) +
                    ", type expected: " + parameters.type);
                }
                if (parameters.expression2.objType.getKind() != objectType.get(parameters.type)) {
                    report_error("Expression not of expected type! " + printExpr(parameters.expression2) +
                            ", type expected: " + parameters.type);
                }
                break;
            }
        }
    }

    public void updateContext(SemanticSymbol type, SemanticParameters parameters) {

        switch(type) {

            case CONST: {
                lastConstDeclared = Tab.insert(Obj.Con, parameters.name, new Struct(currentDeclarationType));
                break;
            }
            case CONST_VAL: {
                lastConstDeclared.setAdr(parameters.value);
                lastConstDeclared = null;
            }
            case CONST_FACTOR: {
                break;
            }
            case VAR: {
                Tab.insert(Obj.Var, parameters.name, new Struct(currentDeclarationType));
                break;
            }
            case ARRAY: {
                Struct arrType = new Struct(Struct.Array);
                arrType.setElementType(new Struct(currentDeclarationType));
                Tab.insert(Obj.Var, parameters.name, arrType);
                break;
            }

            case PROGRAM: {
                programObj = Tab.insert(Obj.Prog, parameters.name, Tab.noType);
                Tab.openScope();
                break;
            }
            case PROGRAM_EXIT: {
                Code.dataSize = Tab.currentScope().getnVars();
                Tab.chainLocalSymbols(programObj);
                Tab.closeScope();
                break;
            }

            case METHOD: {
                currMethod = Tab.insert(Obj.Meth, parameters.name, new Struct(currentDeclarationType));
                Tab.openScope();
                currMethodName = parameters.name;
                isCurrMethodStatic = false;
                break;
            }
            case METHOD_START: {
                currMethod.setAdr(Code.pc);
                currMethod.setLevel(parameters.value);
                break;
            }
            case METHOD_EXIT: {
                returnFound = false;
                Tab.chainLocalSymbols(currMethod);
                Tab.closeScope();
                currMethodName = null;
                currMethod = null;
                break;
            }
            case METHOD_CALL: {
                break;
            }
            case METHOD_CALL_FACTOR: {
                break;
            }
            case FORMAL_PARAMETER: {
                Tab.insert(Obj.Var, parameters.name, new Struct(objectType.get(parameters.type)));
                break;
            }
            case FORMAL_PARAMETER_ARRAY: {
                Tab.insert(Obj.Var, parameters.name, new Struct(Struct.Array, new Struct(objectType.get(parameters.type))));
                break;
            }
            case RETURN: {
                returnFound = true;
                break;
            }
            case STATIC: {
                setCurrMethodStatic();
                break;
            }

            case CLASS: {
                currClassStruct = new Struct(Struct.Class);
                currClass = Tab.insert(Obj.Type, parameters.name, currClassStruct);
                Tab.openScope();
                currClassName = parameters.name;
                break;
            }
            case CLASS_EXIT: {
                Tab.chainLocalSymbols(currClassStruct);
                Tab.closeScope();
                currClassName = null;
                currClass = null;
                break;
            }

            case TYPE: {
                if (objectType.containsKey(parameters.name)) {
                    currentDeclarationType = objectType.get(parameters.name);
                } else {
                    currentDeclarationType = objectType.get("class");
                }
                break;
            }
            case TYPE_CLASS: {
                break;
            }

            case STATEMENT_BLOCK: {
                statementBlockLevel ++;
                break;
            }
            case STATEMENT_BLOCK_EXIT: {
                statementBlockLevel--;
                break;
            }

            case DESIGNATOR: {
                break;
            }
            case DESIGNATOR_ASSIGN: {
                break;
            }
            case DESIGNATOR_FACTOR: {
                break;
            }

            case IFSTART: {
                break;
            }
            case IFEND: {
                break;
            }
            case ELSESTART: {
                break;
            }
            case ELSEEND: {
                break;
            }
            case PRINT: {
                break;
            }

            case EXPRESSION: {
                break;
            }
        }
    }

    public void generateCode(SemanticSymbol type, SemanticParameters parameters) {

        switch(type) {

            case CONST: {
                break;
            }
            case CONST_VAL: {
                break;
            }
            case CONST_FACTOR: {
                Obj c = Tab.insert(Obj.Con, null, new Struct(objectType.get(parameters.type)));
                c.setAdr(parameters.value);
                Code.load(c);
            }
            case VAR: {
                break;
            }
            case ARRAY: {
                break;
            }

            case PROGRAM: {
                break;
            }
            case PROGRAM_EXIT: {
                break;
            }

            case METHOD: {
                break;
            }
            case METHOD_START: {
                if (currMethodName.equals("main")) {
                    Code.mainPc = Code.pc;
                }
                Code.put(Code.enter);
                Code.put(currMethod.getLevel());
                Code.put(Tab.currentScope().getnVars());
                break;
            }
            case METHOD_EXIT: {
                Code.put(Code.exit);
                Code.put(Code.return_);
                break;
            }
            case METHOD_CALL: {
                Obj function = Tab.find(parameters.name);
                int functionAdr = function.getAdr() - Code.pc;
                Code.put(Code.call);
                Code.put2(functionAdr);
                if (function.getType() != Tab.noType) {
                    Code.put(Code.pop);
                }
                break;
            }
            case METHOD_CALL_FACTOR: {
                Obj function = Tab.find(parameters.name);
                int functionAdr = function.getAdr() - Code.pc;
                Code.put(Code.call);
                Code.put2(functionAdr);
                break;
            }
            case FORMAL_PARAMETER: {
                break;
            }
            case FORMAL_PARAMETER_ARRAY: {
                break;
            }
            case RETURN: {
                break;
            }
            case STATIC: {
                break;
            }

            case CLASS: {
                break;
            }
            case CLASS_EXIT: {
                break;
            }

            case TYPE: {
                break;
            }
            case TYPE_CLASS: {
                break;
            }

            case STATEMENT_BLOCK: {
                break;
            }
            case STATEMENT_BLOCK_EXIT: {
                break;
            }

            case DESIGNATOR: {
                break;
            }
            case DESIGNATOR_ASSIGN: {
                Code.store(Tab.find(parameters.name));
                break;
            }
            case DESIGNATOR_FACTOR: {
                Obj varObj = Tab.find(parameters.name);
                Code.load(varObj);
                break;
            }

            case RELOP: {
                Code.putFalseJump(parameters.value,0);
                int adrFalseJump = Code.pc - 2;
                Code.loadConst(1);
                Code.putJump(0);
                int adrTrueJump = Code.pc - 2;
                Code.fixup(adrFalseJump);
                Code.loadConst(0);
                Code.fixup(adrTrueJump);
                break;
            }

            case IFSTART: {
                Code.loadConst(0);
                Code.putFalseJump(Code.ne, 0);
                adrFalseJump = Code.pc - 2;
                break;
            }
            case IFEND: {
                Code.fixup(adrFalseJump);
                break;
            }
            case ELSESTART: {
                Code.putJump(0);
                adrTrueJump = Code.pc - 2;
                Code.fixup(adrFalseJump);
                break;
            }
            case ELSEEND: {
                Code.fixup(adrTrueJump);
                break;
            }
            case PRINT: {
                if (parameters.expression.objType.getKind() == Struct.Int) {
                    Code.loadConst(5);
                    Code.put(Code.print);
                } else if (parameters.expression.objType.getKind() == Struct.Char) {
                    Code.loadConst(1);
                    Code.put(Code.bprint);
                }
                break;
            }

            case EXPRESSION: {
                Code.put(parameters.value);
                break;
            }
        }

    }

    public void updateCounters(SemanticSymbol type, SemanticParameters parameters) {
        if (type == CONST) {
            symbolByNameCounter.inc(getCounterContext() + "const");
            if(currMethodName != null || currClassName != null) {
                report_error("error?");
            }
            symbolCounter.inc(CountType.GLOBAL_CONST);
        }
        if (type == VAR) {
            symbolByNameCounter.inc(getCounterContext() +"var");
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
            symbolByNameCounter.inc(getCounterContext() + "array");
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
            symbolByNameCounter.inc(getCounterContext() + "method");
        }
        if (type == CLASS) {
            symbolCounter.inc(CountType.CLASS);
            symbolByNameCounter.inc(getCounterContext() + "class");
        }
        if (type == FORMAL_PARAMETER) {
            symbolCounter.inc(CountType.FORMAL_ARGUMENT);
            symbolByNameCounter.inc(getCounterContext() + "formal");
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

    public String getCounterContext() {
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


    public void dumpTable() {
        Tab.dump();
    }

    private void report_info(String msg) {
        logger.info(msg);
    }

    private void report_debug(String msg) {
        logger.debug(msg);
    }

    private void report_error(String msg) {
        logger.error(msg);
        errorDetected = true;
    }

}
