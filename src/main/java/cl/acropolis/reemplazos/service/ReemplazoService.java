package cl.acropolis.reemplazos.service;

import cl.acropolis.reemplazos.dto.BalanceMensualDTO;
import cl.acropolis.reemplazos.dto.DashboardStatsDTO;
import cl.acropolis.reemplazos.dto.ReemplazoRequest;
import cl.acropolis.reemplazos.model.Personal;
import cl.acropolis.reemplazos.model.Reemplazo;
import cl.acropolis.reemplazos.repository.PersonalRepository;
import cl.acropolis.reemplazos.repository.ReemplazoRepository;
import cl.acropolis.reemplazos.repository.AtrasoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReemplazoService {

    private final ReemplazoRepository reemplazoRepository;
    private final PersonalRepository personalRepository;
    private final AtrasoRepository atrasoRepository;

    /**
     * Registra un nuevo reemplazo.
     */
    @Transactional
    public Reemplazo registrarReemplazo(ReemplazoRequest request) {
        Personal ausente = personalRepository.findById(request.getDocenteAusenteId())
                .orElseThrow(
                        () -> new RuntimeException("Docente ausente no encontrado: " + request.getDocenteAusenteId()));

        Personal reemplazante = personalRepository.findById(request.getReemplazanteId())
                .orElseThrow(() -> new RuntimeException("Reemplazante no encontrado: " + request.getReemplazanteId()));

        if (ausente.getId().equals(reemplazante.getId())) {
            throw new RuntimeException("El docente ausente y el reemplazante no pueden ser la misma persona.");
        }

        Reemplazo reemplazo = Reemplazo.builder()
                .docenteAusente(ausente)
                .reemplazante(reemplazante)
                .fecha(LocalDate.parse(request.getFecha()))
                .horaInicio(request.getHoraInicio() != null && !request.getHoraInicio().isEmpty()
                        ? LocalTime.parse(request.getHoraInicio())
                        : null)
                .curso(request.getCurso())
                .asignatura(request.getAsignatura())
                .minutosReemplazo(request.getMinutosReemplazo())
                .build();

        return reemplazoRepository.save(reemplazo);
    }

    /**
     * Actualiza un reemplazo existente.
     */
    @Transactional
    public Reemplazo actualizarReemplazo(Long id, ReemplazoRequest request) {
        Reemplazo reemplazo = reemplazoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reemplazo no encontrado: " + id));

        Personal ausente = personalRepository.findById(request.getDocenteAusenteId())
                .orElseThrow(
                        () -> new RuntimeException("Docente ausente no encontrado: " + request.getDocenteAusenteId()));

        Personal reemplazante = personalRepository.findById(request.getReemplazanteId())
                .orElseThrow(() -> new RuntimeException("Reemplazante no encontrado: " + request.getReemplazanteId()));

        if (ausente.getId().equals(reemplazante.getId())) {
            throw new RuntimeException("El docente ausente y el reemplazante no pueden ser la misma persona.");
        }

        reemplazo.setDocenteAusente(ausente);
        reemplazo.setReemplazante(reemplazante);
        reemplazo.setFecha(LocalDate.parse(request.getFecha()));
        reemplazo.setHoraInicio(request.getHoraInicio() != null && !request.getHoraInicio().isEmpty()
                ? LocalTime.parse(request.getHoraInicio())
                : null);
        reemplazo.setCurso(request.getCurso());
        reemplazo.setAsignatura(request.getAsignatura());
        reemplazo.setMinutosReemplazo(request.getMinutosReemplazo());

        return reemplazoRepository.save(reemplazo);
    }

    /**
     * Elimina un reemplazo por su ID.
     */
    @Transactional
    public void eliminarReemplazo(Long id) {
        if (!reemplazoRepository.existsById(id)) {
            throw new RuntimeException("Reemplazo no encontrado: " + id);
        }
        reemplazoRepository.deleteById(id);
    }

    /**
     * Lista todos los reemplazos.
     */
    public List<Reemplazo> listarTodos() {
        return reemplazoRepository.findAll();
    }

    /**
     * Lista reemplazos por mes y año.
     */
    public List<Reemplazo> listarPorMesAnio(int mes, int anio) {
        return reemplazoRepository.findByMesYAnio(mes, anio);
    }

    /**
     * CÁLCULO MENSUAL: Para cada funcionario calcula Deuda, Favor y Balance.
     *
     * Balance = (TotalReemplazos - TotalAusencias) - MinutosContratoReemplazo
     */
    public List<BalanceMensualDTO> calcularBalanceMensual(int mes, int anio) {
        List<Personal> todosPersonal = personalRepository.findAllByOrderByApellidosAscNombreAsc();

        return todosPersonal.stream().map(persona -> {
            Integer totalAusente = reemplazoRepository.sumMinutosComoAusente(persona.getId(), mes, anio);
            Integer totalReemplazante = reemplazoRepository.sumMinutosComoReemplazante(persona.getId(), mes, anio);
            Integer totalAtraso = atrasoRepository.sumMinutosByPersonalAndMes(persona.getId(), mes, anio);
            Integer balance = (totalReemplazante - totalAusente) - persona.getMinutosContratoReemplazo();

            return BalanceMensualDTO.builder()
                    .personalId(persona.getId())
                    .run(persona.getRun())
                    .nombreCompleto(persona.getNombreCompleto())
                    .cargo(persona.getCargo())
                    .emailInstitucional(persona.getEmailInstitucional())
                    .minutosContratoReemplazo(persona.getMinutosContratoReemplazo())
                    .totalMinutosAusente(totalAusente)
                    .totalMinutosReemplazante(totalReemplazante)
                    .totalMinutosAtraso(totalAtraso)
                    .balance(balance)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Balance mensual de un funcionario específico.
     */
    public BalanceMensualDTO calcularBalanceIndividual(Long personalId, int mes, int anio) {
        Personal persona = personalRepository.findById(personalId)
                .orElseThrow(() -> new RuntimeException("Personal no encontrado: " + personalId));

        Integer totalAusente = reemplazoRepository.sumMinutosComoAusente(personalId, mes, anio);
        Integer totalReemplazante = reemplazoRepository.sumMinutosComoReemplazante(personalId, mes, anio);
        Integer totalAtraso = atrasoRepository.sumMinutosByPersonalAndMes(personalId, mes, anio);
        Integer balance = (totalReemplazante - totalAusente) - persona.getMinutosContratoReemplazo();

        return BalanceMensualDTO.builder()
                .personalId(persona.getId())
                .run(persona.getRun())
                .nombreCompleto(persona.getNombreCompleto())
                .cargo(persona.getCargo())
                .emailInstitucional(persona.getEmailInstitucional())
                .minutosContratoReemplazo(persona.getMinutosContratoReemplazo())
                .totalMinutosAusente(totalAusente)
                .totalMinutosReemplazante(totalReemplazante)
                .totalMinutosAtraso(totalAtraso)
                .balance(balance)
                .build();
    }

    /**
     * Tendencia de los últimos 6 meses de un funcionario (para gráfico de línea).
     */
    public List<java.util.Map<String, Object>> calcularTendenciaIndividual(Long personalId, int mes, int anio) {
        List<java.util.Map<String, Object>> tendencia = new java.util.ArrayList<>();
        String[] meses = {"", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        for (int i = 5; i >= 0; i--) {
            LocalDate fecha = LocalDate.of(anio, mes, 1).minusMonths(i);
            int m = fecha.getMonthValue();
            int a = fecha.getYear();

            Integer totalAusente = reemplazoRepository.sumMinutosComoAusente(personalId, m, a);
            Integer totalReemplazante = reemplazoRepository.sumMinutosComoReemplazante(personalId, m, a);
            Integer totalAtraso = atrasoRepository.sumMinutosByPersonalAndMes(personalId, m, a);

            java.util.Map<String, Object> punto = new java.util.LinkedHashMap<>();
            punto.put("mes", meses[m] + " " + a);
            punto.put("reemplazo", totalReemplazante);
            punto.put("ausencia", totalAusente);
            punto.put("atraso", totalAtraso);
            punto.put("balance", totalReemplazante - totalAusente);
            tendencia.add(punto);
        }
        return tendencia;
    }

    /**
     * Estadísticas para el Dashboard.
     */
    public DashboardStatsDTO obtenerEstadisticasDashboard() {
        LocalDate hoy = LocalDate.now();
        int mes = hoy.getMonthValue();
        int anio = hoy.getYear();

        Integer totalMinutos = reemplazoRepository.sumMinutosMes(mes, anio);
        Long totalReemplazos = reemplazoRepository.countByMesYAnio(mes, anio);
        Long totalPersonal = personalRepository.count();

        // Calcular totales de deuda y favor del mes
        List<BalanceMensualDTO> balances = calcularBalanceMensual(mes, anio);
        int totalDeuda = balances.stream().mapToInt(BalanceMensualDTO::getTotalMinutosAusente).sum();
        int totalFavor = balances.stream().mapToInt(BalanceMensualDTO::getTotalMinutosReemplazante).sum();

        return DashboardStatsDTO.builder()
                .totalMinutosMes(totalMinutos)
                .totalReemplazosMes(totalReemplazos)
                .totalPersonal(totalPersonal)
                .totalMinutosDeuda(totalDeuda)
                .totalMinutosFavor(totalFavor)
                .build();
    }

    /**
     * Movimientos individuales de un funcionario en un mes/año.
     * Retorna todos los reemplazos donde participó (como ausente o reemplazante).
     */
    public List<Reemplazo> obtenerMovimientosIndividuales(Long personalId, int mes, int anio) {
        return reemplazoRepository.findByMesYAnio(mes, anio).stream()
                .filter(r -> r.getDocenteAusente().getId().equals(personalId)
                        || r.getReemplazante().getId().equals(personalId))
                .collect(Collectors.toList());
    }

    /**
     * Últimos 10 reemplazos para el feed de actividad.
     */
    public List<Reemplazo> obtenerActividadReciente() {
        return reemplazoRepository.findTop10ByOrderByFechaDescHoraInicioDesc();
    }

    /**
     * Tendencia mensual para el gráfico del Dashboard.
     * Retorna totales de ausencias y reemplazos de los últimos 6 meses.
     */
    public List<java.util.Map<String, Object>> obtenerTendenciaMensualGlobal() {
        String[] mesesNombres = {"", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        LocalDate hoy = LocalDate.now();

        List<java.util.Map<String, Object>> resultado = new java.util.ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate fecha = hoy.minusMonths(i).withDayOfMonth(1);
            int m = fecha.getMonthValue();
            int a = fecha.getYear();

            Integer totalMinutosAusencia = reemplazoRepository.sumMinutosMes(m, a);
            Long totalReemplazos = reemplazoRepository.countByMesYAnio(m, a);

            java.util.Map<String, Object> punto = new java.util.LinkedHashMap<>();
            punto.put("mes", mesesNombres[m] + " " + a);
            punto.put("minutosAusencia", totalMinutosAusencia != null ? totalMinutosAusencia : 0);
            punto.put("cantidadReemplazos", totalReemplazos != null ? totalReemplazos : 0);
            resultado.add(punto);
        }
        return resultado;
    }
}
