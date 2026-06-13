# GoLite — Intérprete (Fase 1)

El proyecto incluye un intérprete completo (análisis léxico, sintáctico y semántico, construcción y recorrido del AST) y un entorno de desarrollo (IDE) con editor, consola y reportes.

## Tecnologías

- **Java 21** — lógica del intérprete e interfaz gráfica.
- **JFLEX** — generación del analizador léxico.
- **CUP** — generación del analizador sintáctico.
- **Maven** — gestión de dependencias y construcción.
- **Swing + RSyntaxTextArea** — interfaz gráfica y editor de código.

## Requisitos

- **JDK 21** o superior.
- **Apache Maven**.
- (Opcional) **NetBeans 21 o VS Code** para abrir el proyecto directamente.

## Compilación y ejecución

### Con NetBeans

1. Abrir el proyecto (`File → Open Project`).
2. Ejecutar **Clean and Build** (importante para regenerar el lexer y el parser).
3. Ejecutar el proyecto con **Run**.

### Con Maven (línea de comandos)

```bash
mvn clean package
java -jar target/golite-1.0-jar-with-dependencies.jar
```

El comando `package` genera un `.jar` ejecutable con todas las dependencias incluidas dentro de la carpeta `target/`.

## Uso

1. Escribir o pegar código GoLite en el editor, o abrir un archivo `.glt`.
2. Presionar **Ejecutar**. El intérprete realiza los análisis léxico, sintáctico y semántico, y ejecuta el programa.
3. El resultado de la ejecución se muestra en la **consola** (parte inferior).
4. Desde el menú **Reportes** se puede abrir:
   - **Reporte de tokens:** todos los tokens reconocidos (lexema, tipo, línea, columna).
   - **Reporte de errores:** todos los errores encontrados (descripción, línea, columna, tipo).

### Ejemplo de código

```go
fmt.Println("Hola GoLite")

var contador int = 0
for i := 1; i <= 5; i++ {
    contador += i
}
fmt.Println("Suma:", contador)

if contador > 10 {
    fmt.Println("Mayor a 10")
} else {
    fmt.Println("Menor o igual a 10")
}
```

## Autor

- **Nombre:** Alexander Domingo
- **Carné:** 201946161
- Curso: Organización de Lenguajes y Compiladores 1
