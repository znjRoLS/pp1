package rosko.bojan.semanticcontext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;


/**
 * Created by rols on 5/9/17.
 */
public class SemanticContextSemanticChecker {

    SemanticContext context;
    Logger logger;
    
    public SemanticContextSemanticChecker(SemanticContext ctx) {
        this.context = ctx;
        logger = LogManager.getLogger(SemanticContextSemanticChecker.class);
    }

    private int loggerPad = 1;

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

    private Obj getLastObjDesignator(String designator) {
        Obj design;
        if (designator.contains(".")) {
            String paramName = designator;

            String firstSection = paramName.substring(0, paramName.indexOf('.'));
            Obj currentObj = Tab.find(firstSection);
            paramName = paramName.substring(paramName.indexOf('.') + 1);

            while(paramName.contains(".")) {
                firstSection = paramName.substring(0, paramName.indexOf('.'));
                paramName = paramName.substring(paramName.indexOf('.') + 1);
                currentObj = currentObj.getType().getMembersTable().searchKey(firstSection);
                if (currentObj.getName().equals("this")) {
                    currentObj = Tab.currentScope().getOuter().getLocals().searchKey(firstSection);
                }
            }

            design = currentObj.getType().getMembersTable().searchKey(paramName);
            if (currentObj.getName().equals("this")) {
                design = Tab.currentScope().getOuter().getLocals().searchKey(paramName);
            }

        } else {
            design = Tab.find(designator);
        }

        return design;
    }

    private Struct getNextDesignatorSection(Struct currentType, String currentSection){
        Struct result = null;

        if (currentSection.contains("[]")) {
            String varName = currentSection.substring(0, currentSection.indexOf('['));
            Obj foundObj = currentType.getMembersTable().searchKey(currentSection);
            if (foundObj == null) {
                report_error("Identifier name not declared: " + currentSection);
                return null;
            }
            if (foundObj.getType().getKind() != Struct.Array) {
                report_error("Identifier name not an array: " + currentSection);
                return null;
            }
            result = foundObj.getType().getElemType();
        } else {
            Obj foundObj = currentType.getMembersTable().searchKey(currentSection);
            if (foundObj == null) {
                report_error("Identifier name not declared: " + currentSection);
                return null;
            }
            result = foundObj.getType();
        }

        return result;
    }

