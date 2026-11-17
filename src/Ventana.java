import java.time.Year;
import java.util.ArrayList;
import javax.swing.*;


public class Ventana extends JFrame {
    private enum TipoBusqueda { NINGUNA, CODIGO, TEXTO }

    private JPanel panelPrincipal;
    private JTabbedPane pestanas;
    private JPanel panelRegistro, panelBusqueda, panelRanking;
    private JTextField txtCodigo, txtTitulo, txtDirector, txtBuscarCodigo, txtBuscarTexto;
    private JTextField txtEditarTitulo, txtEditarDirector;
    private JComboBox<String> cmbGenero, cmbEditarGenero;
    private JSpinner spnAnio, spnEditarAnio;
    private JLabel lblTotal, lblResultados, lblCodigoSeleccionado, lblMeGusta;
    private JList<Pelicula> lstResultados;
    private JList<String> lstRanking;
    private JButton btnRegistrar, btnBuscarCodigo, btnBuscarTexto;
    private JButton btnGuardar, btnMeGusta, btnActualizarRanking;

    private final CatalogoPeliculas catalogo = new CatalogoPeliculas();
    private final DefaultListModel<Pelicula> modeloResultados = new DefaultListModel<>();
    private final DefaultListModel<String> modeloRanking = new DefaultListModel<>();
    private TipoBusqueda tipoBusqueda = TipoBusqueda.NINGUNA;
    private String ultimaConsulta = "";
    private int ultimoCodigoBuscado = -1;

