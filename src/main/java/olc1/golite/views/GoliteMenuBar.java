package olc1.golite.views;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class GoliteMenuBar extends JMenuBar {

    private final JMenuItem itemNuevo = new JMenuItem("Nuevo");
    private final JMenuItem itemAbrir = new JMenuItem("Abrir...");
    private final JMenuItem itemGuardar = new JMenuItem("Guardar");
    private final JMenuItem itemGuardarComo = new JMenuItem("Guardar como...");
    private final JMenuItem itemSalir = new JMenuItem("Salir");

    private final JMenuItem itemEjecutar = new JMenuItem("Ejecutar");
    private final JMenuItem itemLimpiar = new JMenuItem("Limpiar consola");

    private final JMenuItem itemTokens = new JMenuItem("Reporte de tokens");
    private final JMenuItem itemErrores = new JMenuItem("Reporte de errores");

    private final JMenuItem itemAcerca = new JMenuItem("Acerca de");

    public GoliteMenuBar() {
        // Menu Archivo
        JMenu menuArchivo = new JMenu("Archivo");
        itemNuevo.setAccelerator(KeyStroke.getKeyStroke("control N"));
        itemAbrir.setAccelerator(KeyStroke.getKeyStroke("control O"));
        itemGuardar.setAccelerator(KeyStroke.getKeyStroke("control S"));
        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemGuardarComo);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        // Menu Ejecutar
        JMenu menuEjecutar = new JMenu("Ejecutar");
        itemEjecutar.setAccelerator(KeyStroke.getKeyStroke("F5"));
        menuEjecutar.add(itemEjecutar);
        menuEjecutar.add(itemLimpiar);

        // Menu Reportes
        JMenu menuReportes = new JMenu("Reportes");
        menuReportes.add(itemTokens);
        menuReportes.add(itemErrores);

        // Menu Ayuda
        JMenu menuAyuda = new JMenu("Ayuda");
        menuAyuda.add(itemAcerca);

        add(menuArchivo);
        add(menuEjecutar);
        add(menuReportes);
        add(menuAyuda);
    }

    // Hooks: el frame conecta aqui cada accion
    public void onNew(ActionListener l) {
        itemNuevo.addActionListener(l);
    }

    public void onOpen(ActionListener l) {
        itemAbrir.addActionListener(l);
    }

    public void onSave(ActionListener l) {
        itemGuardar.addActionListener(l);
    }

    public void onSaveAs(ActionListener l) {
        itemGuardarComo.addActionListener(l);
    }

    public void onExit(ActionListener l) {
        itemSalir.addActionListener(l);
    }

    public void onRun(ActionListener l) {
        itemEjecutar.addActionListener(l);
    }

    public void onClean(ActionListener l) {
        itemLimpiar.addActionListener(l);
    }

    public void onTokens(ActionListener l) {
        itemTokens.addActionListener(l);
    }

    public void onErrors(ActionListener l) {
        itemErrores.addActionListener(l);
    }

    public void onAbout(ActionListener l) {
        itemAcerca.addActionListener(l);
    }
}
