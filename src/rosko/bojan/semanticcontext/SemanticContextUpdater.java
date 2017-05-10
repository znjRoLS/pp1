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

    private int loggerPad = 9;

    private void report_info(String msg) {
        msg = "Line " + context.currentLine + ": " + msg;
        msg = String.format("%1$" + loggerPad + "s", "") + msg;
        logger.info(msg);
    }

    private void report_debug(String msg) {
        msg = "Line " + context.currentLine + ": " + msg;
        msg = String.format("%1$" + loggerPad + "s", "") + msg;
        logger.debug(msg);
    }

    private void report_error(String msg) {
        msg = "Line " + context.currentLine + ": " + msg;
        msg = String.format("%1$" + loggerPad + "s", "") + msg;
        logger.error(msg);
        context.errorDetected();
    }

    private Struct extractLastDesignatorType(String paramName) {

        if (!paramName.contains(".")) {
            return Tab.find(paramName).getType();
        }

        String firstPart = paramName.substring(0,paramName.indexOf("."));
        paramName = paramName.substring(paramName.indexOf(".") + 1);
        Obj currentObject = Tab.find(firstPart);

        while(paramName.contains(".")) {
            firstPart = paramName.substring(0,paramName.indexOf("."));
            paramName = paramName.substring(paramName.indexOf(".") + 1);
            currentObject = currentObject.getType().getMembersTable().searchKey(firstPart);
        }

        currentObject = currentObject.getType().getMembersTable().searchKey(paramName);

        return currentObject.getType();
    }

    private Struct cloneStruct(Struct struct) {
        Struct cloned = new Struct(struct.getKind());

        if (cloned.getKind() == Struct.Array) {
            cloned.setElementType(struct.getElemType());
        }

        if (cloned.getKind() == Struct.Class) {
            cloned.setMembers(struct.getMembersTable());
        }

        return cloned;
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
                if (context.currClass != null && context.currMethod == null) {
                    // these are fields
                    Tab.insert(Obj.Fld, parameters.name, context.currentDeclarationType);
                } else {
                    Tab.insert(Obj.Var, parameters.name, context.currentDeclarationType);
                }
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
            case EXTENDED: {
                Obj parentClass = Tab.find(parameters.type);

                for(Obj member : parentClass.getType().getMembers()) {
                    Tab.insert(Obj.Fld, member.getName(), member.getType());
                }

                result = context.currClassStruct;
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
                result = extractLastDesignatorType(parameters.name);
                break;
            }
            case DESIGNATOR_ASSIGN: {
                result = extractLastDesignatorType(parameters.name);
                break;
            }
            case DESIGNATOR_FACTOR: {
                result = extractLastDesignatorType(parameters.name);
                break;
            }
            case RELOP: {
                result = context.objHelper.objectStructs.get("bool");
                break;
            }

            case IF_START: {
                break;
            }
            case IF_END: {
                break;
            }
            case ELSE_START: {
                break;
            }
            case ELSE_END: {
                break;
            }

            case FOR_INIT: {
                break;
            }
            case FOR_CONDITION: {
                break;
            }
            case FOR_ITERATION: {
                break;
            }
            case FOR_BLOCK: {
                break;
            }
            case BREAK : {
                break;
            }
            case CONTINUE : {
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
