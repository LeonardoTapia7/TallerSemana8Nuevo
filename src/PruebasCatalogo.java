import java.util.ArrayList;


public class PruebasCatalogo {
    private static int comprobaciones;

    private static void verificar(boolean condicion, String mensaje) {
        comprobaciones++;
        if (!condicion) throw new AssertionError(mensaje);
    }

    private static void esperarError(Runnable accion, String mensaje) {
        boolean error = false;
        try {
            accion.run();
        } catch (IllegalArgumentException ex) {
            error = true;
        }
        verificar(error, mensaje);
    }

    private static void codigos(ArrayList<Pelicula> peliculas, int... esperados) {
        verificar(peliculas.size() == esperados.length, "Cantidad inesperada");
        for (int i = 0; i < esperados.length; i++) {
            verificar(peliculas.get(i).getCodigo() == esperados[i], "Código inesperado en " + i);
        }
    }

    public static void main(String[] args) {
        CatalogoPeliculas c = new CatalogoPeliculas();
        verificar(c.getTotal() == 20, "La carga inicial debe tener 20 películas");
        ArrayList<Pelicula> principal = c.obtenerPeliculas();
        for (int i = 0; i < principal.size(); i++) {
            verificar(principal.get(i).getCodigo() == 2001 + i, "Orden inicial por código");
            verificar(c.buscarIndicePorCodigo(2001 + i) == i, "Búsqueda binaria inicial");
        }
        codigos(c.buscarCoincidencias("  ESPACIO  "), 2002, 2007, 2013, 2019);
        codigos(c.buscarCoincidencias("vega"), 2004, 2011, 2018);
        codigos(c.buscarCoincidencias("puerto"), 2006);
        codigos(c.buscarCoincidencias("inexistente"));
        esperarError(() -> c.buscarCoincidencias("  "), "Consulta vacía");
        esperarError(() -> c.buscarCoincidencias(null), "Consulta nula");
        esperarError(() -> c.buscarIndicePorCodigo(0), "Código no positivo");
        verificar(c.buscarIndicePorCodigo(9999) == -1, "Código inexistente");

        ArrayList<Pelicula> ranking = c.obtenerRanking();
        verificar(ranking.size() == 20, "El ranking conserva todos los registros");
        codigos(new ArrayList<>(ranking.subList(0, 3)), 2003, 2006, 2011);
        for (int i = 1; i < ranking.size(); i++) {
            Pelicula anterior = ranking.get(i - 1);
            Pelicula actual = ranking.get(i);
            verificar(anterior.getMeGusta() >= actual.getMeGusta(), "Orden del ranking");
            if (anterior.getMeGusta() == actual.getMeGusta()) {
                verificar(anterior.getCodigo() < actual.getCodigo(), "Empate por código");
            }
        }
        verificar(c.buscarIndicePorCodigo(2005) == 4, "Ranking no altera el orden principal");
        esperarError(() -> c.registrar(2001, "Duplicado", "Alguien", "Drama", 2020),
                "Código duplicado");
        verificar(c.getTotal() == 20, "El duplicado no se registró");
        c.registrar(1999, "  Película nueva  ", "  Equipo  ", "Drama", 2020);
        verificar(c.getTotal() == 21, "Registro válido");
        verificar(c.buscarIndicePorCodigo(1999) == 0, "Inserción antes de los originales");
        verificar(c.buscarPorCodigo(1999).getMeGusta() == 0, "Nuevo registro inicia en cero");
        verificar(c.buscarPorCodigo(1999).getTitulo().equals("Película nueva"), "Texto normalizado");

        Pelicula pelicula = c.buscarPorCodigo(2015);
        String tituloAnterior = pelicula.getTitulo();
        String directorAnterior = pelicula.getDirector();
        esperarError(() -> c.editarPelicula(2015, "Otro título", "  ", "Comedia", 2022),
                "Edición inválida");
        verificar(pelicula.getTitulo().equals(tituloAnterior)
                && pelicula.getDirector().equals(directorAnterior), "Edición atómica");
        c.editarPelicula(2015, "Nuevo título", "Nueva directora", "Comedia", 2022);
        verificar(pelicula.getTitulo().equals("Nuevo título") && pelicula.getCodigo() == 2015
                && pelicula.getMeGusta() == 5, "Edición conserva código y contador");
        verificar(c.buscarCoincidencias("Memorias").isEmpty(), "La edición cambia las coincidencias");

        for (int i = 0; i < 3; i++) c.darMeGusta(2001);
        verificar(c.buscarPorCodigo(2001).getMeGusta() == 7, "Tres clics suman tres");
        verificar(c.obtenerRanking().get(0) == c.buscarPorCodigo(2001),
                "Ranking usa el mismo registro y queda primero");
        verificar(c.buscarIndicePorCodigo(2005) >= 0, "Binaria funciona tras el ranking");
        esperarError(() -> c.darMeGusta(9999), "No se incrementa un código inexistente");
        System.out.println("Correcto: " + comprobaciones + " comprobaciones del catálogo.");
    }
}
 