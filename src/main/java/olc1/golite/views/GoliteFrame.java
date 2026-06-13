package olc1.golite.views;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import java_cup.runtime.Symbol;

import olc1.golite.Lexer;
import olc1.golite.parser;
import olc1.golite.sym;
import olc1.golite.ast.ASTNode;
import olc1.golite.reports.GoliteError;
import olc1.golite.reports.SemanticException;
import olc1.golite.reports.Token;
import olc1.golite.visitor.interpreter.InterpreterVisitor;
import olc1.golite.visitor.interpreter.control.BreakSignal;
import olc1.golite.visitor.interpreter.control.ContinueSignal;

public class GoliteFrame extends JFrame {

    private final EditorPanel editorPanel;
    private final JTextArea consoleTextArea;
    private Lexer lexer;
    private parser parser;

    public GoliteFrame() {
        setTitle("Golite");
        setMinimumSize(new Dimension(600, 400));
        setSize(new Dimension(1200, 675));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        editorPanel = new EditorPanel();
        consoleTextArea = new JTextArea();
        cleanConsole();

        GoliteMenuBar menuBar = new GoliteMenuBar();
        setJMenuBar(menuBar);
        add(new MainPanel(editorPanel, consoleTextArea));

        wireActions(menuBar);

        setVisible(true);
        editorPanel.getTextArea().requestFocus();
    }

    private void wireActions(GoliteMenuBar menuBar) {
        menuBar.onRun(e -> run());
        menuBar.onClean(e -> cleanConsole());
        menuBar.onNew(e -> editorPanel.setText("fmt.Println(\"Hola GoLite\");\n"));
        menuBar.onExit(e -> System.exit(0));
        menuBar.onTokens(e -> tokens());
        menuBar.onErrors(e -> errors());
        menuBar.onAbout(e -> JOptionPane.showMessageDialog(
                this,
                "GoLite\nVersión 1.0.0\nLaboratorio OLC1",
                "Acerca de",
                JOptionPane.INFORMATION_MESSAGE));
    }

    private void run() {
        cleanConsole();
        try {
            lexer = new Lexer(new BufferedReader(new StringReader(editorPanel.getText())));
            parser = new parser(lexer);

            ASTNode ast = (ASTNode) parser.parse().value;

            // si hubo errores lexicos o sintacticos no interpretamos
            if (!lexer.errors.isEmpty() || !parser.errors.isEmpty()) {
                consoleTextArea.append("Se encontraron errores en el análisis. Revise el reporte de errores.\n");
                return;
            }

            InterpreterVisitor interpreter = new InterpreterVisitor();
            interpreter.Visit(ast);
            consoleTextArea.append(interpreter.output);
        } catch (SemanticException se) {
            consoleTextArea.append(se.getError().toString() + "\n");
        } catch (BreakSignal bs) {
            consoleTextArea.append(new GoliteError("Semantico",
                    "La sentencia break solo puede usarse dentro de un ciclo",
                    bs.getLine(), bs.getColumn()).toString() + "\n");
        } catch (ContinueSignal cs) {
            consoleTextArea.append(new GoliteError("Semantico",
                    "La sentencia continue solo puede usarse dentro de un ciclo",
                    cs.getLine(), cs.getColumn()).toString() + "\n");
        } catch (Exception e) {
            consoleTextArea.append("Error: " + e.getMessage() + "\n");
        }
        consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
        editorPanel.getTextArea().requestFocus();
    }

    // recolecta todos los errores (lexicos, sintacticos y el primer semantico) para el reporte
    private List<GoliteError> collectErrors() {
        List<GoliteError> all = new ArrayList<>();
        try {
            Lexer lx = new Lexer(new BufferedReader(new StringReader(editorPanel.getText())));
            parser ps = new parser(lx);
            ASTNode ast = (ASTNode) ps.parse().value;
            all.addAll(lx.errors);
            all.addAll(ps.errors);

            if (lx.errors.isEmpty() && ps.errors.isEmpty() && ast != null) {
                try {
                    new InterpreterVisitor().Visit(ast);
                } catch (SemanticException se) {
                    all.add(se.getError());
                } catch (BreakSignal bs) {
                    all.add(new GoliteError("Semantico",
                            "La sentencia break solo puede usarse dentro de un ciclo",
                            bs.getLine(), bs.getColumn()));
                } catch (ContinueSignal cs) {
                    all.add(new GoliteError("Semantico",
                            "La sentencia continue solo puede usarse dentro de un ciclo",
                            cs.getLine(), cs.getColumn()));
                } catch (Exception e) {
                    // otros errores de ejecucion no se listan en el reporte
                }
            }
        } catch (Exception e) {
            // error inesperado durante el analisis
        }
        return all;
    }

    private void errors() {
        List<GoliteError> all = collectErrors();
        Object[][] datos = new Object[all.size()][5];
        for (int i = 0; i < all.size(); i++) {
            GoliteError e = all.get(i);
            datos[i][0] = i + 1;
            datos[i][1] = e.getDescription();
            datos[i][2] = e.getLine();
            datos[i][3] = e.getColumn();
            datos[i][4] = e.getType();
        }
        String[] columnas = {"No.", "Descripcion", "Linea", "Columna", "Tipo"};
        new ReportFrame("Reporte de Errores", columnas, datos);
    }

    private void tokens() {
        try {
            Lexer lx = new Lexer(new BufferedReader(new StringReader(editorPanel.getText())));
            // consumimos todos los tokens hasta el fin de archivo
            while (true) {
                Symbol s = lx.next_token();
                if (s == null || s.sym == sym.EOF) {
                    break;
                }
            }
            List<Token> toks = lx.tokens;
            Object[][] datos = new Object[toks.size()][5];
            for (int i = 0; i < toks.size(); i++) {
                Token t = toks.get(i);
                datos[i][0] = i + 1;
                datos[i][1] = t.getLexema();
                datos[i][2] = t.getTipo();
                datos[i][3] = t.getLinea();
                datos[i][4] = t.getColumna();
            }
            String[] columnas = {"No.", "Lexema", "Tipo", "Linea", "Columna"};
            new ReportFrame("Reporte de Tokens", columnas, datos);
        } catch (Exception e) {
            consoleTextArea.append("Error generando reporte de tokens: " + e.getMessage() + "\n");
        }
    }

    private void cleanConsole() {
        consoleTextArea.setText("CONSOLA  -  LABORATORIO DE ORGANIZACION DE LENGUAJES Y COMPILADORES 1\n\n");
    }

    public EditorPanel getEditorPanel() {
        return editorPanel;
    }

    public JTextArea getConsoleTextArea() {
        return consoleTextArea;
    }
}
