package cl.acropolis.reemplazos.repository;

import cl.acropolis.reemplazos.model.Personal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalRepository extends JpaRepository<Personal, Long> {

    Optional<Personal> findByRun(String run);

    List<Personal> findByApellidosContainingIgnoreCaseOrNombreContainingIgnoreCase(String apellidos, String nombre);

    List<Personal> findAllByOrderByApellidosAscNombreAsc();
}
