package cl.acropolis.reemplazos.repository;

import cl.acropolis.reemplazos.model.Reemplazo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReemplazoRepository extends JpaRepository<Reemplazo, Long> {

    /**
     * Todos los reemplazos de un mes/año específico.
     */
    @Query("SELECT r FROM Reemplazo r " +
            "WHERE MONTH(r.fecha) = :mes AND YEAR(r.fecha) = :anio " +
            "ORDER BY r.fecha DESC, r.horaInicio DESC")
    List<Reemplazo> findByMesYAnio(@Param("mes") int mes, @Param("anio") int anio);

    /**
     * Suma de minutos donde un funcionario fue REEMPLAZANTE en un mes/año.
     */
    @Query("SELECT COALESCE(SUM(r.minutosReemplazo), 0) FROM Reemplazo r " +
            "WHERE r.reemplazante.id = :personalId " +
            "AND MONTH(r.fecha) = :mes AND YEAR(r.fecha) = :anio")
    Integer sumMinutosComoReemplazante(@Param("personalId") Long personalId,
            @Param("mes") int mes,
            @Param("anio") int anio);

    /**
     * Suma de minutos donde un funcionario fue DOCENTE AUSENTE en un mes/año.
     */
    @Query("SELECT COALESCE(SUM(r.minutosReemplazo), 0) FROM Reemplazo r " +
            "WHERE r.docenteAusente.id = :personalId " +
            "AND MONTH(r.fecha) = :mes AND YEAR(r.fecha) = :anio")
    Integer sumMinutosComoAusente(@Param("personalId") Long personalId,
            @Param("mes") int mes,
            @Param("anio") int anio);

    /**
     * Últimos N reemplazos registrados (para actividad reciente del dashboard).
     */
    List<Reemplazo> findTop10ByOrderByFechaDescHoraInicioDesc();

    /**
     * Contar reemplazos del mes actual.
     */
    @Query("SELECT COUNT(r) FROM Reemplazo r " +
            "WHERE MONTH(r.fecha) = :mes AND YEAR(r.fecha) = :anio")
    Long countByMesYAnio(@Param("mes") int mes, @Param("anio") int anio);

    /**
     * Suma total de minutos en el mes.
     */
    @Query("SELECT COALESCE(SUM(r.minutosReemplazo), 0) FROM Reemplazo r " +
            "WHERE MONTH(r.fecha) = :mes AND YEAR(r.fecha) = :anio")
    Integer sumMinutosMes(@Param("mes") int mes, @Param("anio") int anio);
}
