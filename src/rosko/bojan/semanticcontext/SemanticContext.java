package rosko.bojan.semanticcontext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rosko.bojan.ExpressionToken;
import rosko.bojan.Parser;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

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

    static HashMap<String,Integer> objectType = new HashMap<String,Integer>(){
        {
            put("int", Struct.Int);
            put("char", Struct.Char);
            put("bool", Struct.Bool);
            put("void", Struct.None);
            put("class", Struct.Class);
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
    int currentDeclarationType;
    boolean returnFound;
    private Logger logger = LogManager.getLogger(SemanticContext.class);
    int adrFalseJump, adrTrueJump;
    public boolean errorDetected;
    boolean errorState;

    private SemanticContextSymbolCounter symbolCounter;
    private SemanticContextSemanticChecker semanticChecker;
    private SemanticContextCodeGenerator codeGenerator;
    private SemanticContextUpdater contextUpdater;

    public SemanticContext() {
        symbolCounter = new SemanticContextSymbolCounter(this);
        semanticChecker = new SemanticContextSemanticChecker(this);
        contextUpdater = new SemanticContextUpdater(this);
        codeGenerator = new SemanticContextCodeGenerator(this);

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
        symbolCounter.updateCounters(type, parameters);
        semanticChecker.checkSemantics(type, parameters);
        contextUpdater.updateContext(type, parameters);
        codeGenerator.generateCode(type, parameters);

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

    public void printCounts() {
        symbolCounter.symbolByNameCounter.printAllCounts();
        symbolCounter.symbolCounter.printAllCounts();
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
