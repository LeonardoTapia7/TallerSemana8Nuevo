import java.time.Year;
import java.util.ArrayList;
import java.util.Locale;

public class CatalogoPeliculas {
    public static final String[] GENEROS = {
            "Ciencia ficción", "Drama", "Aventura", "Comedia",
            "Animación", "Suspenso", "Documental"
    };

    private final ArrayList<Pelicula> peliculas;

    public CatalogoPeliculas() {
        peliculas = DatosIniciales.cargar(); // Exactamente una carga por instancia.
        ordenarPorCodigoBurbuja();
    }

    public int getTotal() {
        return peliculas.size();
    }

    public ArrayList<Pelicula> obtenerPeliculas() {
        return new ArrayList<>(peliculas);
    }

    private static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.trim();
    }

    private static void validarDatos(int codigo, String titulo, String director,
                                     String genero, int anio) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("El código debe ser positivo.");
        }
        textoObligatorio(titulo, "El título");
        textoObligatorio(director, "El director");
        boolean generoValido = false;
        for (String permitido : GENEROS) {
            if (permitido.equals(genero)) {
                generoValido = true;
                break;
            }
        }
        if (!generoValido) {
            throw new IllegalArgumentException("Seleccione un género válido.");
        }
        if (anio < 1 || anio > Year.now().getValue()) {
            throw new IllegalArgumentException("El año debe estar entre 1 y el actual.");
        }
    }

    public void registrar(int codigo, String titulo, String director,
                          String genero, int anio) {
        validarDatos(codigo, titulo, director, genero, anio);
        if (buscarIndicePorCodigo(codigo) >= 0) {
            throw new IllegalArgumentException("Ya existe una película con ese código.");
        }
        peliculas.add(new Pelicula(codigo, titulo.trim(), director.trim(), genero, anio, 0));
        ordenarPorCodigoBurbuja();
    }

    public ArrayList<Pelicula> buscarCoincidencias(String texto) {
        String consulta = textoObligatorio(texto, "La consulta").toLowerCase(Locale.ROOT);
        ArrayList<Pelicula> resultados = new ArrayList<>();
        for (Pelicula pelicula : peliculas) {
            if (pelicula.getTitulo().toLowerCase(Locale.ROOT).contains(consulta)
                    || pelicula.getDirector().toLowerCase(Locale.ROOT).contains(consulta)) {
                resultados.add(pelicula);
            }
        }
        return resultados;
    }

    public int buscarIndicePorCodigo(int codigo) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("El código debe ser positivo.");
        }
        int izquierda = 0;
        int derecha = peliculas.size() - 1;
        while (izquierda <= derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;
            int codigoMedio = peliculas.get(medio).getCodigo();
            if (codigoMedio == codigo) {
                return medio;
            }
            if (codigoMedio < codigo) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }
        return -1;
    }

    public Pelicula buscarPorCodigo(int codigo) {
        int indice = buscarIndicePorCodigo(codigo);
        return indice < 0 ? null : peliculas.get(indice);
    }

    public void editarPelicula(int codigo, String titulo, String director,
                               String genero, int anio) {
        validarDatos(codigo, titulo, director, genero, anio); // Todo antes de mutar.
        Pelicula pelicula = buscarPorCodigo(codigo);
        if (pelicula == null) {
            throw new IllegalArgumentException("No existe una película con ese código.");
        }
        pelicula.setTitulo(titulo.trim());
        pelicula.setDirector(director.trim());
        pelicula.setGenero(genero);
        pelicula.setAnio(anio);
    }

    public void darMeGusta(int codigo) {
        Pelicula pelicula = buscarPorCodigo(codigo);
        if (pelicula == null) {
            throw new IllegalArgumentException("No existe una película con ese código.");
        }
        pelicula.incrementarMeGusta();
    }

    public void ordenarPorCodigoBurbuja() {
        for (int fin = peliculas.size() - 1; fin > 0; fin--) {
            boolean intercambio = false;
            for (int i = 0; i < fin; i++) {
                if (peliculas.get(i).getCodigo() > peliculas.get(i + 1).getCodigo()) {
                    Pelicula temporal = peliculas.get(i);
                    peliculas.set(i, peliculas.get(i + 1));
                    peliculas.set(i + 1, temporal);
                    intercambio = true;
                }
            }
            if (!intercambio) {
                break;
            }
        }
    }

    public ArrayList<Pelicula> obtenerRanking() {
        ArrayList<Pelicula> ranking = new ArrayList<>(peliculas);
        for (int fin = ranking.size() - 1; fin > 0; fin--) {
            boolean intercambio = false;
            for (int i = 0; i < fin; i++) {
                if (ranking.get(i).getMeGusta() < ranking.get(i + 1).getMeGusta()) {
                    Pelicula temporal = ranking.get(i);
                    ranking.set(i, ranking.get(i + 1));
                    ranking.set(i + 1, temporal);
                    intercambio = true;
                }
            }
            if (!intercambio) {
                break;
            }
        }
        return ranking;
    }
}