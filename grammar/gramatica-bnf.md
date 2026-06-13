# Gramática de GoLite — Notación BNF

Gramática del lenguaje **GoLite** (Fase 1), implementada con JFLEX (análisis léxico) y CUP (análisis sintáctico).

## Convenciones

- `<no_terminal>` : símbolo no terminal.
- `"literal"` : símbolo terminal (token), mostrado con su lexema.
- `::=` : definición de una regla.
- `|` : separa alternativas de una misma regla.
- `ε` : producción vacía (la cadena vacía).
- `<identificador>`, `<entero>`, `<decimal>`, `<cadena>`, `<caracter>` : categorías léxicas, definidas en la sección **Definiciones léxicas**.

## Símbolo inicial

```
<programa>
```

## Gramática sintáctica

```
<programa> ::= <instrucciones>

<instrucciones> ::= <instrucciones> <instruccion>
                  | <instruccion>

<instruccion> ::= "fmt" "." "Println" "(" <argumentos> ")" <fin>
                | "var" <identificador> <tipo> "=" <expresion> <fin>
                | "var" <identificador> <tipo> <fin>
                | <identificador> ":=" <expresion> <fin>
                | <identificador> "=" <expresion> <fin>
                | <identificador> "+=" <expresion> <fin>
                | <identificador> "-=" <expresion> <fin>
                | <identificador> "++" <fin>
                | "break" <fin>
                | "continue" <fin>
                | <if>
                | <for>
                | <bloque>

<fin> ::= ";"
        | ε

<bloque> ::= "{" <instrucciones> "}"
           | "{" "}"

<if> ::= "if" <expresion> <bloque>
       | "if" <expresion> <bloque> "else" <bloque>
       | "if" <expresion> <bloque> "else" <if>

<for> ::= "for" <expresion> <bloque>
        | "for" <for_simple> ";" <expresion> ";" <for_simple> <bloque>

<for_simple> ::= <identificador> ":=" <expresion>
               | <identificador> "=" <expresion>
               | <identificador> "+=" <expresion>
               | <identificador> "-=" <expresion>
               | <identificador> "++"

<argumentos> ::= <lista_expresiones>
               | ε

<lista_expresiones> ::= <lista_expresiones> "," <expresion>
                      | <expresion>

<tipo> ::= "int"
         | "float64"
         | "string"
         | "bool"
         | "rune"

<expresion> ::= "-" <expresion>
              | "!" <expresion>
              | <expresion> "+" <expresion>
              | <expresion> "-" <expresion>
              | <expresion> "*" <expresion>
              | <expresion> "/" <expresion>
              | <expresion> "%" <expresion>
              | <expresion> "==" <expresion>
              | <expresion> "!=" <expresion>
              | <expresion> "<" <expresion>
              | <expresion> "<=" <expresion>
              | <expresion> ">" <expresion>
              | <expresion> ">=" <expresion>
              | <expresion> "&&" <expresion>
              | <expresion> "||" <expresion>
              | "(" <expresion> ")"
              | "strconv" "." "Atoi" "(" <expresion> ")"
              | "strconv" "." "ParseFloat" "(" <expresion> ")"
              | "reflect" "." "TypeOf" "(" <expresion> ")"
              | "reflect" "." "TypeOf" "(" <expresion> ")" "." "string"
              | "true"
              | "false"
              | <identificador>
              | <entero>
              | <decimal>
              | <cadena>
              | <caracter>
```

## Precedencia y asociatividad

La regla `<expresion>` es ambigua por sí sola; la desambiguación se realiza mediante la siguiente tabla de precedencia y asociatividad (declarada en CUP), de **menor a mayor** precedencia:

| Nivel | Operadores            | Asociatividad        |
|-------|-----------------------|----------------------|
| 1     | `\|\|`                | izquierda a derecha  |
| 2     | `&&`                  | izquierda a derecha  |
| 3     | `==`  `!=`            | izquierda a derecha  |
| 4     | `<`  `<=`  `>`  `>=`  | izquierda a derecha  |
| 5     | `+`  `-`              | izquierda a derecha  |
| 6     | `*`  `/`  `%`         | izquierda a derecha  |
| 7     | `!`  `-` (unario)     | derecha a izquierda  |

Los signos de agrupación `(` `)` tienen la mayor prioridad y se resuelven mediante la producción `"(" <expresion> ")"`.
