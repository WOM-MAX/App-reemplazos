package cl.acropolis.reemplazos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "atraso", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "personal_id", "fecha" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Atraso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "personal_id", nullable = false)
    @NotNull
    private Personal personal;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull
    @Min(1)
    @Column(name = "minutos_atraso", nullable = false)
    private Integer minutosAtraso;

    @Column(length = 200)
    private String observacion;
}
