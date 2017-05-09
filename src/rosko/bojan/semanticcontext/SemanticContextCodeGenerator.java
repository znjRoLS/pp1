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
public class SemanticContextCodeGenerator {

    SemanticContext context;
    Logger logger;

    public SemanticContextCodeGenerator(SemanticContext ctx) {
        this.context = ctx;
        logger = LogManager.getLogger(SemanticContextCodeGenerator.class);
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

    public void generateCode(SemanticContext.SemanticSymbol type, SemanticParameters parameters) {

        switch(type) {

            case CONST: {
                break;
            }
            case CONST_VAL: {
                break;
            }
            case CONST_FACTOR: {
                Obj c = Tab.insert(Obj.Con, null, new Struct(context.objHelper.objectType.get(parameters.type)));
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
                if (context.currMethodName.equals("main")) {
                    Code.mainPc = Code.pc;
                }
                Code.put(Code.enter);
                Code.put(context.currMethod.getLevel());
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
                context.adrFalseJump = Code.pc - 2;
                break;
            }
            case IFEND: {
                Code.fixup(context.adrFalseJump);
                break;
            }
            case ELSESTART: {
                Code.putJump(0);
                context.adrTrueJump = Code.pc - 2;
                Code.fixup(context.adrFalseJump);
                break;
            }
            case ELSEEND: {
                Code.fixup(context.adrTrueJump);
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

            case SINGLE_EXPRESSION:
                if (Code.inc == parameters.value) {
                    Code.load(Tab.find(parameters.name));
                    Code.load(context.objHelper.constant1);
                    Code.put(Code.add);
                    Code.store(Tab.find(parameters.name));

//                    Code.put(Code.inc);
//                    Code.put(Tab.find(parameters.name).getAdr());
//                    Code.put(1);
                } else if (Code.inc + 1 == parameters.value) {
                    Code.load(Tab.find(parameters.name));
                    Code.load(context.objHelper.constant1);
                    Code.put(Code.sub);
                    Code.store(Tab.find(parameters.name));

//                    Code.put(Code.inc);
//                    Code.put(Tab.find(parameters.name).getAdr());
//                    Code.put(-1);
                } else {
                    Code.put(parameters.value);
                }
                break;

        }

    }
}
