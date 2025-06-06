package proyecto.aplicacion.services;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.dto.AuthResponse;
import proyecto.aplicacion.models.dto.LoginRequest;
import proyecto.aplicacion.models.entities.Usuario;
import proyecto.aplicacion.repositories.IUsuarioRepository;
import proyecto.aplicacion.utils.JwtUtil;
import proyecto.aplicacion.utils.exceptions.SessionExpiredException;
import proyecto.aplicacion.utils.exceptions.UnauthorizedException;

@Service
@Slf4j
public class AuthService {
	
	//Repositorios
	@Autowired
    private IUsuarioRepository iUsuarioRepository;
    
	@Autowired
    private PasswordEncoder passwordEncoder;
	
    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${jwt.cookie-expiration}")
    private long cookieExpiration; // Tiempo en milisegundos
	
	public AuthResponse login(LoginRequest loginRequest, HttpServletResponse response) throws UnauthorizedException {
        log.debug("Buscando usuario {} en la base de datos", loginRequest.getEmail());
		
		// Validamos si el usuario existe en la base de datos
        
        Optional<Usuario> userOpt = iUsuarioRepository.findByEmail(loginRequest.getEmail());

        if (userOpt.isEmpty()) {
            throw new UnauthorizedException(401, "Credenciales incorrectas");
        }

        Usuario user = userOpt.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(401, "Credenciales incorrectas");
        }

        // Se verifican las credenciales y lanzamos excepción si son incorrectas
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
		    String error = "Credenciales incorrectas";
		    log.error(error);
            throw new UnauthorizedException(401, error);
        }
        
        log.info("Generando tokens para el usuario {}",loginRequest.getEmail());
        // Si las credenciales son correctas, generamos los tokens
        String email = user.getEmail();
        String accessToken = jwtUtil.generateAccessToken(email);  
        String refreshToken = jwtUtil.generateRefreshToken(email);
        
        log.debug("Access token generado para el usuario '{}': {}", email, accessToken);
        log.debug("Refresh token generado para el usuario '{}': {}", email, refreshToken);
        
        setRefreshTokenCookie(response, refreshToken);
        
        log.info("Tokens generados y refresh token establecido en la cookie.");
        
        return new AuthResponse(accessToken);

    }
	
	public void setRefreshTokenCookie (HttpServletResponse response, String refreshToken) {
        int refreshTokenCookieExpiration = (int) (cookieExpiration / 1000);
        
        log.debug("Estableciendo refresh token en la cookie con expiración de {} segundos", refreshTokenCookieExpiration);

        // Establecer refresh token en una cookie HTTP-Only
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(refreshTokenCookieExpiration);
        
        response.addCookie(refreshTokenCookie);
        log.info("Refresh token configurado en la cookie.");
	}
	
    public String refreshAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException, SessionExpiredException {
        // Obtener el refresh token de la cookie
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        
        log.debug("Buscando el refresh token en las cookies...");

        // Buscar la cookie llamada "refresh_token"
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    log.debug("Refresh_token encontrado", refreshToken);
                    break;
                }
            }
        }

        // Si no se encuentra el refresh token, devolver error
        if (refreshToken == null) {
        	String error = "No se encontró el refresh token en las cookies";
        	log.error(error);
            throw new SessionExpiredException(419, error );
        }

        // Verificar si el refresh token está expirado, si lo está llama al método de logout para redirigir al login
        log.debug("Verficando si el token ha expirado...");
        if (jwtUtil.isTokenExpired(refreshToken)) {
            log.warn("El refresh token ha expirado. Lanzando SessionExpiredException.");
            logout(request, response); // opcional, si quieres limpiar la cookie igualmente
            throw new SessionExpiredException(419, "El refresh token ha expirado. La sesión ha finalizado.");
        }

        // Si el refresh token es válido, obtener el username y generar un nuevo access token
        String email = jwtUtil.extractEmail(refreshToken);
        log.debug("Refresh token válido. Generando nuevo access token para el usuario {}", email);
        
        String newAccessToken = jwtUtil.generateAccessToken(email);
        log.info("Nuevo access token generado para el usuario {}", email);
        
        return newAccessToken; // Devuelve el nuevo access token
    }
    
    // Método de logout
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Eliminar el refresh token de las cookies
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);  // Evitar acceso JavaScript
        refreshTokenCookie.setSecure(true);    // Asegurarse que solo se envíe por HTTPS
        refreshTokenCookie.setPath("/");       // Asegurarse de que coincida con el path donde se guardó el cookie
        refreshTokenCookie.setMaxAge(0);      // Establecer el tiempo de vida a 0 elimina la cookie
        response.addCookie(refreshTokenCookie); // Eliminar la cookie del cliente
        log.info("Refresh token eliminado de las cookies.");
        
        log.info("Sesión expirada. Redirigiendo al login...");
        response.setStatus(HttpServletResponse.SC_OK);
        return; //Se utiliza return para asegurada que el método termine en este momento
        
        
        //Tengo pensado que el accessToken se almacene en el localStorage a través de vue.js, que será mejor para gestionar el token en las cabeceras
        //Por lo tanto, en el caso de que un usuario quisiera hacer logout, habría que llamar a este método desde vue.js para eliminar el refreshToken
        //de las cookies del navegador, y aparte, en el front con vue.js habría que eliminar el accessToken del localStorage para que el logout
        //sea instantaneo y no se mantenga la sesión mientras dure ese accessToken
    }
    
    
}
