package rosko.bojan.semanticcontext;

import com.sun.org.apache.xpath.internal.WhitespaceStrippingElementMatcher;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

/**
 * Created by rols on 5/10/17.
 */
public class DesignatorHelper {
    Obj firstDesignator;
    Struct currentStruct;
    String lastFieldName;
    Obj methodObject;

    boolean lastMemberExists;
    boolean lastMemberArrayNotClass;
    boolean thisVar;

    SemanticContext context;

    public DesignatorHelper(SemanticContext context) {
        this.context = context;
        lastMemberArrayNotClass = lastMemberExists = false;
        firstDesignator = null;
        currentStruct = null;
        thisVar = false;
        methodObject = null;
    }

    String setFirstPart(String name) {
        firstDesignator = Tab.find(name);
        if (firstDesignator == Tab.noObj) {
            return "Identifier name not declared: " + name;
        }
        if (firstDesignator.getKind() == Obj.Type) {
            return "Identifier name is a type: " + name;
        }
        if (firstDesignator.getKind() == Obj.Prog) {
            return "Identifier name is program name: " + name;
        }
        lastFieldName = name;
        if (lastFieldName.equals("this")) {
            thisVar = true;
        }
        currentStruct = firstDesignator.getType();
        return null;
    }

    void propagateMemberClass() {
        Obj field;
        if (thisVar) {
            thisVar = false;
            field = Tab.currentScope().getOuter().getLocals().searchKey(lastFieldName);
        } else {
            field = currentStruct.getMembersTable().searchKey(lastFieldName);
        }
        Code.put(Code.getfield);
        Code.put2(field.getAdr());
        currentStruct = field.getType();
    }

    void propagateMemberArray() {
        Code.put(Code.aload);
        currentStruct = currentStruct.getElemType();
    }

    String memberArray() {
        if (!lastMemberExists) {
            // firrstvar[]
            // just store [] for later
            if (firstDesignator == null) {
                return "There isn't a designator's first part!";
            }

            if (currentStruct.getKind() != Struct.Array) {
                return "Identifier not an array: " + lastFieldName;
            }

            // well need this in either way
            Code.load(firstDesignator);
        } else {
            if (lastMemberArrayNotClass) {
                return "No multidimensional arrays allowed!";
            } else {

                // var.field[]
                // execute var.field and store []
                // BUT WAIT, we already have an expression on a stack. oh mother of god, we must invert this
                // we have var address on stack and we have expression in [] on stack
                //Code.put(Code.dup_x1);
                //Code.put(Code.pop);

                propagateMemberClass();

                if (currentStruct.getKind() != Struct.Array) {
                    return "Identifier not an array: " + lastFieldName;
                }

                //yep, revert it once more, because of aload and astore instruction format
                //Code.put(Code.dup_x1);
                //Code.put(Code.pop);
            }
        }

        lastMemberExists = true;
        lastMemberArrayNotClass = true;

        return null;
    }

    String memberClass(String fieldName) {
        if (!lastMemberExists) {
            // firrstvar.var
            // just store .var for later
            if (firstDesignator == null) {
                return "There isn't a designator's first part!";
            }

            if (currentStruct.getKind() != Struct.Class) {
                return "Identifier not of class type: " + lastFieldName;
            }

            // well need this in either way
            Code.load(firstDesignator);
        }
        else {
            if (lastMemberArrayNotClass) {
                // var[].field
                // hmmm, propagate array and store .field for later
                // we have val, expr on expression stack, co just call aload


                propagateMemberArray();

                if (currentStruct.getKind() != Struct.Class) {
                    return "Identifier not of class type: " + lastFieldName;
                }

            } else {
                // var.field.field2
                // propagate var.field, store .field2 for later
                // there is val of var on expr stack

                propagateMemberClass();

                if (currentStruct.getKind() != Struct.Array) {
                    return "Identifier not an array: " + lastFieldName;
                }

            }
        }

        lastMemberExists = true;
        lastMemberArrayNotClass = false;
        lastFieldName = fieldName;

        return null;
    }

