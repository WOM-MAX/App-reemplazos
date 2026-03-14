package cl.acropolis.reemplazos.config;

import cl.acropolis.reemplazos.model.Usuario;
import cl.acropolis.reemplazos.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioService usuarioService;

    @Override
    public void run(String... args) {
        if (!usuarioService.existeAlguno()) {
            Usuario admin = Usuario.builder()
                    .username("admin")
                    .password("admin123")
                    .nombreCompleto("Administrador")
                    .email("admin@colegioacropolis.cl")
                    .rol("ADMIN")
                    .activo(true)
                    .build();
            usuarioService.crear(admin);
            log.info("✅ Usuario administrador creado: admin / admin123");
        }
    }
}
