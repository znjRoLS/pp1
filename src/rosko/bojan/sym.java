package rosko.bojan;

import sun.reflect.generics.scope.ConstructorScope;

/**
 * Created by rols on 4/21/17.
 */
public class sym {

    // keywords
    public static final int PROGRAM = 1;
    public static final int PRINT = 2;
    public static final int RETURN = 3;

    // operators
    public static final int PLUS = 6;
    public static final int EQUALS = 7;
    public static final int COMMA = 8;
    public static final int SEMICOLON = 9;
    public static final int PARENT_LEFT = 10;
    public static final int PARENT_RIGHT = 11;
    public static final int BRACES_LEFT = 12;
    public static final int BRACES_RIGHT = 13;

    // eof, idents, consts, void
    public static final int IDENT = 4;
    public static final int NUMBER = 5;
    public static final int EOF = 14;
    public static final int VOID = 15;
}
