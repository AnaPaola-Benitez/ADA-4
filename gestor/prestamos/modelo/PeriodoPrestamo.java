/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestor.prestamos.modelo;

import java.time.LocalDate;


public final class PeriodoPrestamo {
    private final LocalDate inicio;
    private final LocalDate limite;

    public PeriodoPrestamo(LocalDate inicio, LocalDate limite) {
        if (inicio == null || limite == null) {
            throw new IllegalArgumentException("Las fechas de préstamo y límite son obligatorias");
        }
        if (limite.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha límite no puede ser anterior al préstamo");
        }
        this.inicio = inicio;
        this.limite = limite;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getLimite() {
        return limite;
    }
}