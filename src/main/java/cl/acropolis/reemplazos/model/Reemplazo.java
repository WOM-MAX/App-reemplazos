package cl.acropolis.reemplazos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reemplazo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reemplazo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "docente_ausente_id", nullable = false)
    @NotNull
    private Personal docenteAusente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reemplazante_id", nullable = false)
    @NotNull
    private Personal reemplazante;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(length = 30)
    private String curso;

    @Column(length = 80)
    private String asignatura;

    @NotNull
    @Column(name = "minutos_reemplazo", nullable = false)
    private Integer minutosReemplazo;
}
