/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestor.prestamos.modelo;

/**
 * Situación de un préstamo en un momento concreto.
 */
public enum EstadoPrestamo {

    /** El préstamo está abierto y todavía dentro del plazo. */
    ACTIVO,

    /** El préstamo está abierto y la fecha límite ya ha pasado. */
    ATRASADO,

    /** La herramienta ya fue devuelta. */
    DEVUELTO
}
