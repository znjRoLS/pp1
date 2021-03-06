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


    private Struct getNextDesignatorSection(Struct currentType, String currentSection){
        Struct result = null;

        if (currentSection.contains("[]")) {
            String varName = currentSection.substring(0, currentSection.indexOf('['));
            Obj foundObj = currentType.getMembersTable().searchKey(currentSection);

            result = foundObj.getType().getElemType();
        } else {
            Obj foundObj = currentType.getMembersTable().searchKey(currentSection);

            result = foundObj.getType();
        }

        return result;
    }



    private Struct extractLastDesignatorType(String paramName) {

        Struct varType;

        String varName;
        String firstPartVarName;

        boolean isField = paramName.contains(".");

        if (isField) {
            firstPartVarName = paramName.substring(0, paramName.indexOf('.'));
        } else {
            firstPartVarName = paramName;
        }

        boolean isArray = firstPartVarName.contains("[]");

        if (isArray) {
            varName = firstPartVarName.substring(0, firstPartVarName.indexOf('['));
        } else {
            varName = firstPartVarName;
        }

        Obj var = Tab.find(varName);

        if (isArray) {
            varType = var.getType().getElemType();
        } else {
            varType = var.getType();
        }

        if (isField) {

            String restOfName = paramName.substring(paramName.indexOf('.') + 1);

            while (restOfName.contains(".")) {
                String firstPart = restOfName.substring(0, restOfName.indexOf('.'));
                restOfName = restOfName.substring(restOfName.indexOf('.') + 1);

                varType = getNextDesignatorSection(varType, firstPart);
            }

            varType = getNextDesignatorSection(varType, restOfName);
        }

        return varType;
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
                Struct arrType = new Struct(Struct.Array);
                arrType.setElementType(context.currentDeclarationType);
                if (context.currClass != null && context.currMethod == null) {
                    // these are fields
                    Tab.insert(Obj.Fld, parameters.name, arrType);
                } else {
                    Tab.insert(Obj.Var, parameters.name, arrType);
                }
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

                if (context.currClass != null && !context.isCurrMethodStatic) {
                    //add THIS formal param
                    Tab.insert(Obj.Var, "this", context.currClassStruct);
                }

                result = context.currentDeclarationType;
                break;
            }
            case METHOD_START: {
                context.currMethod.setAdr(Code.pc);
                if (context.currClass != null && !context.isCurrMethodStatic) {
                    parameters.value ++;
                }
                context.currMethod.setLevel(parameters.value);
                break;
            }
            case METHOD_EXIT: {
                context.returnFound = false;
                Tab.chainLocalSymbols(context.currMethod);
                Tab.closeScope();

                if (context.isCurrMethodStatic) {
                    context.staticMethods.add(context.currMethod.getAdr());
                    context.isCurrMethodStatic = false;
                }

                context.currMethodName = null;
                context.currMethod = null;
                break;
            }
            case METHOD_CALL: {
                DesignatorHelper currentDesignator = context.currentDesignators.peek();
                result = currentDesignator.currentStruct;
                break;
            }
            case METHOD_CALL_FACTOR: {
                DesignatorHelper currentDesignator = context.currentDesignators.peek();
                result = currentDesignator.currentStruct;
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
                    Tab.currentScope().addToLocals(member);
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
                report_error("shouldnt be here, right?");
                break;
            }
            case DESIGNATOR_ASSIGN: {
                DesignatorHelper currentDesignator = context.currentDesignators.peek();
                String res = currentDesignator.designatorAssign(parameters.expression);
                if (res != null) {
                    report_error(res);
                }

                result = currentDesignator.currentStruct;

                context.currentDesignators.pop();
                break;
            }
            case DESIGNATOR_FACTOR: {
                DesignatorHelper currentDesignator = context.currentDesignators.peek();
                String res = currentDesignator.designatorFactor();
                if (res != null) {
                    report_error(res);
                }

                result = currentDesignator.currentStruct;

                context.currentDesignators.pop();
                break;
            }
            case DESIGNATOR_FIRSTPART: {
                context.currentDesignators.push(new DesignatorHelper(context));
                DesignatorHelper currentDesignator = context.currentDesignators.peek();
                String res = currentDesignator.setFirstPart(parameters.name);
                if (res != null) {
                    report_error(res);
                }

                break;
            }
            case DESIGNATOR_MEMBER_ARRAY: {
                DesignatorHelper currentDesignator = context.currentDesignators.peek();
                String res = currentDesignator.memberArray();
                if (res != null) {
                    report_error(res);
                }
                break;
            }
            case DESIGNATOR_MEMBER_CLASS: {
                DesignatorHelper currentDesignator = context.currentDesignators.peek();
                String res = currentDesignator.memberClass(parameters.name);
                if (res != null) {
                    report_error(res);
                }
                break;
            }
            case METHOD_CALL_START: {
                DesignatorHelper currentDesignator = context.currentDesignators.peek();
                String res = currentDesignator.methodCall();
                if (res != null) {
                    report_error(res);
                }
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
            case READ: {

                Code.put(Code.read);

                DesignatorHelper currentDesignator = context.currentDesignators.peek();
                String res = currentDesignator.designatorAssign(
                        new ExpressionToken(context.objHelper.objectStructs.get("int"), false)
                );
                if (res != null) {
                    report_error(res);
                }

                result = currentDesignator.currentStruct;

                context.currentDesignators.pop();
                break;
            }

            case NEW: {
                result = Tab.find(parameters.name).getType();
                break;
            }
            case NEW_ARRAY: {
                Obj arrayType = Tab.find(parameters.name);
                result = new Struct(Struct.Array, arrayType.getType());
                break;
            }

            case EXPRESSION: {
                result = parameters.expression.objType;
                break;
            }
            case INCREMENT: {
                String res = context.currentDesignators.peek().increment(parameters.value);
                if (res != null) {
                    report_error(res);
                }
            }
        }

        return result;
    }
}
