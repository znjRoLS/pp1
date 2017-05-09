package rosko.bojan;

import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.HashMap;

/**
 * Created by rols on 5/9/17.
 */
public class ObjHelper {

    public static String printObj(Obj object){
        return "name: " + object.getName() +
                ", type: " + printObjKind(object.getType().getKind()) +
                ", kind: " + printObjType(object.getKind());
    }

    public static String printObjType(int type) {
        // public static final int Con = 0, Var = 1, Type = 2, Meth = 3, Fld = 4, Elem=5, Prog = 6;
        final HashMap<Integer, String> typeMap = new HashMap<Integer, String>() {
            {
                put(0, "Con");
                put(1, "Var");
                put(2, "Type");
                put(3, "Meth");
                put(4, "Fld");
                put(5, "Elem");
                put(6, "Prog");
            }
        };

        return typeMap.get(type);
    }

    public static String printObjKind(int type) {
        // kodiranje tipova
        // public static final int None = 0;
        // public static final int Int = 1;
        // public static final int Char = 2;
        // public static final int Array = 3;
        // public static final int Class = 4;
        // public static final int Bool = 5;
        final HashMap<Integer, String> typeMap = new HashMap<Integer, String>() {
            {
                put(0, "None");
                put(1, "Int");
                put(2, "Char");
                put(3, "Array");
                put(4, "Class");
                put(5, "Bool");
            }
        };

        return typeMap.get(type);

    }
}
