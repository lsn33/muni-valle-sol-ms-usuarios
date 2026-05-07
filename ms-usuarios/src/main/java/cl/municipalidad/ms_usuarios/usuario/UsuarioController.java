package cl.municipalidad.ms_usuarios.usuario;

import cl.municipalidad.ms_usuarios.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> registrar(@RequestBody Map<String, String> body) {
        UsuarioDTO dto = usuarioService.registrar(
            body.get("nombre"),
            body.get("email"),
            body.get("password"),
            body.get("rol")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        return usuarioService.buscarPorEmail(body.get("email"))
            .filter(u -> passwordEncoder.matches(body.get("password"), u.getPassword()))
            .map(u -> {
                String token = jwtUtil.generarToken(u.getEmail(), u.getRol());
                return ResponseEntity.ok(Map.of("token", token, "rol", u.getRol()));
            })
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}