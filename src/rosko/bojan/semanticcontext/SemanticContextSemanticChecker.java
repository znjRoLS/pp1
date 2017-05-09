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
    
    public void checkSemantics(SemanticContext.SemanticSymbol type, SemanticParameters parameters) {
        switch(type) {

            case CONST: {
                break;
            }
            case CONST_VAL: {
                if (context.currentDeclarationType != SemanticContext.objectType.get(parameters.type)) {
                    report_error("what now?");
                }
                if (context.lastConstDeclared == null) {
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
                report_info("Entered method: " + context.currMethodName);
                break;
            }
            case METHOD_EXIT: {
                report_info("Exited method: " + context.currMethodName);
                if (!context.returnFound && context.currMethod.getType() != Tab.noType) {
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
                if (context.returnFound) {
                    report_error("Method cannot have more than one return statement!");
                }
                if (!(parameters.expression.objType ==  context.currMethod.getType() ||
                        parameters.expression.objType.getKind() == context.currMethod.getType().getKind()) ) {
                    report_error("Method declaration and return expression are not of same type!");
                }
                break;
            }
            case STATIC: {
                break;
            }

            case CLASS: {
                report_info("Entered class: " + context.currClassName);
                break;
            }
            case CLASS_EXIT: {
                report_info("Exited class: " + context.currClassName);

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
                            parameters.expression +
                            " - " +
                            parameters.expression2);
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
                report_debug(parameters.expression.toString());
                if (parameters.expression.objType.getKind() != Struct.Int
                        && parameters.expression.objType.getKind() != Struct.Char) {
                    report_error("Print expression neither int nor char!");
                }
                break;
            }

            case EXPRESSION: {
                if (parameters.expression.objType.getKind() != SemanticContext.objectType.get(parameters.type)) {
                    report_error("Expression not of expected type! " + parameters.expression +
                            ", type expected: " + parameters.type);
                }
                if (parameters.expression2.objType.getKind() != SemanticContext.objectType.get(parameters.type)) {
                    report_error("Expression not of expected type! " + parameters.expression2 +
                            ", type expected: " + parameters.type);
                }
                break;
            }
        }
    }
}
