package rosko.bojan;


import rs.etf.pp1.symboltable.concepts.Struct;

/**
 * Created by rols on 4/28/17.
 */
public class ExpressionToken {

    public ExpressionToken(Struct obj, boolean var) {
        objType = obj;
        isVar = var;
    }

//    public enum ValueType {
//        LVALUE, RVALUE
//    }

    //public ValueType varType;
    public boolean isVar;
    public Struct objType;

    public boolean compatible(ExpressionToken other) {
        return other.objType.getKind() == objType.getKind();
    }
}
