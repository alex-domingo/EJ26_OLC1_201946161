package olc1.golite.views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MainPanel extends JPanel {

    public MainPanel(JPanel editorPanel, JTextArea consoleTextArea) {
        setLayout(new BorderLayout());

        // Console setup
        consoleTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        consoleTextArea.setBackground(Color.darkGray);
        consoleTextArea.setForeground(Color.lightGray);
        consoleTextArea.setCaretColor(Color.darkGray);
        consoleTextArea.setEditable(false);
        consoleTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        consoleTextArea.setLineWrap(true);
        consoleTextArea.setWrapStyleWord(true);
        JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, consoleScrollPane);
        splitPane.setResizeWeight(0.67);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Posición inicial proporcional: espera al primer resize para tener tamaño real
        splitPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                splitPane.setDividerLocation(0.67);
                splitPane.removeComponentListener(this);
            }
        });

        add(splitPane, BorderLayout.CENTER);
    }
}
