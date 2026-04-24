package cl.acropolis.reemplazos.repository;

import cl.acropolis.reemplazos.model.FeriadoInstitucional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FeriadoInstitucionalRepository extends JpaRepository<FeriadoInstitucional, Long> {
    List<FeriadoInstitucional> findByFechaBetweenOrderByFechaAsc(LocalDate startInclusive, LocalDate endInclusive);
}
