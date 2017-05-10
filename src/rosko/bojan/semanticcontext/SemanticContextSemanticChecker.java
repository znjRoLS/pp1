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
                if (!context.returnFound && context.currMethod.getType() != Tab.noType) {
                    report_error("Non void method must have return statement!");
                    break;
                }
                break;
            }
            case METHOD_CALL: {
                report_info("Function call: " + parameters.name);
                Obj function = Tab.find(parameters.name);
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
            case METHOD_CALL_FACTOR: {
                report_info("Function expression call: " + parameters.name);
                Obj function = getLastObjDesignator(parameters.name);
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
                if (context.returnFound) {
                    report_error("Method cannot have more than one return statement!");
                    break;
                }
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
                Obj design;

                if (parameters.name.contains(".")) {
                    String paramName = parameters.name;
                    String firstPart = paramName.substring(0,paramName.indexOf("."));
                    paramName = paramName.substring(paramName.indexOf(".") + 1);
                    Obj currentObject = Tab.find(firstPart);

                    while(paramName.contains(".")) {
                        firstPart = paramName.substring(0,paramName.indexOf("."));
                        paramName = paramName.substring(paramName.indexOf(".") + 1);

                        if (currentObject.getType().getKind() != Struct.Class) {
                            report_error("Var " + firstPart + " is not of class type!");
                            break;
                        }
                        currentObject = currentObject.getType().getMembersTable().searchKey(firstPart);
                        if (currentObject.getName().equals("this")) {
                            //this is not chained yet!
                            currentObject = Tab.currentScope().getOuter().getLocals().searchKey(firstPart);
                        }
                        if (currentObject == Tab.noObj) {
                            report_error("Field " + firstPart + " not existant!");
                            break;
                        }
                    }

                    design = currentObject.getType().getMembersTable().searchKey(paramName);
                    if (firstPart.equals("this")) {
                        //this is not chained yet!
                        design = Tab.currentScope().getOuter().getLocals().searchKey(paramName);
                    }

                } else {
                    design = Tab.find(parameters.name);
                }


                if (design == Tab.noObj || design == null) {
                    report_error("Identifier name not declared: " + parameters.name);
                    break;
                }
                if (design.getKind() == Obj.Type) {
                    report_error("Identifier name is a type: " + parameters.name);
                    break;
                }
                if (design.getKind() == Obj.Prog) {
                    report_error("Identifier name is program name: " + parameters.name);
                    break;
                }
                break;
            }
            case DESIGNATOR_ASSIGN: {
                report_info("Found designator assign");
                Obj design = getLastObjDesignator(parameters.name);

                if (design == null || !parameters.expression.objType.equals(design.getType())) {
                    report_error("Not assignable!");
                    break;
                }
                break;
            }
            case DESIGNATOR_FACTOR: {
                report_info("Found designator factor");

                Obj design = getLastObjDesignator(parameters.name);

                if (design.getKind() == Obj.Meth) {
                    report_error("Identifier name is a method: " + parameters.name);
                    break;
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
                    break;
                }
                if (objType.getKind() != Obj.Type) {
                    report_error("Symbol not a type! " + parameters.name);
                    break;
                }
                if (objType.getType().getKind() != Struct.Class) {
                    report_error("Type not a class type! " + parameters.name);
                    break;
                }

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
                Obj node = Tab.find(parameters.name);
                if (!node.getType().equals(context.objHelper.objectStructs.get("int"))) {
                    report_error("Var not of int type: " + parameters.name);
                    break;
                }
                break;
            }

        }
    }
}
