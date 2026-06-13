package olc1.golite;

// Importaciones necesarias
import java.util.ArrayList;
import java.util.List;

import java_cup.runtime.Symbol;

import olc1.golite.reports.GoliteError;

%%

// Configuración de JFLEX
%cup //Indicamos que vamos a usar CUP
// Nombre de la clase del lexer
%class Lexer 
%public // Paquete del lexer
%line // conteo de lienas
%column // conteo de columnas
%8bit  // recibir caracteres en formato UTF-8
// %debug // Habilitar modo debug para ver el proceso de tokenización
//%unicode

%{
    // private Symbol symbol(int type) {
    //     return new Symbol(type, yyline, yycolumn);
    // }

    // private Symbol symbol(int type, Object value) {
    //     return new Symbol(type, yyline, yycolumn, value);
    // }

    public final List<GoliteError> errors = new ArrayList<>();
%}

%init{
    yyline = 1;
    yycolumn = 1;
%init}

%eofval{
    return new Symbol(sym.EOF, yyline, yycolumn, yytext());
%eofval}

// Definición de patrones léxicos
digit = [0-9]
letter = [a-zA-Z]
whitespace = [\ \r\t\f\n]+
escape_char = \\ [\"\\nrt]
normal_char = [^\"\\\n\r]
rune_normal = [^\'\\\n\r]
rune_escape = \\ [\'\\nrt]
str_lex = ({normal_char} | {escape_char})*

%%

// Numbers
{digit}+\.{digit}+  { return new Symbol(sym.decimal, yyline, yycolumn, yytext()); }
{digit}+            { return new Symbol(sym.integer, yyline, yycolumn, yytext()); }

// Symbols
"("     { return new Symbol(sym.lparen, yyline, yycolumn, yytext()); }
")"     { return new Symbol(sym.rparen, yyline, yycolumn, yytext()); }
"+"     { return new Symbol(sym.plus, yyline, yycolumn, yytext()); }
"-"     { return new Symbol(sym.minus, yyline, yycolumn, yytext()); }
"*"     { return new Symbol(sym.times, yyline, yycolumn, yytext()); }
"/"     { return new Symbol(sym.slash, yyline, yycolumn, yytext()); }
// "="     { return new Symbol(sym.allocate); }
";"     { return new Symbol(sym.scol, yyline, yycolumn, yytext()); }
"{"     { return new Symbol(sym.lbrace, yyline, yycolumn, yytext()); }
"}"     { return new Symbol(sym.rbrace, yyline, yycolumn, yytext()); }
"="     { return new Symbol(sym.assign, yyline, yycolumn, yytext()); }
":="    { return new Symbol(sym.dassign, yyline, yycolumn, yytext()); }

// Comparacion, logicos, modulo y asignacion compuesta
"=="    { return new Symbol(sym.equal,       yyline, yycolumn, yytext()); }
"!="    { return new Symbol(sym.nequal,      yyline, yycolumn, yytext()); }
"<="    { return new Symbol(sym.lesseq,      yyline, yycolumn, yytext()); }
">="    { return new Symbol(sym.greatereq,   yyline, yycolumn, yytext()); }
"<"     { return new Symbol(sym.less,        yyline, yycolumn, yytext()); }
">"     { return new Symbol(sym.greater,     yyline, yycolumn, yytext()); }
"&&"    { return new Symbol(sym.and,         yyline, yycolumn, yytext()); }
"||"    { return new Symbol(sym.or,          yyline, yycolumn, yytext()); }
"!"     { return new Symbol(sym.not,         yyline, yycolumn, yytext()); }
"%"     { return new Symbol(sym.percent,     yyline, yycolumn, yytext()); }
"+="    { return new Symbol(sym.plusassign,  yyline, yycolumn, yytext()); }
"-="    { return new Symbol(sym.minusassign, yyline, yycolumn, yytext()); }

// Key Words
"imprimir"  { return new Symbol(sym.imprimir, yyline, yycolumn, yytext()); }
"true"      { return new Symbol(sym.kwTrue,    yyline, yycolumn, yytext()); }
"false"     { return new Symbol(sym.kwFalse,   yyline, yycolumn, yytext()); }
"if"        { return new Symbol(sym.kwIf,      yyline, yycolumn, yytext()); }
"var"      { return new Symbol(sym.rVar,       yyline, yycolumn, yytext()); }
"int"      { return new Symbol(sym.tipoInt,    yyline, yycolumn, yytext()); }
"float64"  { return new Symbol(sym.tipoFloat,  yyline, yycolumn, yytext()); }
"string"   { return new Symbol(sym.tipoString, yyline, yycolumn, yytext()); }
"bool"     { return new Symbol(sym.tipoBool,   yyline, yycolumn, yytext()); }
"rune"     { return new Symbol(sym.tipoRune,   yyline, yycolumn, yytext()); }

// Literal rune (caracter entre comillas simples)
\'({rune_normal}|{rune_escape})\'  { return new Symbol(sym.runeLit, yyline, yycolumn, yytext()); }

// ID - String
({letter}|_)({letter}|{digit}|_)* { return new Symbol(sym.id, yyline, yycolumn, yytext()); }
\"{str_lex}\"               { return new Symbol(sym.string, yyline, yycolumn, yytext()); }

// Ignorar
{whitespace}    {/* pass */}

// Error
.   { errors.add(new GoliteError("Lexico", "Caracter no reconocido: " + yytext(), yyline, yycolumn)); }
