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

    public boolean Successful = true;
%}

%xstate COMMENT

%eofval{
    return symbol(sym.EOF);
%eofval}

LineTerminator  = \r|\n|\r\n
WhiteSpace      = {LineTerminator} | [ \t\f\b]
Identifier      = [:jletter:] [:jletterdigit:]*

DecIntegerLiteral = 0 | [1-9][0-9]*

%%

{WhiteSpace} { }

"program"   {return symbol(sym.PROGRAM, "program");}
"break"   {return symbol(sym.BREAK, "break");}
"class"      {return symbol(sym.CLASS, "class");}
"else"   {return symbol(sym.ELSE, "else");}
"const"      {return symbol(sym.CONST, "const");}
"if"   {return symbol(sym.IF, "if");}
"new"   {return symbol(sym.NEW, "new");}
"print"   {return symbol(sym.PRINT, "print");}
"read"   {return symbol(sym.READ, "read");}
"return"    {return symbol(sym.RETURN, "return");}
"void"      {return symbol(sym.VOID, "void");}
"for"   {return symbol(sym.FOR, "for");}
"extends"   {return symbol(sym.EXTENDS, "extends");}
"continue"   {return symbol(sym.CONTINUE, "continue");}
"static"   {return symbol(sym.STATIC, "static");}


{DecIntegerLiteral}/[^[:jletter:]] { return symbol(sym.CONST_NUMBER, new Integer(yytext()));}
\'([a-z]|[A-Z])\' { return symbol(sym.CONST_CHAR, new Character(yytext().charAt(1)));}
(true|false) { return symbol(sym.CONST_BOOL, new Boolean(yytext()));}
{Identifier} { return symbol(sym.ID, yytext());}


"+"         {return symbol(sym.ADDITION, "+");}
"-"         {return symbol(sym.SUBTRACTION, "-");}
"*"         {return symbol(sym.MULTIPLICATION, "*");}
"/"         {return symbol(sym.DIVISION, "/");}
"%"         {return symbol(sym.MODULO, "%");}
"=="         {return symbol(sym.EQUAL, "==");}
"!="         {return symbol(sym.NOT_EQUAL, "!=");}
">"         {return symbol(sym.GREATER, ">");}
">="         {return symbol(sym.GREATER_EQUAL, ">=");}
"<"         {return symbol(sym.LESS, "<");}
"<="         {return symbol(sym.LESS_EQUAL, "<=");}
"&&"         {return symbol(sym.AND, "&&");}
"||"         {return symbol(sym.OR, "||");}
"="         {return symbol(sym.ASSIGN, "=");}
"+="         {return symbol(sym.ASSIGN_ADDITION, "+=");}
"-="         {return symbol(sym.ASSIGN_SUBTRACTION, "-=");}
"*="         {return symbol(sym.ASSIGN_MULTIPLICATION, "*=");}
"/="         {return symbol(sym.ASSIGN_DIVISION, "/=");}
"%="         {return symbol(sym.ASSIGN_MODULO, "%=");}
"++"         {return symbol(sym.INCREMENT, "++");}
"--"         {return symbol(sym.DECREMENT, "--");}
";"         {return symbol(sym.SEMICOLON, ";");}
","         {return symbol(sym.COMMA, ",");}
"."         {return symbol(sym.DOT, ".");}
"("         {return symbol(sym.PARENTHESES_LEFT, "(");}
")"         {return symbol(sym.PARENTHESES_RIGHT, ")");}
"["         {return symbol(sym.BRACKETS_LEFT, "[");}
"]"         {return symbol(sym.BRACKETS_RIGHT, "]");}
"{"         {return symbol(sym.BRACES_LEFT, "{");}
"}"         {return symbol(sym.BRACES_RIGHT, "}");}

"//"        { yybegin(COMMENT); }
<COMMENT> . { }
<COMMENT> {LineTerminator} {yybegin(YYINITIAL);}


. { Successful = false; System.out.println("Error in lexical analysis, token: " + yytext() + ", line: " + (yyline+1)); return symbol(sym.ERROR);}