    public void checkSemantics(SemanticContext.SemanticSymbol type, SemanticParameters parameters) {
        switch(type) {

            case CONST: {
                break;
            }
            case CONST_VAL: {
                if (context.currentDeclarationType != context.objHelper.objectStructs.get(parameters.type)) {
                    report_error("Constant value differs from constant declaration type: "
                            + context.currentDeclarationType + " - "
                            + context.objHelper.objectStructs.get(parameters.type));
                    break;
                }
                if (context.lastConstDeclared == null) {
                    report_info("This shouldn't happen, right?");
                    report_error("Syntax error!");
                    break;
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
                if (context.isCurrMethodStatic && context.currClass == null) {
                    report_error("Cannot declare static function (just static methods are allowed)");
                    break;
                }
                report_info("Method found: " + parameters.name);
                break;
            }
            case METHOD_START: {
                report_info("Entered method: " + context.currMethodName);
                break;
            }
            case METHOD_EXIT: {
                report_info("Exited method: " + context.currMethodName);
                if (!context.returnFound && context.currMethod.getType() != context.objHelper.objectStructs.get("void")) {
                    report_error("Non void method must have return statement!");
                    break;
                }
                break;
            }
            case METHOD_CALL: {
                DesignatorHelper currentDesignator = context.currentDesignators.peek();
                Obj function = currentDesignator.methodObject;
                report_info("Function call finished "  +function.getName());
                if (function.getKind() != Obj.Meth) {
                    report_error("Not a function!");
                    break;
                }
                break;
            }
            case METHOD_CALL_FACTOR: {
                report_info("Function expression call: " + parameters.name);
                DesignatorHelper currentDesignator = context.currentDesignators.peek();
                Obj function = currentDesignator.methodObject;
                if (function.getKind() != Obj.Meth) {
                    report_error("Not a function!");
                    break;
                }
                if (function.getType() == Tab.noType) {
                    report_error("Function doesn't return value!");
                    break;
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
                if (!(parameters.expression.objType.equals(context.currMethod.getType()) ||
                        parameters.expression.objType.equals(context.currMethod.getType())) ) {
                    report_error("Method declaration and return expression are not of same type!");
                    break;
                }
                break;
            }
            case STATIC: {
                break;
            }

            case CLASS: {
                report_info("Entered class: " + parameters.name);
                break;
            }
            case CLASS_EXIT: {
                report_info("Exited class: " + context.currClassName);
                break;
            }
            case EXTENDED: {
                report_info("Entered class extended: " + parameters.name + " - " + parameters.type);
                break;
            }

            case TYPE: {
                Obj node = Tab.find(parameters.name);
                if (node == Tab.noObj) {
                    report_error("Type not declared: " + parameters.name);
                    break;
                } else if (node.getKind() != Obj.Type) {
                    report_error("Token doesn't represent type: " + parameters.name);
                    break;
                }
                break;
            }
            case TYPE_CLASS: {
                Obj node = Tab.find(parameters.name);
                if (node == Tab.noObj) {
                    report_error("Type not declared: " + parameters.name);
                    break;
                }
                if (node.getKind() != Obj.Type) {
                    report_error("Token doesn't represent type: " + parameters.name);
                    break;
                }
                if (node.getType().getKind() != Struct.Class) {
                    report_error("Token not of class type: " + parameters.name);
                    break;
                }
                if (!context.objHelper.objectStructs.containsKey(parameters.name)) {
                    report_info("This should never happen, right ?");
                    report_error("Type not declared: " + parameters.name);
                    break;
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
                Struct varType;

                String varName;
                String firstPartVarName;

                boolean isField = parameters.name.contains(".");

                if (isField) {
                    firstPartVarName = parameters.name.substring(0, parameters.name.indexOf('.'));
                } else {
                    firstPartVarName = parameters.name;
                }

                boolean isArray = firstPartVarName.contains("[]");

                if (isArray) {
                    varName = firstPartVarName.substring(0, firstPartVarName.indexOf('['));
                } else {
                    varName = firstPartVarName;
                }

                Obj var = Tab.find(varName);
                if (var == Tab.noObj) {
                    report_error("Identifier name not declared: " + firstPartVarName);
                    return;
                }
                if (var.getKind() == Obj.Type) {
                    report_error("Identifier name is a type: " + firstPartVarName);
                    return;
                }
                if (var.getKind() == Obj.Prog) {
                    report_error("Identifier name is program name: " + firstPartVarName);
                    return;
                }

                if (isArray) {
                    if (var.getType().getKind() != Struct.Array) {
                        report_error("Identifier not an array type: " + firstPartVarName);
                        return;
                    }
                    varType = var.getType().getElemType();
                } else {
                    varType = var.getType();
                }

                if (isField) {

                    if (varType.getKind() != Struct.Class) {
                        report_error("Identifier not of class type: " + firstPartVarName);
                        return;
                    }

                    String restOfName = parameters.name.substring(parameters.name.indexOf('.')+1);

                    while(restOfName.contains(".")) {
                        String firstPart = restOfName.substring(0,restOfName.indexOf('.'));
                        restOfName = restOfName.substring(restOfName.indexOf('.') + 1);

                        varType = getNextDesignatorSection(varType,firstPart);

                        if(varType == null) {
                            //this sum eraaa
                            return;
                        }

                        if (varType.getKind() != Struct.Class) {
                            report_error("Identifier not of class type: " + firstPartVarName);
                            return;
                        }
                    }

                    varType = getNextDesignatorSection(varType, restOfName);
                }

                break;
            }


            case RELOP: {
                if (!parameters.expression.compatible(parameters.expression2)) {
                    report_error("Expressions not compatible! " +
                            parameters.expression +
                            " - " +
                            parameters.expression2);
                    break;
                }
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
                if (!context.branchHelper.inFor()) {
                    report_error("Cannot use break statement if not in for loop!");
                    break;
                }
                break;
            }
            case CONTINUE : {
                if (!context.branchHelper.inFor()) {
                    report_error("Cannot use continue statement if not in for loop!");
                    break;
                }
                break;
            }

            case PRINT: {
                report_debug(parameters.expression.toString());
                if (!parameters.expression.objType.equals(context.objHelper.objectStructs.get("int"))
                        && !parameters.expression.objType.equals(context.objHelper.objectStructs.get("char"))) {
                    report_error("Print expression neither int nor char!");
                    break;
                }
                break;
            }
            case NEW: {
                Obj objType = Tab.find(parameters.name);
                if (objType == Tab.noObj) {
                    report_error("Symbol not found! " + parameters.name);
                    report_info("This should never happen, right?");
                    break;
                }
                if (objType.getKind() != Obj.Type) {
                    report_error("Symbol not a type! " + parameters.name);
                    report_info("This should never happen, right?");
                    break;
                }
                if (objType.getType().getKind() != Struct.Class) {
                    report_error("Type not a class type! " + parameters.name);
                    break;
                }

                break;
            }
            case NEW_ARRAY: {

                break;
            }

            case EXPRESSION: {
                if (!parameters.expression.objType.equals(context.objHelper.objectStructs.get(parameters.type))) {
                    report_error("Expression not of expected type! " + parameters.expression +
                            ", type expected: " + parameters.type);
                    break;
                }
                else if (!parameters.expression2.objType.equals(context.objHelper.objectStructs.get(parameters.type))) {
                    report_error("Expression not of expected type! " + parameters.expression2 +
                            ", type expected: " + parameters.type);
                    break;
                }
                break;
            }

            case SINGLE_EXPRESSION: {
                break;
            }

            case INCREMENT: {

                break;
            }
            case NEGATE: {
                if (!parameters.expression.objType.equals(context.objHelper.objectStructs.get("int"))) {
                    report_error("Expression not of expected type! " + parameters.expression +
                            ", type expected: int");
                    break;
                }
                break;
            }

        }
    }
}
