package cl.acropolis.reemplazos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReemplazoRequest {
    private Long docenteAusenteId;
    private Long reemplazanteId;
    private String fecha;
    private String horaInicio;
    private String curso;
    private String asignatura;
    private Integer minutosReemplazo;
}
