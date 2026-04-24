package cl.acropolis.reemplazos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "feriado_institucional", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"fecha"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeriadoInstitucional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String descripcion;

}