    String designatorAssign(ExpressionToken expressionToken) {

        if (!lastMemberExists){
            // just a simple designator, yay!

            if (!expressionToken.objType.equals(currentStruct)) {
                return "Not assignable!";
            }

            Code.store(firstDesignator);
        } else {
            if (lastMemberArrayNotClass) {
                // var[expr] = expressionToken;
                // and on exprstack we have varVal, expr, expressionToken - BINGO

                currentStruct = currentStruct.getElemType();

                if (!expressionToken.objType.equals(currentStruct)) {
                    return "Not assignable!";
                }

                Code.put(Code.astore);
            } else {
                // var.field = expressionToken;
                // on exprstack we have varVal, expressionToken - another BINGO

                Obj field;
                if (thisVar) {
                    thisVar = false;
                    field = Tab.currentScope().getOuter().getLocals().searchKey(lastFieldName);
                } else {
                    field = currentStruct.getMembersTable().searchKey(lastFieldName);
                }
                currentStruct = field.getType();

                if (!expressionToken.objType.equals(currentStruct)) {
                    return "Not assignable!";
                }

                Code.put(Code.putfield);
                Code.put2(field.getAdr());
            }
        }

        return null;
    }

    String designatorFactor() {

        if (!lastMemberExists) {
            // just a simple designator, load it to exprstack
            Code.load(firstDesignator);
        }

        if (lastMemberExists) {
            // if not, than its a simple designator, no propagation needed
            if (lastMemberArrayNotClass) {
                // sth[]
                // not an assign statement, so propagate!

                propagateMemberArray();
            } else {
                // sth.sth
                // not assign stmt, so propagate!

                propagateMemberClass();
            }
        }

        return null;
    }

    String methodCall() {
        if (!lastMemberExists) {
            methodObject = firstDesignator;
        } else {
            if (lastMemberArrayNotClass) {
                return "Array of functions not allowed: " + lastFieldName;
            } else {
                Obj field;
                if (thisVar) {
                    thisVar = false;
                    field = Tab.currentScope().getOuter().getLocals().searchKey(lastFieldName);
                } else {
                    field = currentStruct.getMembersTable().searchKey(lastFieldName);
                }

                if (field == null) {
                    return "Method " + lastFieldName + " not declared!";
                }

                methodObject = field;
                currentStruct = methodObject.getType();

                if (context.staticMethods.contains(methodObject.getAdr())) {
                    // this method is static, pop the value of this
                    // this doesnt catch global methods, but we dont have an loaded val on expressionstack for them
                    Code.put(Code.pop);
                }

            }
        }
        return null;
    }

    String increment(int val) {

        if (!lastMemberExists) {
            Obj node = firstDesignator;
            if (!node.getType().equals(context.objHelper.objectStructs.get("int"))) {
                return "Var not of int type: " + firstDesignator.getName();
            }

            Code.load(node);
            Code.load(context.objHelper.constant1);
            Code.put(1==val?Code.add:Code.sub);
            Code.store(node);
        } else {
            if (lastMemberArrayNotClass){
                // var[nesto] ++;
                // exprstack has varval, nestoval. should duplicate this
                // varval,nestoval
                // varval,nestoval,varval,nestoval
                // varval,nestoval,var[nesto]
                // varval,nestoval,var[nesto],1
                // varval,nestoval, newvar[nesto] <- and then store this

                if (!currentStruct.getElemType().equals(context.objHelper.objectStructs.get("int"))) {
                    return "Var not of int type: " + lastFieldName;
                }

                Code.put(Code.dup2);
                Code.put(Code.aload);
                Code.load(context.objHelper.constant1);
                Code.put(1==val?Code.add:Code.sub);
                Code.put(Code.astore);

            } else {
                //var.nesto++
                // exprstack has varval
                // varval,
                // varval, var.nesto
                //varval, var.nesto, 1
                //varval, newvar.nesto <- putfield

                Obj field;
                if (thisVar) {
                    thisVar = false;
                    field = Tab.currentScope().getOuter().getLocals().searchKey(lastFieldName);
                } else {
                    field = currentStruct.getMembersTable().searchKey(lastFieldName);
                }

                if (field == null) {
                    return "Field not declared " + lastFieldName;
                }
                if (field.getType().equals(context.objHelper.objectStructs.get("int"))) {
                    return "Var not of int type: " + lastFieldName;
                }

                Code.put(Code.getfield);
                Code.put2(field.getAdr());
                Code.load(context.objHelper.constant1);
                Code.put(1==val?Code.add:Code.sub);
                Code.put(Code.putfield);
                Code.put2(field.getAdr());
            }
        }

        return null;
    }


}
