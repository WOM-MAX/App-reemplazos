package cl.acropolis.reemplazos.service;

import cl.acropolis.reemplazos.model.Atraso;
import cl.acropolis.reemplazos.model.Personal;
import cl.acropolis.reemplazos.repository.AtrasoRepository;
import cl.acropolis.reemplazos.repository.PersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AtrasoService {

    private final AtrasoRepository atrasoRepository;
    private final PersonalRepository personalRepository;

    /**
     * Registrar un nuevo atraso.
     */
    public Atraso registrar(Long personalId, LocalDate fecha, int minutosAtraso, String observacion) {
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new RuntimeException("Personal no encontrado con ID: " + personalId));

        Atraso atraso = Atraso.builder()
                .personal(personal)
                .fecha(fecha)
                .minutosAtraso(minutosAtraso)
                .observacion(observacion)
                .build();

        return atrasoRepository.save(atraso);
    }

    /**
     * Actualizar un atraso existente.
     */
    public Atraso actualizar(Long id, int minutosAtraso, String observacion) {
        Atraso atraso = atrasoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atraso no encontrado con ID: " + id));

        atraso.setMinutosAtraso(minutosAtraso);
        atraso.setObservacion(observacion);

        return atrasoRepository.save(atraso);
    }

    /**
     * Eliminar un atraso.
     */
    public void eliminar(Long id) {
        if (!atrasoRepository.existsById(id)) {
            throw new RuntimeException("Atraso no encontrado con ID: " + id);
        }
        atrasoRepository.deleteById(id);
    }

    /**
     * Listar atrasos de un mes/año.
     */
    public List<Atraso> listarPorMes(int mes, int anio) {
        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        return atrasoRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
    }

    /**
     * Listar atrasos de un funcionario en un mes/año.
     */
    public List<Atraso> listarPorPersonalYMes(Long personalId, int mes, int anio) {
        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        return atrasoRepository.findByPersonalIdAndFechaBetweenOrderByFechaDesc(personalId, inicio, fin);
    }

    /**
     * Obtener resumen mensual de un funcionario.
     */
    public Map<String, Object> obtenerResumenMensual(Long personalId, int mes, int anio) {
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new RuntimeException("Personal no encontrado con ID: " + personalId));

        int totalMinutos = atrasoRepository.sumMinutosByPersonalAndMes(personalId, mes, anio);
        int totalDias = atrasoRepository.countByPersonalAndMes(personalId, mes, anio);
        List<Atraso> atrasos = listarPorPersonalYMes(personalId, mes, anio);

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("nombreCompleto", personal.getNombreCompleto());
        resumen.put("run", personal.getRun());
        resumen.put("cargo", personal.getCargo());
        resumen.put("totalMinutos", totalMinutos);
        resumen.put("totalDias", totalDias);
        resumen.put("promedioMinutos", totalDias > 0 ? Math.round((double) totalMinutos / totalDias) : 0);
        resumen.put("atrasos", atrasos);

        return resumen;
    }
}
