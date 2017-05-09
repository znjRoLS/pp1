package rosko.bojan.semanticcontext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

/**
 * Created by rols on 5/9/17.
 */
public class SemanticContextUpdater {

    SemanticContext context;
    Logger logger;

    public SemanticContextUpdater(SemanticContext ctx) {
        this.context = ctx;
        logger = LogManager.getLogger(SemanticContextUpdater.class);
    }

    private void report_info(String msg) {
        logger.info(msg);
    }

    private void report_debug(String msg) {
        logger.debug(msg);
    }

    private void report_error(String msg) {
        logger.error(msg);
        context.errorDetected();
    }

    public Struct updateContext(SemanticContext.SemanticSymbol type, SemanticParameters parameters) {

        Struct result = null;

        switch(type) {

            case CONST: {
                context.lastConstDeclared = Tab.insert(Obj.Con, parameters.name, context.currentDeclarationType);
                result = context.currentDeclarationType;
                break;
            }
            case CONST_VAL: {
                context.lastConstDeclared.setAdr(parameters.value);
                context.lastConstDeclared = null;
                result = context.currentDeclarationType;
            }
            case CONST_FACTOR: {
                result = context.objHelper.objectStructs.get(parameters.type);
                break;
            }
            case VAR: {
                Tab.insert(Obj.Var, parameters.name, context.currentDeclarationType);
                result = context.currentDeclarationType;
                break;
            }
            case ARRAY: {
                report_error("Arrays are not processed properly");
                Struct arrType = new Struct(Struct.Array);
                arrType.setElementType(context.currentDeclarationType);
                Tab.insert(Obj.Var, parameters.name, arrType);
                result = arrType;
                break;
            }

            case PROGRAM: {
                context.programObj = Tab.insert(Obj.Prog, parameters.name, Tab.noType);
                Tab.openScope();
                break;
            }
            case PROGRAM_EXIT: {
                Code.dataSize = Tab.currentScope().getnVars();
                Tab.chainLocalSymbols(context.programObj);
                Tab.closeScope();
                break;
            }

            case METHOD: {
                context.currMethod = Tab.insert(Obj.Meth, parameters.name, context.currentDeclarationType);
                Tab.openScope();
                context.currMethodName = parameters.name;
                context.isCurrMethodStatic = false;
                result = context.currentDeclarationType;
                break;
            }
            case METHOD_START: {
                context.currMethod.setAdr(Code.pc);
                context.currMethod.setLevel(parameters.value);
                break;
            }
            case METHOD_EXIT: {
                context.returnFound = false;
                Tab.chainLocalSymbols(context.currMethod);
                Tab.closeScope();
                context.currMethodName = null;
                context.currMethod = null;
                break;
            }
            case METHOD_CALL: {
                break;
            }
            case METHOD_CALL_FACTOR: {
                break;
            }
            case FORMAL_PARAMETER: {
                Tab.insert(Obj.Var, parameters.name, context.objHelper.objectStructs.get(parameters.type));
                result = context.objHelper.objectStructs.get(parameters.type);
                break;
            }
            case FORMAL_PARAMETER_ARRAY: {
                report_error("Arrays are not processed properly");
                Tab.insert(Obj.Var, parameters.name, new Struct(Struct.Array, context.objHelper.objectStructs.get(parameters.type)));
                result = context.objHelper.objectStructs.get(parameters.type);
                break;
            }
            case RETURN: {
                context.returnFound = true;
                break;
            }
            case STATIC: {
                context.isCurrMethodStatic = true;
                break;
            }

            case CLASS: {
                context.currClassStruct = new Struct(Struct.Class);
                context.currClass = Tab.insert(Obj.Type, parameters.name, context.currClassStruct);
                Tab.openScope();
                context.currClassName = parameters.name;

                context.objHelper.objectStructs.put(parameters.name, context.currClassStruct);

                result = context.currClassStruct;
                break;
            }
            case CLASS_EXIT: {
                Tab.chainLocalSymbols(context.currClassStruct);
                Tab.closeScope();
                context.currClassName = null;
                context.currClass = null;
                break;
            }

            case TYPE: {
                context.currentDeclarationType = context.objHelper.objectStructs.get(parameters.name);
                result = context.currentDeclarationType;
                break;
            }
            case TYPE_CLASS: {
                break;
            }

            case STATEMENT_BLOCK: {
                context.statementBlockLevel ++;
                break;
            }
            case STATEMENT_BLOCK_EXIT: {
                context.statementBlockLevel--;
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
            case NEW: {
                result = Tab.find(parameters.name).getType();
                break;
            }

            case EXPRESSION: {
                result = parameters.expression.objType;
                break;
            }
        }

        return result;
    }
}
