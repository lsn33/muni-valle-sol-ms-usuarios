package cl.municipalidad.msusers.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import cl.municipalidad.msusers.dto.UserDTO;
import cl.municipalidad.msusers.security.JwtUtil;
import cl.municipalidad.msusers.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService usuarioService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registrar(@RequestBody Map<String, String> body) {
        UserDTO dto = usuarioService.registrar(
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

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> buscarPorEmail(@PathVariable String email) {
        return usuarioService.buscarPorEmail(email)
            .map(u -> ResponseEntity.ok(new UserDTO(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getRol(),
                u.getActivo()
            )))
            .orElse(ResponseEntity.notFound().build());
    }
}