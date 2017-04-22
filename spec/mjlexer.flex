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
    return sym.EOF;
%eofval}

%%

" "     { }
"\b"    { }
"\t"    { }
"\r\n"  { }
"\f"    { }

"program"   {return symbol(sym.PROGRAM, "program");}
"print"     {return symbol(sym.PROGRAM, "print");}
"return"    {return symbol(sym.PROGRAM, "return");}
"void"      {return symbol(sym.PROGRAM, "void");}
"+"         {return symbol(sym.PROGRAM, "+");}
"="         {return symbol(sym.PROGRAM, "=");}
";"         {return symbol(sym.PROGRAM, ";");}
","         {return symbol(sym.PROGRAM, ",");}
"("         {return symbol(sym.PROGRAM, "(");}
")"         {return symbol(sym.PROGRAM, ")");}
"{"         {return symbol(sym.PROGRAM, "{");}
"}"         {return symbol(sym.PROGRAM, "}");}

"//"        { yybegin(COMMENT); }
<COMMENT> . { }
<COMMENT> "\r\n" {yybegin(YYINITIAL);}

[0-9]+ { return symbol(sym.NUMBER, new Integer(yytext()));}
([a-z]|[A-Z])[a-z|A-Z|0-9|_]* { return symbol(sym.IDENT, yytext());}

. { System.err.println("Error in lexical analysis, token: " + yytext() + ", line: " + (yyline+1));}