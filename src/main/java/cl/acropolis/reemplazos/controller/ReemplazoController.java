package cl.acropolis.reemplazos.controller;

import cl.acropolis.reemplazos.dto.ReemplazoRequest;
import cl.acropolis.reemplazos.dto.BalanceMensualDTO;
import cl.acropolis.reemplazos.model.Reemplazo;
import cl.acropolis.reemplazos.service.ReemplazoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reemplazos")
@RequiredArgsConstructor
public class ReemplazoController {
    private final ReemplazoService reemplazoService;
    private final cl.acropolis.reemplazos.repository.AtrasoRepository atrasoRepository;

    @GetMapping
    public List<Reemplazo> listarTodos() {
        return reemplazoService.listarTodos();
    }

    @GetMapping("/mes")
    public List<Reemplazo> listarPorMes(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) {
        if (mes == 0)
            mes = LocalDate.now().getMonthValue();
        if (anio == 0)
            anio = LocalDate.now().getYear();
        return reemplazoService.listarPorMesAnio(mes, anio);
    }

    @PostMapping
    public ResponseEntity<Reemplazo> registrar(@RequestBody ReemplazoRequest request) {
        try {
            Reemplazo reemplazo = reemplazoService.registrarReemplazo(request);
            return ResponseEntity.ok(reemplazo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reemplazo> actualizar(@PathVariable Long id, @RequestBody ReemplazoRequest request) {
        try {
            Reemplazo reemplazo = reemplazoService.actualizarReemplazo(id, request);
            return ResponseEntity.ok(reemplazo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            reemplazoService.eliminarReemplazo(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cálculo mensual de balance para todos los funcionarios.
     */
    @GetMapping("/calculo-mensual")
    public List<BalanceMensualDTO> calculoMensual(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) {
        if (mes == 0)
            mes = LocalDate.now().getMonthValue();
        if (anio == 0)
            anio = LocalDate.now().getYear();
        return reemplazoService.calcularBalanceMensual(mes, anio);
    }

    /**
     * Actividad reciente (últimos 10 reemplazos).
     */
    @GetMapping("/recientes")
    public List<Reemplazo> actividadReciente() {
        return reemplazoService.obtenerActividadReciente();
    }

    /**
     * Estadísticas agrupadas por día de la semana (para gráfico del Dashboard).
     */
    @GetMapping("/estadisticas-diarias")
    public List<java.util.Map<String, Object>> estadisticasDiarias(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) {
        if (mes == 0)
            mes = LocalDate.now().getMonthValue();
        if (anio == 0)
            anio = LocalDate.now().getYear();
        return reemplazoService.obtenerEstadisticasDiarias(mes, anio);
    }

    /**
     * Movimientos individuales de un funcionario en un mes/año + balance.
     */
    @GetMapping("/movimientos/{personalId}")
    public Map<String, Object> movimientosIndividuales(
            @PathVariable Long personalId,
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) {
        if (mes == 0)
            mes = LocalDate.now().getMonthValue();
        if (anio == 0)
            anio = LocalDate.now().getYear();

        BalanceMensualDTO balance = reemplazoService.calcularBalanceIndividual(personalId, mes, anio);
        List<Reemplazo> movimientos = reemplazoService.obtenerMovimientosIndividuales(personalId, mes, anio);

        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        List<cl.acropolis.reemplazos.model.Atraso> atrasos = atrasoRepository.findByPersonalIdAndFechaBetweenOrderByFechaDesc(personalId, inicio, fin);

        Map<String, Object> result = new HashMap<>();
        result.put("balance", balance);
        result.put("movimientos", movimientos);
        result.put("atrasos", atrasos);
        return result;
    }
}
