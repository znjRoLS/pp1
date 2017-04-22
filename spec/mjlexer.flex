package rosko.bojan;
import java_cup.runtime.Symbol;

%%

%class Lexer
%line
%column
%cup

%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

%xstate COMMENT

%eofval{
    return symbol(sym.EOF);
%eofval}

%%

" "     { }
"\b"    { }
"\t"    { }
"\r\n"  { }
"\n"    { }
"\f"    { }

"program"   {return symbol(sym.PROGRAM, "program");}
"print"     {return symbol(sym.PRINT, "print");}
"return"    {return symbol(sym.RETURN, "return");}
"void"      {return symbol(sym.VOID, "void");}
"+"         {return symbol(sym.PLUS, "+");}
"="         {return symbol(sym.EQUALS, "=");}
";"         {return symbol(sym.SEMICOLON, ";");}
","         {return symbol(sym.COMMA, ",");}
"("         {return symbol(sym.PARENT_LEFT, "(");}
")"         {return symbol(sym.PARENT_RIGHT, ")");}
"{"         {return symbol(sym.BRACES_LEFT, "{");}
"}"         {return symbol(sym.BRACES_RIGHT, "}");}

"//"        { yybegin(COMMENT); }
<COMMENT> . { }
<COMMENT> "\r\n" {yybegin(YYINITIAL);}
<COMMENT> "\n"  {yybegin(YYINITIAL);}

[0-9]+ { return symbol(sym.NUMBER, new Integer(yytext()));}
([a-z]|[A-Z])[a-z|A-Z|0-9|_]* { return symbol(sym.IDENT, yytext());}

. { System.err.println("Error in lexical analysis, token: " + yytext() + ", line: " + (yyline+1));}