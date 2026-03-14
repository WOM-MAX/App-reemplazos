package cl.acropolis.reemplazos.repository;

import cl.acropolis.reemplazos.model.Atraso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AtrasoRepository extends JpaRepository<Atraso, Long> {

    List<Atraso> findByFechaBetweenOrderByFechaDesc(LocalDate inicio, LocalDate fin);

    List<Atraso> findByPersonalIdAndFechaBetweenOrderByFechaDesc(Long personalId, LocalDate inicio, LocalDate fin);

    @Query("SELECT COALESCE(SUM(a.minutosAtraso), 0) FROM Atraso a " +
            "WHERE a.personal.id = :personalId " +
            "AND MONTH(a.fecha) = :mes AND YEAR(a.fecha) = :anio")
    int sumMinutosByPersonalAndMes(@Param("personalId") Long personalId,
            @Param("mes") int mes,
            @Param("anio") int anio);

    @Query("SELECT COUNT(a) FROM Atraso a " +
            "WHERE a.personal.id = :personalId " +
            "AND MONTH(a.fecha) = :mes AND YEAR(a.fecha) = :anio")
    int countByPersonalAndMes(@Param("personalId") Long personalId,
            @Param("mes") int mes,
            @Param("anio") int anio);
}
