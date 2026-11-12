/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestor.prestamos.consola;

import gestor.prestamos.modelo.EstadoPrestamo;
import gestor.prestamos.modelo.Herramienta;
import gestor.prestamos.modelo.PeriodoPrestamo;
import gestor.prestamos.modelo.Prestamo;
import gestor.prestamos.persistencia.RepositorioCsv;
import gestor.prestamos.servicio.GestorPrestamos;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import java.util.Map;
import java.util.HashMap;

/**
 * Menú principal de la aplicación. Muestra las opciones, pide los datos necesarios
 * y delega las operaciones en el servicio. Tras cada cambio, guarda los datos.
 */
public final class MenuConsola {

    private static final String MENU = """

            ========== GESTOR DE PRÉSTAMOS ==========
              1. Ver herramientas y disponibilidad
              2. Añadir herramienta
              3. Prestar herramienta
              4. Registrar devolución
              5. Ver préstamos activos
              6. Ver historial y penalizaciones
              7. Dar de baja una herramienta
              8. Reactivar herramienta
              0. Salir
            =========================================
            """;

    private static final List<Integer> OPCIONES = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8);

    private final GestorPrestamos gestor;
    private final RepositorioCsv repositorio;
    private final Entrada entrada;

    private interface ComandoMenu {
        boolean ejecutar();
    }

    private final Map<Integer, ComandoMenu> comandos = new HashMap<>();

    /**
     * Crea el menú con sus dependencias.
     *
     * @param gestor servicio de préstamos
     * @param repositorio repositorio donde se guardan los datos
     * @param entrada lectura de datos desde la consola
     */
    public MenuConsola(GestorPrestamos gestor, RepositorioCsv repositorio, Entrada entrada) {
        this.gestor = gestor;
        this.repositorio = repositorio;
        this.entrada = entrada;
        
        comandos.put(1, () -> { listarHerramientas(); return false; });
        comandos.put(2, () -> { anadirHerramienta(); return false; });
        comandos.put(3, () -> { prestar(); return false; });
        comandos.put(4, () -> { devolver(); return false; });
        comandos.put(5, () -> { verPrestamosActivos(); return false; });
        comandos.put(6, () -> { verHistorial(); return false; });
        comandos.put(7, () -> { darDeBaja(); return false; });
        comandos.put(8, () -> { reactivar(); return false; });
        comandos.put(0, () -> true);
    }

    /**
     * Muestra el menú en bucle hasta que el usuario elige salir. Los errores de
     * negocio se muestran sin cerrar la aplicación.
     */
    public void ejecutar() {
        boolean salir = false;
        while (!salir) {
            System.out.println(MENU);
            int opcion = entrada.pedirOpcion("Elige una opción: ", OPCIONES);
            try {
                salir = ejecutarOpcion(opcion);
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("  Error: " + e.getMessage());
            }
        }
        System.out.println("¡Hasta luego!");
    }

    /**
     * Ejecuta la opción elegida.
     *
     * @param opcion número de opción del menú
     * @return {@code true} si el usuario quiere salir de la aplicación
     */
    private boolean ejecutarOpcion(int opcion) {
        /* 
         *REFACTORIZACION: 
         TÉCNICA 10: Replace Conditional with Polymorphism
         * Se eliminó el bloque 'switch' original. En su lugar, se implementó el patrón Command 
         * mediante la interfaz 'ComandoMenu' y un mapa. El polimorfismo delega la ejecución al 
         * objeto correspondiente sin necesidad de evaluar condicionales.
         */
        ComandoMenu comando = comandos.get(opcion);
        if (comando != null) {
            return comando.ejecutar();
        }
        return false;
    }

    private void listarHerramientas() {
        List<Herramienta> herramientas = gestor.getHerramientas();
        if (herramientas.isEmpty()) {
            System.out.println("\n  No hay herramientas registradas.");
            return;
        }
        System.out.println("\n  ID  Herramienta             Estado");
        System.out.println("  " + "-".repeat(60));
        for (Herramienta herramienta : herramientas) {
            Optional<Prestamo> activo = gestor.prestamoActivo(herramienta);
            String estado = activo
                    .map(p -> "Prestada a " + p.getPrestatario() + " (hasta " + p.getFechaLimite() + ")")
                    .orElse(herramienta.isDadaDeBaja() ? "Dada de baja" : "Disponible");
            System.out.printf("  %-4d%-24s%s%n", herramienta.getId(), herramienta.getNombre(), estado);
        }
    }

    private void darDeBaja() {
        List<Herramienta> candidatas = gestor.getHerramientas().stream()
                .filter(h -> !h.isDadaDeBaja())
                .toList();
        if (candidatas.isEmpty()) {
            System.out.println("\n  No hay herramientas que dar de baja.");
            return;
        }
        System.out.println("\n  Herramientas en uso:");
        for (Herramienta herramienta : candidatas) {
            System.out.printf("    %d. %s%n", herramienta.getId(), herramienta.getNombre());
        }
        List<Integer> ids = candidatas.stream().map(Herramienta::getId).toList();
        int id = entrada.pedirOpcion("ID de la herramienta a dar de baja: ", ids);
        Herramienta elegida = candidatas.stream()
                .filter(h -> h.getId() == id)
                .findFirst()
                .orElseThrow();
        gestor.darDeBaja(id);
        guardar();
        System.out.printf("  '%s' dada de baja. Su historial se conserva.%n", elegida.getNombre());
    }

    private void reactivar() {
        List<Herramienta> dadas = gestor.getHerramientas().stream()
                .filter(Herramienta::isDadaDeBaja)
                .toList();
        if (dadas.isEmpty()) {
            System.out.println("\n  No hay herramientas dadas de baja.");
            return;
        }
        System.out.println("\n  Herramientas dadas de baja:");
        for (Herramienta herramienta : dadas) {
            System.out.printf("    %d. %s%n", herramienta.getId(), herramienta.getNombre());
        }
        List<Integer> ids = dadas.stream().map(Herramienta::getId).toList();
        int id = entrada.pedirOpcion("ID de la herramienta a reactivar: ", ids);
        Herramienta elegida = dadas.stream()
                .filter(h -> h.getId() == id)
                .findFirst()
                .orElseThrow();
        gestor.reactivar(id);
        guardar();
        System.out.printf("  '%s' reactivada y disponible para préstamo.%n", elegida.getNombre());
    }

    private void anadirHerramienta() {
        String nombre = entrada.pedirTexto("Nombre de la herramienta: ");
        Herramienta nueva = gestor.anadirHerramienta(nombre);
        guardar();
        System.out.printf("  Herramienta '%s' añadida con ID %d.%n", nueva.getNombre(), nueva.getId());
    }

  
    // REFACTORIZACIÓN: Extract Method.
    // Se separan la selección, la captura de fechas y el registro en métodos
    // auxiliares, conservando el orden de ejecución y los textos originales.
    private void prestar() {
        List<Herramienta> disponibles = gestor.herramientasDisponibles();
        if (disponibles.isEmpty()) {
            System.out.println("\n  No hay herramientas disponibles para prestar.");
            return;
        }
        int id = seleccionarHerramientaParaPrestamo(disponibles);
        String prestatario = entrada.pedirTexto("Nombre de quien la recibe: ");

        LocalDate hoy = LocalDate.now();
        LocalDate inicio = solicitarFechaPrestamo(hoy);
        LocalDate limite = solicitarFechaLimite(inicio);

        registrarPrestamo(id, prestatario, inicio, limite);
    }

    // REFACTORIZACIÓN: Extract Method.
    // Se extrae la selección de herramientas.
    private int seleccionarHerramientaParaPrestamo(List<Herramienta> disponibles) {
        System.out.println("\n  Herramientas disponibles:");
        for (Herramienta herramienta : disponibles) {
            System.out.printf("    %d. %s%n", herramienta.getId(), herramienta.getNombre());
        }
        List<Integer> ids = disponibles.stream().map(Herramienta::getId).toList();
        return entrada.pedirOpcion("ID de la herramienta: ", ids);
    }

    // REFACTORIZACIÓN: Extract Method.
    // Se extrae la petición de fecha reutilizando la misma fecha por defecto.
    private LocalDate solicitarFechaPrestamo(LocalDate hoy) {
        return entrada.pedirFecha("Fecha de préstamo (AAAA-MM-DD, Enter = " + hoy + "): ", hoy);
    }

    // REFACTORIZACIÓN: Extract Method.
    // Se extrae el cálculo y la petición de la fecha límite.
    private LocalDate solicitarFechaLimite(LocalDate inicio) {
        LocalDate limitePorDefecto = inicio.plusDays(GestorPrestamos.DIAS_PRESTAMO_DEFECTO);
        return entrada.pedirFecha(
                "Fecha límite (AAAA-MM-DD, Enter = " + limitePorDefecto + "): ", limitePorDefecto);
    }

    // REFACTORIZACIÓN: Extract Method.
    // Se extraen el registro, el guardado y el mensaje final.
    private void registrarPrestamo(int id, String prestatario, LocalDate inicio, LocalDate limite) {
        PeriodoPrestamo periodo = new PeriodoPrestamo(inicio, limite);
        Prestamo nuevo = gestor.prestar(id, prestatario, periodo);
        guardar();
        System.out.printf("  '%s' prestada a %s hasta %s.%n",
                nuevo.getHerramienta().getNombre(), nuevo.getPrestatario(), nuevo.getFechaLimite());
    }
    
    private void devolver() {
        List<Prestamo> activos = gestor.prestamosActivos();
        if (activos.isEmpty()) {
            System.out.println("\n  No hay préstamos pendientes de devolución.");
            return;
        }
        System.out.println("\n  Préstamos activos:");
        for (Prestamo p : activos) {
            System.out.printf("    %d. %s - %s (límite %s)%n",
                    p.getId(), p.getHerramienta().getNombre(), p.getPrestatario(), p.getFechaLimite());
        }
        List<Integer> ids = activos.stream().map(Prestamo::getId).toList();
        int id = entrada.pedirOpcion("ID del préstamo a devolver: ", ids);

        LocalDate hoy = LocalDate.now();
        LocalDate fecha = entrada.pedirFecha("Fecha de devolución (AAAA-MM-DD, Enter = " + hoy + "): ", hoy);

        gestor.devolver(id, fecha);
        // Técnica Separate Query from Modifier: se consulta el préstamo ya actualizado por separado.
        Prestamo devuelto = gestor.buscarPrestamo(id).orElseThrow();
        
        guardar();
        System.out.println("  Devolución registrada.");
        if (devuelto.getDiasRetraso() > 0) {
            System.out.printf("  Retraso de %d día(s). Penalización: %.2f €%n",
                    devuelto.getDiasRetraso(), devuelto.getPenalizacion());
        } else {
            System.out.println("  Devuelta a tiempo, sin penalización.");
        }
    }

    private void verPrestamosActivos() {
        List<Prestamo> activos = gestor.prestamosActivos();

        if (activos.isEmpty()) {
            System.out.println("\n  No hay préstamos activos.");
            return;
        }

        System.out.println("\n  Préstamos activos:");

        for (Prestamo p : activos) {
            /* 
             * TÉCNICA 9: Decompose Conditional
             * Se extrajo el bloque if/else que mezclaba cálculo y presentación.
             * 
             * TÉCNICA 2: Replace Temp with Query 
             * Se utiliza obtenerFechaActual() en lugar de la variable temporal.
             */
            String situacion = p.estado(obtenerFechaActual()) == EstadoPrestamo.ATRASADO ? 
                               situacionAtrasado(p) : 
                               situacionAlDia(p);

            System.out.printf("    %s - %s | desde %s | %s%n",
                    p.getHerramienta().getNombre(),
                    p.getPrestatario(),
                    p.getFechaPrestamo(),
                    situacion);
        }
    }

    // MÉTODOS DE LA TÉCNICA 9 (Decompose Conditional) 
    private String situacionAtrasado(Prestamo p) {
        long dias = p.getDiasRetrasoA(obtenerFechaActual());
        double estimada = p.calcularPenalizacion(
                obtenerFechaActual(),
                GestorPrestamos.PENALIZACION_POR_DIA);
        
        return String.format("ATRASADO %d día(s), penalización estimada %.2f €", dias, estimada);
    }

    private String situacionAlDia(Prestamo p) {
        return "Al día, vence " + p.getFechaLimite();
    }

    // MÉTODO DE LA TÉCNICA 2 (Replace Temp with Query) 
    // Se creó un método que devuelve la fecha actual, reemplazando la variable temporal 'hoy'.
    private LocalDate obtenerFechaActual() {
        return LocalDate.now();
    }

    private void verHistorial() {
        List<Prestamo> historial = gestor.historial();
        if (historial.isEmpty()) {
            System.out.println("\n  No hay devoluciones registradas.");
            return;
        }
        System.out.println("\n  Historial de devoluciones:");
        for (Prestamo p : historial) {
            System.out.printf("    %s - %s | %s -> %s | retraso %d día(s) | penalización %.2f €%n",
                    p.getHerramienta().getNombre(), p.getPrestatario(), p.getFechaPrestamo(),
                    p.getFechaDevolucion(), p.getDiasRetraso(), p.getPenalizacion());
        }
        System.out.printf("  Total de penalizaciones cobradas: %.2f €%n", gestor.totalPenalizaciones());
    }

    private void guardar() {
        try {
            repositorio.guardarHerramientas(gestor.getHerramientas());
            repositorio.guardarPrestamos(gestor.getPrestamos());
        } catch (IOException e) {
            System.out.println("  Aviso: no se pudieron guardar los datos: " + e.getMessage());
        }
    }
}