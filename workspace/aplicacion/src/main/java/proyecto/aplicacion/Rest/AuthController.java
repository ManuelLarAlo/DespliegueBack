package proyecto.aplicacion.Rest;

import java.io.IOException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.dto.AuthResponse;
import proyecto.aplicacion.models.dto.LoginRequest;
import proyecto.aplicacion.models.dto.UsuarioDTO;
import proyecto.aplicacion.services.AuthService;
import proyecto.aplicacion.services.CustomUserDetailsService;
import proyecto.aplicacion.utils.exceptions.MandatoryResourceException;
import proyecto.aplicacion.utils.exceptions.ResourceAlreadyExistsException;
import proyecto.aplicacion.utils.exceptions.ResourceNotFoundException;
import proyecto.aplicacion.utils.exceptions.SessionExpiredException;
import proyecto.aplicacion.utils.exceptions.UnauthorizedException;

@RestController
@RequestMapping("/auth")
@CrossOrigin() //Tengo que ajustar el puerto desde donde se vaya a ejecutar el frontend, vue.js generalmente usa el puerto 8080
@Slf4j         //El crossOrigin solo se debe habilitar para métodos o controladores relacionados con la autenticación del usuario, no se debe habilitar en el controlador de tareas, por ejemplo
               //Ya que el cors se pude configurar globalmente en la clase SecurityConfig, no es necesario configurarlo por controladores, a no ser que se necesite cambiar
               //la configuración global en un controlador específico
public class AuthController {
	
	//Implementación servicios
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	@Autowired
	private AuthService authService;
	
	@RequestMapping(value = "/registro", method = RequestMethod.POST)
	public ResponseEntity<?> registrarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) throws ResourceAlreadyExistsException, MandatoryResourceException, ResourceNotFoundException {
		
		//Valida si el usuario existe a partir del método existsByUsername, creado en el servicio
		this.customUserDetailsService.validarEmail(usuarioDTO.getEmail());
		
		this.customUserDetailsService.crearUsuario(usuarioDTO);
		return new ResponseEntity<>("El usuario se ha creado correctamente", HttpStatus.CREATED);
	}
    
	@RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) throws UnauthorizedException {
		log.info("Intento de inicio de sesión para el usuario {} ", loginRequest.getEmail());
		
        AuthResponse authResponse = authService.login(loginRequest, response);
        
        log.info("El usuario {} ha iniciado sesión correctamente", loginRequest.getEmail());
        return ResponseEntity.ok().body(authResponse);
    }
	
	
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Inicio del proceso del logout");
    	
    	// Llamar al servicio para realizar el logout
        authService.logout(request, response);
        log.info("Logout exitoso para el usuario");
        
        return ResponseEntity.ok().body("Logout exitoso");
    }
	
	@RequestMapping(value ="/refresh", method = RequestMethod.POST)
	 public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException, SessionExpiredException {
		
		log.info("Recibida solicitud para refrescar el access token.");
		
		String newAccessToken = authService.refreshAccessToken(request, response);
		log.info("Access token generado exitosamente");
		
		return ResponseEntity.ok(new AuthResponse(newAccessToken));
	}
	
	//---------------------METODOS DEPRECADOS---------------------
	
	/*
	//Método usado para logear al usuario en el sistema, crear tanto el accessToken como el refreshToken y almacena el refreshToken en una cookie para mantener la sesión iniciada mientras sea válido
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        // Validamos si el usuario existe en la base de datos
        Usuario user = this.iUsuarioRepository.findByUsername(loginRequest.getUsername());

     // Se verifican las credenciales del usuario y se devuelve un mensaje de error genérico para no exponer información
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // No se especifica si el usuario o la contraseña es incorrecta, solo un error genérico.
            return new ResponseEntity<>("Credenciales incorrectas", HttpStatus.UNAUTHORIZED);
        }

        // Si las credenciales son correctas, generamos los tokens
        String username = user.getUsername();
        String accessToken = JwtUtil.generateAccessToken(username);  // Generar Access Token
        String refreshToken = JwtUtil.generateRefreshToken(username); // Generar Refresh Token
        
        // Establecer refresh token en una cookie HTTP-Only
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true); // Asegura que no sea accesible por JavaScript
        refreshTokenCookie.setSecure(true);  // Asegura que se envíe solo por HTTPS
        refreshTokenCookie.setPath("/");     // Configura el path donde se puede acceder a la cookie
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 30);  // 30 días de expiración 
        response.addCookie(refreshTokenCookie);

        // Retornamos los tokens en una respuesta exitosa
        return ResponseEntity.ok(new AuthResponse(accessToken));
    }
    
        //Método que se encarga de refrescar el accessToken mientras el refreshToken sea válido
    @RequestMapping(value = "/refresh", method = RequestMethod.POST)
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        // Obtener el refresh token de la cookie
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        // Buscar la cookie llamada "refresh_token"
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // Si no se encuentra el refresh token, devolver error
        if (refreshToken == null) {
            return ResponseEntity.status(401).body("No se encontró el refresh token en las cookies");
        }

        // Verificar si el refresh token está expirado
        if (JwtUtil.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(401).body("Refresh token expirado");
        }

        // Si el refresh token es válido, obtener el username y generar un nuevo access token
        String username = JwtUtil.extractUsername(refreshToken);
        String newAccessToken = JwtUtil.generateAccessToken(username);

        // Devuelve solo el nuevo access token (no el refresh token)
        return ResponseEntity.ok(new AuthResponse(newAccessToken)); // Solo enviar el access token
    }
    */

}
