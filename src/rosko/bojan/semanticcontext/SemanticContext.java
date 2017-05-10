package rosko.bojan.semanticcontext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import static rosko.bojan.semanticcontext.SemanticContext.SemanticSymbol.*;

/**
 * Created by rols on 4/26/17.
 */
public class SemanticContext {

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
        EXTENDED,
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
        DESIGNATOR_FIRSTPART,
        DESIGNATOR_MEMBER_CLASS,
        DESIGNATOR_MEMBER_ARRAY,
        METHOD_CALL_START,
        TYPE_CLASS,
        RETURN,
        RELOP,
        IF_START,
        IF_END,
        ELSE_START,
        ELSE_END,
        FOR_INIT,
        FOR_CONDITION,
        FOR_ITERATION,
        FOR_BLOCK,
        BREAK,
        CONTINUE,
        PRINT,
        NEW,
        NEW_ARRAY,
        EXPRESSION,
        SINGLE_EXPRESSION,
        INCREMENT,
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
                    put(METHOD_CALL, new String[]{}); // function that is called
                    put(METHOD_CALL_FACTOR, new String[]{"name"}); // function that is called
                    put(FORMAL_PARAMETER, new String[]{"type", "name"}); // type and name of parameter
                    put(FORMAL_PARAMETER_ARRAY, new String[]{"type", "name"}); // type and name of parameter
                    put(RETURN, new String[]{"expression"}); // return expression
                    put(STATIC, new String[]{});
                    put(CLASS, new String[]{"name"}); // class name
                    put(CLASS_EXIT, new String[]{});
                    put(EXTENDED, new String[]{"type"}); // name of type(class) extending
                    put(TYPE, new String[]{"name"}); // type name
                    put(TYPE_CLASS, new String[]{"name"}); // type name
                    put(STATEMENT_BLOCK, new String[]{});
                    put(STATEMENT_BLOCK_EXIT, new String[]{});
                    put(DESIGNATOR, new String[]{"name"}); // designator name
                    put(DESIGNATOR_ASSIGN, new String[]{"name", "expression"}); // designator name and expression assigned
                    put(DESIGNATOR_FACTOR, new String[]{"name"}); // designator name
                    put(DESIGNATOR_FIRSTPART, new String[]{"name"}); // designator name
                    put(DESIGNATOR_MEMBER_CLASS, new String[]{"name"}); // designator name
                    put(DESIGNATOR_MEMBER_ARRAY, new String[]{});
                    put(METHOD_CALL_START, new String[]{});
                    put(RELOP, new String[]{"expression", "expression2", "value"}); // value is relop instruction, expressions to compare
                    put(IF_START, new String[]{});
                    put(IF_END, new String[]{});
                    put(ELSE_START, new String[]{});
                    put(ELSE_END, new String[]{});
                    put(FOR_INIT, new String[]{});
                    put(FOR_CONDITION, new String[]{});
                    put(FOR_ITERATION, new String[]{});
                    put(FOR_BLOCK, new String[]{});
                    put(BREAK, new String[]{});
                    put(CONTINUE, new String[]{});
                    put(PRINT, new String[]{"expression"}); // expression that is printed
                    put(NEW, new String[]{"name"}); // expression that is printed
                    put(NEW_ARRAY, new String[]{"name"}); // expression that is printed
                    put(EXPRESSION, new String[]{"expression", "expression2", "type", "value"}); // expressions that are found and type it should be, value is operator code
                    put(SINGLE_EXPRESSION, new String[]{"expression", "type"}); // expression found, type it should be
                    put(INCREMENT, new String[]{"name", "value"}); // name of designator, value to increment with
                    put(ERROR_RECOVERED, new String[]{});
                }
            };


    String currClassName;
    String currMethodName;
    Obj currMethod;
    Obj currClass;
    Obj programObj;
    Struct currClassStruct;
    Obj lastConstDeclared;
    int statementBlockLevel;
    boolean isCurrMethodStatic;
    Struct currentDeclarationType;
    boolean returnFound;
    private Logger logger = LogManager.getLogger(SemanticContext.class);

    public boolean errorDetected;
    boolean errorState;

    public int currentLine;


    private SemanticContextSymbolCounter symbolCounter;
    private SemanticContextSemanticChecker semanticChecker;
    private SemanticContextCodeGenerator codeGenerator;
    private SemanticContextUpdater contextUpdater;
    ObjHelper objHelper;
    BranchHelper branchHelper;
    Stack<DesignatorHelper> currentDesignators;
    HashSet<Integer> staticMethods;

    public SemanticContext() {
        branchHelper = new BranchHelper();
        objHelper = new ObjHelper();
        symbolCounter = new SemanticContextSymbolCounter(this);
        semanticChecker = new SemanticContextSemanticChecker(this);
        contextUpdater = new SemanticContextUpdater(this);
        codeGenerator = new SemanticContextCodeGenerator(this);
        currentDesignators = new Stack<>();
        staticMethods = new HashSet<>();

        currClassName = null;
        currMethodName = null;
        isCurrMethodStatic = false;
        statementBlockLevel = 0;

        lastConstDeclared = null;
        currMethod = null;
        currClass = null;
        returnFound = false;
        errorDetected = false;
        errorState = false;

        currentDeclarationType = null;

    }

    public void errorDetected() {
        errorDetected = true;
        errorState = true;
    }

    public void init() {
        Tab.init();

        objHelper.objectStructs.put("int", Tab.intType);
        objHelper.objectStructs.put("char", Tab.charType);
        objHelper.objectStructs.put("class", new Struct(Struct.Class));
        objHelper.objectStructs.put("void", Tab.noType);
        objHelper.objectStructs.put("bool", new Struct(Struct.Bool));

        Obj voidObj = Tab.insert(Obj.Type, "void", new Struct(Struct.None));
        Obj boolObj = Tab.insert(Obj.Type, "bool", new Struct(Struct.Bool));

        voidObj.setAdr(-1);
        boolObj.setAdr(-1);
        voidObj.setLevel(-1);
        boolObj.setLevel(-1);

        Tab.currentScope.addToLocals(voidObj);
        Tab.currentScope.addToLocals(boolObj);

        Obj c = Tab.insert(Obj.Con, null, objHelper.objectStructs.get("int"));
        c.setAdr(1);
        objHelper.constant1 = c;
    }

    public Struct foundSymbol(SemanticSymbol type, int line, SemanticParameters parameters) {
        currentLine = line + 1;

        report_debug("foundsymbol " + type + " - with params " + parameters);
        report_info("foundsymbol " + type);

        for (String parameter : symbolDeclarations.get(type)) {
            try {
                if (parameters == null) {
                    report_error("Parameters null, but " + parameter + " needed for symbol " + type);
                    return null;
                }
                if (parameters.getClass().getField(parameter).get(parameters) == null) {
                    report_error("You must declare parameter " + parameter + " for symbol " + type);
                    return null;
                }
                if (parameter.equals("expression")) {
                    if (parameters.expression.objType == null) {
                        report_error("Parameter expression doesn't have a type!");
                        return null;
                    }
                }
                if (parameter.equals("expression2")) {
                    if (parameters.expression2.objType == null) {
                        report_error("Parameter expression2 doesn't have a type!");
                        return null;
                    }
                }
            } catch (NoSuchFieldException e) {
                report_error("No such field in parameters: " + parameter);
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                report_error(sw.toString());
                return null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        Struct objectType = null;

        processError(type, parameters);
        symbolCounter.updateCounters(type, parameters);
        semanticChecker.checkSemantics(type, parameters);
        if (!errorDetected) {
            objectType = contextUpdater.updateContext(type, parameters);
        }
        else {
            report_info("Skipping updating context because of an error");
        }
        if (!errorDetected) {
            codeGenerator.generateCode(type, parameters);
        }
        else {
            report_info("Skipping code generation because of an error");
        }

        report_debug("returning for symbol " + type + " - " + objectType);

        return objectType;
    }

    public void processError(SemanticSymbol type, SemanticParameters parameters) {
        if (type == ERROR_RECOVERED) {
            errorDetected = true;
            if (ErrorType.values()[parameters.value] == ErrorType.GLOBAL_VAR) {
                if (currMethod != null) {
                    parameters.value = ErrorType.LOCAL_VAR.ordinal();
                }
            }
            report_error("Syntax error of type: " + ErrorType.values()[parameters.value]);
            report_info("Successfully recovered from error: " + ErrorType.values()[parameters.value]);
        }
    }

    public void printCounts() {
        logger.info("Symbol counters by name:\n" + symbolCounter.symbolByNameCounter.printAllCounts());
        logger.info("Symbol counters by type:\n" + symbolCounter.symbolCounter.printAllCounts());
    }

    public void dumpTable() {
        Tab.dump();
    }

    private int loggerPad = 16;

    private void report_info(String msg) {
        msg = "Line " + currentLine + ": " + msg;
        msg = String.format("%1$" + loggerPad + "s", "") + msg;
        logger.info(msg);
    }

    private void report_debug(String msg) {
        msg = "Line " + currentLine + ": " + msg;
        msg = String.format("%1$" + loggerPad + "s", "") + msg;
        logger.debug(msg);
    }

    private void report_error(String msg) {
        msg = "Line " + currentLine + ": " + msg;
        msg = String.format("%1$" + loggerPad + "s", "") + msg;
        logger.error(msg);
        errorDetected = true;
    }

}
