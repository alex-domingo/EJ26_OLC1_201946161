package olc1.golite.views;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.io.BufferedReader;
import java.io.StringReader;

import olc1.golite.Lexer;
import olc1.golite.parser;
import olc1.golite.ast.ASTNode;
import olc1.golite.reports.GoliteError;
import olc1.golite.visitor.interpreter.InterpreterVisitor;

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
        menuBar.onNew(e -> editorPanel.setText("imprimir(5+5);\n"));
        menuBar.onExit(e -> System.exit(0));
        menuBar.onTokens(e -> {
            /* TODO: reporte de tokens */ });
        menuBar.onErrors(e -> {
            errors();
        });
        menuBar.onAbout(e -> JOptionPane.showMessageDialog(
                this,
                "GolLite\nVersión 1.0.0\nLaboratorio OLC1",
                "Acerca de",
                JOptionPane.INFORMATION_MESSAGE));
    }

    private void run() {
        cleanConsole();
        try {
            lexer = new Lexer(new BufferedReader(new StringReader(editorPanel.getText())));
            parser = new parser(lexer);

            ASTNode ast = (ASTNode) parser.parse().value;

            if (!lexer.errors.isEmpty() || !parser.errors.isEmpty()) {
                consoleTextArea.append("Se encontraron errores en el análisis. Revise el reporte de errores.\n\n");
                for (GoliteError error : lexer.errors) {
                    consoleTextArea.append(error.toString() + "\n");
                }
                for (GoliteError error : parser.errors) {
                    consoleTextArea.append(error.toString() + "\n");
                }
                return;
            }

            InterpreterVisitor interpreter = new InterpreterVisitor();
            interpreter.Visit(ast);
            consoleTextArea.append(interpreter.output);
        } catch (Exception e) {
            consoleTextArea.append("Error: " + e.getMessage() + "\n");
        }
        consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
        editorPanel.getTextArea().requestFocus();
    }

    private void errors() {
        cleanConsole();

        if (lexer == null || parser == null) {
            consoleTextArea.append("Aún no se han ejecutado nada.\n");
            return;
        }

        consoleTextArea.append("Errores léxicos:\n");

        for (GoliteError error : lexer.errors) {
            consoleTextArea.append(error.toString() + "\n");
        }

        consoleTextArea.append("\nErrores sintácticos:\n");

        for (GoliteError error : parser.errors) {
            consoleTextArea.append(error.toString() + "\n");
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
