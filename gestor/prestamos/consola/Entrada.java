/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestor.prestamos.consola;


import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Objects;
import java.util.Scanner;

/**
 * Lectura de datos desde la consola con validación. Repite la petición hasta
 * obtener un valor correcto.
 */
public final class Entrada {

    private final Scanner scanner;

    /**
     * Crea una entrada que lee del escáner indicado.
     *
     * @param scanner fuente de las líneas introducidas por el usuario; no puede ser nula
     */
    public Entrada(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
    }

    /**
     * Pide un texto no vacío.
     *
     * @param mensaje texto que se muestra antes de leer
     * @return el texto introducido, sin espacios sobrantes
     */
    public String pedirTexto(String mensaje) {
        while (true) {
            String valor = leer(mensaje).trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("  El campo no puede estar vacío.");
        }
    }

    /**
     * Pide un número entero que debe pertenecer a un conjunto de opciones válidas.
     *
     * @param mensaje texto que se muestra antes de leer
     * @param validas números aceptados
     * @return el número elegido
     */
    public int pedirOpcion(String mensaje, Collection<Integer> validas) {
        while (true) {
            String texto = leer(mensaje).trim();
            try {
                int numero = Integer.parseInt(texto);
                if (validas.contains(numero)) {
                    return numero;
                }
            } catch (NumberFormatException e) {
                // El texto no es un número: se informa abajo y se vuelve a pedir.
            }
            System.out.println("  Opción no válida.");
        }
    }

    /**
     * Pide una fecha en formato {@code AAAA-MM-DD}. Si el usuario pulsa Enter y hay un valor
     * por defecto, se devuelve ese valor.
     *
     * @param mensaje texto que se muestra antes de leer
     * @param valorPorDefecto fecha que se usa al pulsar Enter, o {@code null} si no hay
     * @return la fecha introducida o el valor por defecto
     */
    public LocalDate pedirFecha(String mensaje, LocalDate valorPorDefecto) {
        while (true) {
            String texto = leer(mensaje).trim();
            if (texto.isEmpty() && valorPorDefecto != null) {
                return valorPorDefecto;
            }
            
            // REFACTORIZACIÓN: Replace Exception with Test
            // Se reemplazó el control de flujo basado en excepciones por una prueba previa
            // que valida el formato y la existencia de la fecha antes de parsearla.
            if (esFechaValida(texto)) {
                return LocalDate.parse(texto);
            }
            System.out.println("  Formato inválido. Usa AAAA-MM-DD.");
        }
    }

    private boolean esFechaValida(String texto) {
        if (!texto.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return false;
        }
        try {
            LocalDate.parse(texto);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private String leer(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine();
    }
}
            
    
