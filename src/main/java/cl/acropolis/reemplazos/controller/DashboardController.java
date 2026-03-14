package cl.acropolis.reemplazos.controller;

import cl.acropolis.reemplazos.dto.DashboardStatsDTO;
import cl.acropolis.reemplazos.service.ReemplazoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ReemplazoService reemplazoService;

    @GetMapping("/stats")
    public DashboardStatsDTO obtenerEstadisticas() {
        return reemplazoService.obtenerEstadisticasDashboard();
    }
}
