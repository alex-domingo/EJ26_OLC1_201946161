package olc1.golite;

// Importaciones necesarias
import java.util.ArrayList;
import java.util.List;

import java_cup.runtime.Symbol;

import olc1.golite.reports.GoliteError;
import olc1.golite.reports.Token;

%%

// Configuracion de JFLEX
%cup
%class Lexer
%public
%line
%column
%unicode

%{
    // errores lexicos encontrados
    public final List<GoliteError> errors = new ArrayList<>();

    // tokens reconocidos, para el reporte de tokens
    public final List<Token> tokens = new ArrayList<>();

    // helper: guarda el token en la lista y devuelve el Symbol que necesita CUP
    private Symbol tok(int type, String tipo) {
        tokens.add(new Token(tipo, yytext(), yyline, yycolumn));
        return new Symbol(type, yyline, yycolumn, yytext());
    }
%}

%init{
    yyline = 1;
    yycolumn = 1;
%init}

%eofval{
    return new Symbol(sym.EOF, yyline, yycolumn, yytext());
%eofval}

// Definicion de patrones lexicos
digit = [0-9]
letter = [a-zA-Z]
whitespace = [\ \r\t\f\n]+
escape_char = \\ [\"\\nrt]
normal_char = [^\"\\\n\r]
str_lex = ({normal_char} | {escape_char})*
rune_normal = [^\'\\\n\r]
rune_escape = \\ [\'\\nrt]

%%

// Comentarios (se ignoran)
"//"[^\n]*    { /* comentario de una linea */ }
"/*"~"*/"     { /* comentario multilinea */ }

// Numeros
{digit}+\.{digit}+  { return tok(sym.decimal, "decimal"); }
{digit}+            { return tok(sym.integer, "entero"); }

// Operadores de dos caracteres (por claridad van primero)
"=="    { return tok(sym.equal,       "igual"); }
"!="    { return tok(sym.nequal,      "diferente"); }
"<="    { return tok(sym.lesseq,      "menor_igual"); }
">="    { return tok(sym.greatereq,   "mayor_igual"); }
"&&"    { return tok(sym.and,         "and"); }
"||"    { return tok(sym.or,          "or"); }
"+="    { return tok(sym.plusassign,  "mas_igual"); }
"-="    { return tok(sym.minusassign, "menos_igual"); }
"++"    { return tok(sym.inc,         "incremento"); }
":="    { return tok(sym.dassign,     "declaracion"); }

// Operadores y simbolos de un caracter
"("     { return tok(sym.lparen,  "parentesis_izq"); }
")"     { return tok(sym.rparen,  "parentesis_der"); }
"{"     { return tok(sym.lbrace,  "llave_izq"); }
"}"     { return tok(sym.rbrace,  "llave_der"); }
"["     { return tok(sym.lbracket, "corchete_izq"); }
"]"     { return tok(sym.rbracket, "corchete_der"); }
"+"     { return tok(sym.plus,    "suma"); }
"-"     { return tok(sym.minus,   "resta"); }
"*"     { return tok(sym.times,   "multiplicacion"); }
"/"     { return tok(sym.slash,   "division"); }
"%"     { return tok(sym.percent, "modulo"); }
"<"     { return tok(sym.less,    "menor"); }
">"     { return tok(sym.greater, "mayor"); }
"!"     { return tok(sym.not,     "not"); }
"="     { return tok(sym.assign,  "asignacion"); }
";"     { return tok(sym.scol,    "punto_coma"); }
","     { return tok(sym.comma,   "coma"); }
"."     { return tok(sym.dot,     "punto"); }
":"     { return tok(sym.colon,   "dos_puntos"); }

// Palabras reservadas (antes de la regla de id)
"var"      { return tok(sym.rVar,       "reservada"); }
"int"      { return tok(sym.tipoInt,    "tipo"); }
"float64"  { return tok(sym.tipoFloat,  "tipo"); }
"string"   { return tok(sym.tipoString, "tipo"); }
"bool"     { return tok(sym.tipoBool,   "tipo"); }
"rune"     { return tok(sym.tipoRune,   "tipo"); }
"true"     { return tok(sym.kwTrue,     "reservada"); }
"false"    { return tok(sym.kwFalse,    "reservada"); }
"if"       { return tok(sym.kwIf,       "reservada"); }
"else"     { return tok(sym.kwElse,     "reservada"); }
"for"      { return tok(sym.kwFor,      "reservada"); }
"range"    { return tok(sym.kwRange,    "reservada"); }
"break"    { return tok(sym.kwBreak,    "reservada"); }
"continue" { return tok(sym.kwContinue, "reservada"); }
"switch"   { return tok(sym.kwSwitch,   "reservada"); }
"case"     { return tok(sym.kwCase,     "reservada"); }
"default"  { return tok(sym.kwDefault,  "reservada"); }
"func"     { return tok(sym.kwFunc,     "reservada"); }
"return"   { return tok(sym.kwReturn,   "reservada"); }

// ----- PALABRAS RESERVADAS (funciones globales) -----
"len"      { return tok(sym.kwLen,    "reservada"); }
"append"   { return tok(sym.kwAppend, "reservada"); }

// Funciones embebidas: paquete y metodo
"fmt"        { return tok(sym.pkgFmt,      "paquete"); }
"Println"    { return tok(sym.mPrintln,    "metodo"); }
"strconv"    { return tok(sym.pkgStrconv,  "paquete"); }
"Atoi"       { return tok(sym.mAtoi,       "metodo"); }
"ParseFloat" { return tok(sym.mParseFloat, "metodo"); }
"reflect"    { return tok(sym.pkgReflect,  "paquete"); }
"TypeOf"     { return tok(sym.mTypeOf,     "metodo"); }

// ----- PAQUETES Y MÉTODOS NUEVOS -----
"slices"   { return tok(sym.pkgSlices, "paquete"); }
"Index"    { return tok(sym.mIndex,    "metodo"); }
"strings"  { return tok(sym.pkgStrings, "paquete"); }
"Join"     { return tok(sym.mJoin,     "metodo"); }

// Identificadores
({letter}|_)({letter}|{digit}|_)* { return tok(sym.id, "id"); }

// Literal cadena
\"{str_lex}\"  { return tok(sym.string, "cadena"); }

// Literal rune
\'({rune_normal}|{rune_escape})\'  { return tok(sym.runeLit, "rune"); }

// Espacios en blanco (se ignoran)
{whitespace}    { /* pass */ }

// Error lexico
.   { errors.add(new GoliteError("Lexico", "Caracter no reconocido: " + yytext(), yyline, yycolumn)); }