    public Ventana() {
        super("CineLikes");
        setContentPane(panelPrincipal);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        cmbGenero.setModel(new DefaultComboBoxModel<>(CatalogoPeliculas.GENEROS));
        cmbEditarGenero.setModel(new DefaultComboBoxModel<>(CatalogoPeliculas.GENEROS));
        int anioActual = Year.now().getValue();
        spnAnio.setModel(new SpinnerNumberModel(anioActual, 1, anioActual, 1));
        spnEditarAnio.setModel(new SpinnerNumberModel(anioActual, 1, anioActual, 1));
        lstResultados.setModel(modeloResultados);
        lstRanking.setModel(modeloRanking);
        btnRegistrar.addActionListener(e -> registrar());
        btnBuscarCodigo.addActionListener(e -> buscarCodigo());
        btnBuscarTexto.addActionListener(e -> buscarTexto());
        btnGuardar.addActionListener(e -> guardarCambios());
        btnMeGusta.addActionListener(e -> darMeGusta());
        btnActualizarRanking.addActionListener(e -> actualizarRanking());
        lstResultados.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mostrarSeleccion();
        });
        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedIndex() == 2) actualizarRanking();
        });
        actualizarTotal();
        limpiarSeleccion();
    }

    private static int leerCodigo(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("El código debe ser un número entero positivo.");
        }
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "CineLikes", JOptionPane.INFORMATION_MESSAGE);
    }

    private void registrar() {
        try {
            catalogo.registrar(leerCodigo(txtCodigo.getText()), txtTitulo.getText(),
                    txtDirector.getText(), (String) cmbGenero.getSelectedItem(),
                    (Integer) spnAnio.getValue());
            actualizarTotal();
            refrescarResultados(-1);
            actualizarRanking();
            txtCodigo.setText(""); txtTitulo.setText(""); txtDirector.setText("");
            aviso("Película registrada.");
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
        }
    }

    private void buscarCodigo() {
        tipoBusqueda = TipoBusqueda.CODIGO;
        try {
            ultimoCodigoBuscado = leerCodigo(txtBuscarCodigo.getText());
            int indice = catalogo.buscarIndicePorCodigo(ultimoCodigoBuscado);
            ArrayList<Pelicula> resultado = new ArrayList<>();
            if (indice >= 0) resultado.add(catalogo.obtenerPeliculas().get(indice));
            presentarResultados(resultado, indice >= 0 ? ultimoCodigoBuscado : -1);
            if (indice < 0) aviso("No se encontró ese código.");
        } catch (IllegalArgumentException ex) {
            ultimoCodigoBuscado = -1;
            presentarResultados(new ArrayList<>(), -1);
            aviso(ex.getMessage());
        }
    }

    private void buscarTexto() {
        tipoBusqueda = TipoBusqueda.TEXTO;
        ultimaConsulta = txtBuscarTexto.getText();
        try {
            presentarResultados(catalogo.buscarCoincidencias(ultimaConsulta), -1);
        } catch (IllegalArgumentException ex) {
            presentarResultados(new ArrayList<>(), -1);
            aviso(ex.getMessage());
        }
    }

    private void presentarResultados(ArrayList<Pelicula> resultados, int codigoASeleccionar) {
        modeloResultados.clear();
        int indiceSeleccion = -1;
        for (Pelicula pelicula : resultados) {
            if (pelicula.getCodigo() == codigoASeleccionar) indiceSeleccion = modeloResultados.size();
            modeloResultados.addElement(pelicula);
        }
        lblResultados.setText("Resultados: " + resultados.size());
        if (indiceSeleccion >= 0) lstResultados.setSelectedIndex(indiceSeleccion);
        else lstResultados.clearSelection();
        mostrarSeleccion();
    }

    private void refrescarResultados(int codigoASeleccionar) {
        if (tipoBusqueda == TipoBusqueda.CODIGO && ultimoCodigoBuscado > 0) {
            Pelicula pelicula = catalogo.buscarPorCodigo(ultimoCodigoBuscado);
            ArrayList<Pelicula> resultado = new ArrayList<>();
            if (pelicula != null) resultado.add(pelicula);
            presentarResultados(resultado, codigoASeleccionar);
        } else if (tipoBusqueda == TipoBusqueda.TEXTO) {
            try {
                presentarResultados(catalogo.buscarCoincidencias(ultimaConsulta), codigoASeleccionar);
            } catch (IllegalArgumentException ex) {
                presentarResultados(new ArrayList<>(), -1);
            }
        }
    }

    private void mostrarSeleccion() {
        Pelicula pelicula = lstResultados.getSelectedValue();
        if (pelicula == null) {
            limpiarSeleccion();
            return;
        }
        lblCodigoSeleccionado.setText(String.valueOf(pelicula.getCodigo()));
        txtEditarTitulo.setText(pelicula.getTitulo());
        txtEditarDirector.setText(pelicula.getDirector());
        cmbEditarGenero.setSelectedItem(pelicula.getGenero());
        spnEditarAnio.setValue(pelicula.getAnio());
        lblMeGusta.setText("Me gusta: " + pelicula.getMeGusta());
    }

    private void limpiarSeleccion() {
        lblCodigoSeleccionado.setText("Ninguna película seleccionada");
        txtEditarTitulo.setText(""); txtEditarDirector.setText("");
        cmbEditarGenero.setSelectedIndex(0);
        spnEditarAnio.setValue(Year.now().getValue());
        lblMeGusta.setText("Me gusta: —");
    }

    private void guardarCambios() {
        Pelicula seleccion = lstResultados.getSelectedValue();
        if (seleccion == null) {
            aviso("Seleccione una película antes de guardar.");
            return;
        }
        int codigo = seleccion.getCodigo();
        try {
            catalogo.editarPelicula(codigo, txtEditarTitulo.getText(), txtEditarDirector.getText(),
                    (String) cmbEditarGenero.getSelectedItem(), (Integer) spnEditarAnio.getValue());
            refrescarResultados(codigo); // Puede desaparecer si deja de coincidir con el texto.
            actualizarRanking();
            aviso("Cambios guardados.");
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
        }
    }

    private void darMeGusta() {
        Pelicula seleccion = lstResultados.getSelectedValue();
        if (seleccion == null) {
            aviso("Seleccione una película antes de dar Me gusta.");
            return;
        }
        int codigo = seleccion.getCodigo();
        catalogo.darMeGusta(codigo);
        refrescarResultados(codigo);
        actualizarRanking();
    }

    private void actualizarRanking() {
        modeloRanking.clear();
        for (Pelicula pelicula : catalogo.obtenerRanking()) {
            modeloRanking.addElement(pelicula.getCodigo() + " | " + pelicula.getTitulo()
                    + " | Me gusta: " + pelicula.getMeGusta());
        }
    }

    private void actualizarTotal() {
        lblTotal.setText("Total de películas: " + catalogo.getTotal());
    }
}

