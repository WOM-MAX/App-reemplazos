package cl.acropolis.reemplazos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "personal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Personal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String run;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(length = 80)
    private String cargo;

    @Email
    @Column(name = "email_institucional", length = 150)
    private String emailInstitucional;

    @Column(name = "minutos_contrato_reemplazo", nullable = false)
    @Builder.Default
    private Integer minutosContratoReemplazo = 0;

    /**
     * Retorna "APELLIDOS NOMBRE" para display en dropdowns.
     */
    public String getNombreCompleto() {
        return apellidos.toUpperCase() + " " + nombre.toUpperCase();
    }
}
