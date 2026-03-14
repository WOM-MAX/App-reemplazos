package cl.acropolis.reemplazos.controller;

import cl.acropolis.reemplazos.model.Usuario;
import cl.acropolis.reemplazos.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    /**
     * Devuelve la info del usuario logueado (para mostrar en el header).
     */
    @GetMapping("/api/auth/me")
    public ResponseEntity<Map<String, Object>> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("username", auth.getName());
        String rol = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_ENCARGADO")
                .replace("ROLE_", "");
        data.put("rol", rol);
        return ResponseEntity.ok(data);
    }

    // ── CRUD de usuarios (solo ADMIN, protegido por SecurityConfig) ──

    @GetMapping("/api/usuarios")
    public List<Usuario> listar() {
        return usuarioService.listarTodos();
    }

    @PostMapping("/api/usuarios")
    public ResponseEntity<?> crear(@RequestBody Usuario usuario) {
        try {
            Usuario creado = usuarioService.crear(usuario);
            return ResponseEntity.ok(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/usuarios/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        try {
            Usuario actualizado = usuarioService.actualizar(id, usuario);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/usuarios/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
