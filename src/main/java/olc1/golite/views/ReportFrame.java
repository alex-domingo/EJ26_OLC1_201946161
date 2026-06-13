package olc1.golite.views;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

// Ventana generica para mostrar un reporte en forma de tabla
public class ReportFrame extends JFrame {

    public ReportFrame(String titulo, String[] columnas, Object[][] datos) {
        setTitle(titulo);
        setSize(720, 420);
        setLocationRelativeTo(null);

        // la tabla no debe ser editable
        DefaultTableModel model = new DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        JTable tabla = new JTable(model);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        setVisible(true);
    }
}
