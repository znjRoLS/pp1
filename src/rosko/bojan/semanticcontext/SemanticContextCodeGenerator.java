package rosko.bojan.semanticcontext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rosko.bojan.Pair;
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

    private int loggerPad = 3;

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

    public Pair<Obj, Obj> getLastDesignators(String designator) {

        String paramName = designator;

        String firstPart = paramName.substring(0, paramName.indexOf("."));
        paramName = paramName.substring(paramName.indexOf(".") + 1);
        String secondPart = paramName.contains(".")?paramName.substring(0, paramName.indexOf(".")):paramName;

        Obj firstVar = Tab.find(firstPart), secondVar = firstVar.getType().getMembersTable().searchKey(secondPart);
        if (firstPart.equals("this")) {
            secondVar = Tab.currentScope().getOuter().getLocals().searchKey(secondPart);
        }
        Code.load(firstVar);

        while(paramName.contains(".")) {

            Code.put(Code.getfield);
            Code.put2(secondVar.getAdr());

            firstPart = paramName.substring(0, paramName.indexOf("."));
            paramName = paramName.substring(paramName.indexOf(".") + 1);
            secondPart = paramName.contains(".")?paramName.substring(0, paramName.indexOf(".")):paramName;

            firstVar = secondVar;
            secondVar = firstVar.getType().getMembersTable().searchKey(secondPart);
        }

        return new Pair<Obj, Obj>(firstVar, secondVar);
    }

    public Pair<Obj, Obj> getLastDesignatorsWithoutCode(String designator) {

        String paramName = designator;

        String firstPart = paramName.substring(0, paramName.indexOf("."));
        paramName = paramName.substring(paramName.indexOf(".") + 1);
        String secondPart = paramName.contains(".")?paramName.substring(0, paramName.indexOf(".")):paramName;

        Obj firstVar = Tab.find(firstPart), secondVar = firstVar.getType().getMembersTable().searchKey(secondPart);
        if (firstPart.equals("this")) {
            secondVar = Tab.currentScope().getOuter().getLocals().searchKey(secondPart);
        }

        while(paramName.contains(".")) {

            firstPart = paramName.substring(0, paramName.indexOf("."));
            paramName = paramName.substring(paramName.indexOf(".") + 1);
            secondPart = paramName.contains(".")?paramName.substring(0, paramName.indexOf(".")):paramName;

            firstVar = secondVar;
            secondVar = firstVar.getType().getMembersTable().searchKey(secondPart);
        }

        return new Pair<Obj, Obj>(firstVar, secondVar);
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
                if (!parameters.name.contains(".")) {
                    Obj function = Tab.find(parameters.name);
                    int functionAdr = function.getAdr() - Code.pc;
                    Code.put(Code.call);
                    Code.put2(functionAdr);
                } else {
                    Pair<Obj, Obj> vars = getLastDesignatorsWithoutCode(parameters.name);

                    int methodAdr = vars.getSecond().getAdr() - Code.pc;
                    Code.put(Code.call);
                    Code.put2(methodAdr);
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
                if (parameters.name.contains(".")) {
                    // this thing generates getfield code for getting val of second-to-last segment
                    // example: var1.var2.var3.var or var1.var2.var3.method() whould have val of reference of var3 on exprstack
                    // so, if its designator_assign, it should become putfield var,
                    //     if its designator_factor it should become getfield var
                    //     BUT if its method call, it should leave the THIS on expr stack(this is in fact val of var3)
                    Pair<Obj, Obj> vars = getLastDesignators(parameters.name);

                    // (another) BUT if its static, just pop the param
                    // TODO this actually generates a lot of unnecessary instructions (why call all the getfields, when we dont need THIS?)
                    if (vars.getSecond().getKind() == Obj.Meth && context.staticMethods.contains(vars.getSecond().getAdr())) {
                        Code.put(Code.pop);
                    }
                }
                break;
            }
            case DESIGNATOR_ASSIGN: {
                if (!parameters.name.contains(".")) {
                    Code.store(Tab.find(parameters.name));
                } else {
                    Pair<Obj, Obj> vars = getLastDesignatorsWithoutCode(parameters.name);

                    //Code.put(Code.dup_x1);
                    //Code.put(Code.pop);
                    Code.put(Code.putfield);
                    Code.put2(vars.getSecond().getAdr());
                }
                break;
            }
            case DESIGNATOR_FACTOR: {
                if (!parameters.name.contains(".")) {
                    Code.load(Tab.find(parameters.name));
                } else {
                    Pair<Obj, Obj> vars = getLastDesignatorsWithoutCode(parameters.name);

                    Code.put(Code.getfield);
                    Code.put2(vars.getSecond().getAdr());
                }
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

            case IF_START: {
                context.branchHelper.openNewIf();
                BranchHelper.IfStruct currentIf = context.branchHelper.getCurrentIf();

                Code.loadConst(0);
                Code.putFalseJump(Code.ne, 0);
                currentIf.adrFalseJump = Code.pc - 2;
                break;
            }
            case IF_END: {
                BranchHelper.IfStruct currentIf = context.branchHelper.getCurrentIf();

                Code.fixup(currentIf.adrFalseJump);

                context.branchHelper.closeCurrentIf();
                break;
            }
            case ELSE_START: {
                BranchHelper.IfStruct currentIf = context.branchHelper.getCurrentIf();

                Code.putJump(0);
                currentIf.adrTrueJump = Code.pc - 2;
                Code.fixup(currentIf.adrFalseJump);
                break;
            }
            case ELSE_END: {
                BranchHelper.IfStruct currentIf = context.branchHelper.getCurrentIf();

                Code.fixup(currentIf.adrTrueJump);

                context.branchHelper.closeCurrentIf();
                break;
            }

            case FOR_INIT: {
                context.branchHelper.openNewFor();
                BranchHelper.ForStruct currentFor = context.branchHelper.getCurrentFor();

                currentFor.forConditionAddress = Code.pc;
                break;
            }
            case FOR_CONDITION: {
                BranchHelper.ForStruct currentFor = context.branchHelper.getCurrentFor();

                Code.loadConst(0);
                Code.putFalseJump(Code.ne, 0);
                currentFor.forFalseConditionJump = Code.pc - 2;
                Code.putJump(0);
                currentFor.forTrueConditionJump = Code.pc - 2;
                currentFor.forIterationAddress = Code.pc;
                break;
            }
            case FOR_ITERATION: {
                BranchHelper.ForStruct currentFor = context.branchHelper.getCurrentFor();

                Code.putJump(currentFor.forConditionAddress);
                Code.fixup(currentFor.forTrueConditionJump);
                break;
            }
            case FOR_BLOCK: {
                BranchHelper.ForStruct currentFor = context.branchHelper.getCurrentFor();

                Code.putJump(currentFor.forIterationAddress);
                Code.fixup(currentFor.forFalseConditionJump);
                for (int breakStatement : currentFor.forBreakStatements) {
                    Code.fixup(breakStatement);
                }
                currentFor.forBreakStatements.clear();

                context.branchHelper.closeCurrentFor();
                break;
            }
            case BREAK : {
                BranchHelper.ForStruct currentFor = context.branchHelper.getCurrentFor();

                Code.putJump(0);
                currentFor.forBreakStatements.add(Code.pc-2);
                break;
            }
            case CONTINUE : {
                BranchHelper.ForStruct currentFor = context.branchHelper.getCurrentFor();

                Code.putJump(currentFor.forIterationAddress);
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
            case NEW: {
                Obj classType = Tab.find(parameters.name);
                Code.put(Code.new_);
                Code.put2(classType.getType().getNumberOfFields());

                break;
            }

            case EXPRESSION: {
                Code.put(parameters.value);
                break;
            }
            case SINGLE_EXPRESSION: {
                break;
            }

            case INCREMENT: {
                if (1 == parameters.value) {
                    Code.load(Tab.find(parameters.name));
                    Code.load(context.objHelper.constant1);
                    Code.put(Code.add);
                    Code.store(Tab.find(parameters.name));
                } else if (-1 == parameters.value) {
                    Code.load(Tab.find(parameters.name));
                    Code.load(context.objHelper.constant1);
                    Code.put(Code.sub);
                    Code.store(Tab.find(parameters.name));
                }
                break;
            }

        }

    }
}
