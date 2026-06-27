package olc1.golite.views;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

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
import olc1.golite.visitor.interpreter.control.ReturnSignal;

public class GoliteFrame extends JFrame {

    private final EditorPanel editorPanel;
    private final JTextArea consoleTextArea;
    private Lexer lexer;
    private parser parser;
    // archivo actualmente abierto en el editor (null si es nuevo / sin guardar)
    private File currentFile = null;

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
        menuBar.onNew(e -> newFile());
        menuBar.onOpen(e -> openFile());
        menuBar.onSave(e -> saveFile());
        menuBar.onSaveAs(e -> saveFileAs());
        menuBar.onExit(e -> System.exit(0));
        menuBar.onRun(e -> run());
        menuBar.onClean(e -> cleanConsole());
        menuBar.onTokens(e -> tokens());
        menuBar.onErrors(e -> errors());
        menuBar.onAbout(e -> JOptionPane.showMessageDialog(
                this,
                "GoLite\nVersión 2.0.0\nLaboratorio OLC1",
                "Acerca de",
                JOptionPane.INFORMATION_MESSAGE));
    }

    private void run() {
        cleanConsole();
        try {
            lexer = new Lexer(new BufferedReader(new StringReader(editorPanel.getText())));
            parser = new parser(lexer);

            Object result = parser.parse().value;

            if (!lexer.errors.isEmpty() || !parser.errors.isEmpty()) {
                consoleTextArea.append("Se encontraron errores en el análisis. Revise el reporte de errores.\n");
                return;
            }

            if (!(result instanceof ASTNode ast)) {
                consoleTextArea.append("No se pudo construir el AST. Revise el reporte de errores.\n");
                return;
            }

            InterpreterVisitor interpreter = new InterpreterVisitor();
            interpreter.interpret(ast);
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
        } catch (ReturnSignal rs) {
            consoleTextArea.append(new GoliteError("Semantico",
                    "La sentencia return solo puede usarse dentro de una funcion",
                    rs.getLine(), rs.getColumn()).toString() + "\n");
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
            Object parsed = ps.parse().value;
            all.addAll(lx.errors);
            all.addAll(ps.errors);

            ASTNode ast = (parsed instanceof ASTNode node) ? node : null;

            if (lx.errors.isEmpty() && ps.errors.isEmpty() && ast != null) {
                try {
                    new InterpreterVisitor().interpret(ast);
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
                } catch (ReturnSignal rs) {   // ← nuevo catch
                    all.add(new GoliteError("Semantico",
                            "La sentencia return solo puede usarse dentro de una funcion",
                            rs.getLine(), rs.getColumn()));
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

    // crea un JFileChooser que solo muestra archivos .glt
    private JFileChooser buildChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos GoLite (*.glt)", "glt"));
        return chooser;
    }

    // Nuevo: archivo en blanco
    private void newFile() {
        editorPanel.setText("");
        currentFile = null;
        updateTitle();
        cleanConsole();
        editorPanel.getTextArea().requestFocus();
    }

    // Abrir: carga un .glt al editor
    private void openFile() {
        JFileChooser chooser = buildChooser();
        chooser.setDialogTitle("Abrir archivo");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            String contenido = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            editorPanel.setText(contenido);
            currentFile = file;
            updateTitle();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir el archivo:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Guardar: si nunca se guardo, se comporta como "Guardar como"
    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
            return;
        }
        writeToFile(currentFile);
    }

    // Guardar como: pide ruta y garantiza la extension .glt
    private void saveFileAs() {
        JFileChooser chooser = buildChooser();
        chooser.setDialogTitle("Guardar archivo");
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".glt")) {
            file = new File(file.getAbsolutePath() + ".glt");
        }
        currentFile = file;
        writeToFile(file);
        updateTitle();
    }

    // escribe el contenido del editor en disco (UTF-8)
    private void writeToFile(File file) {
        try {
            Files.writeString(file.toPath(), editorPanel.getText(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar el archivo:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // refleja el archivo abierto en la barra de titulo
    private void updateTitle() {
        if (currentFile == null) {
            setTitle("Golite - (sin titulo)");
        } else {
            setTitle("Golite - " + currentFile.getName());
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
