package rosko.bojan;

/**
 * Created by rols on 4/28/17.
 */
public class ExpressionToken {

    public ExpressionToken(int obj, boolean var) {
        objType = obj;
        isVar = var;
    }

//    public enum ValueType {
//        LVALUE, RVALUE
//    }

    //public ValueType varType;
    public boolean isVar;
    public int objType;
}
