package cl.acropolis.reemplazos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceMensualDTO {
    private Long personalId;
    private String run;
    private String nombreCompleto;
    private String cargo;
    private String emailInstitucional;
    private Integer minutosContratoReemplazo;
    private Integer totalMinutosAusente; // Deuda: suma minutos donde fue ausente
    private Integer totalMinutosReemplazante; // Favor: suma minutos donde fue reemplazante
    private Integer totalMinutosAtraso; // Deuda adicional: suma minutos de atrasos
    private Integer balance; // (Favor - Deuda) - ContratoReemplazo
}
