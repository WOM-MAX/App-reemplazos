package cl.acropolis.reemplazos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private Integer totalMinutosMes;
    private Long totalReemplazosMes;
    private Long totalPersonal;
    private Integer totalMinutosDeuda;
    private Integer totalMinutosFavor;
}
