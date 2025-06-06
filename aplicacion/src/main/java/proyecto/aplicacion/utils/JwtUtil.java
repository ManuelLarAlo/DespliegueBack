package proyecto.aplicacion.utils;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtil {
    
	@Value("${jwt.secret-key}")
	private String SECRET_KEY;
	
	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpiration;
	
	@Value("${jwt.refresh-token-expiration}")
	private long refreshTokenExpiration;
    
    private SecretKey key;
    
    @PostConstruct
    public void init() {
        if (SECRET_KEY == null || SECRET_KEY.isEmpty()) {
            throw new IllegalStateException("SECRET_KEY no está configurada.");
        }
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
    
    // Método para generar el Access Token (JWT)
    public String generateAccessToken(String email) {
        Date expirationDate = new Date(System.currentTimeMillis() + accessTokenExpiration);
        
        log.debug("Generando Access Token para el usuario '{}', que expirará en '{}'.", email, expirationDate);

        return Jwts.builder()
            .subject(email)
            .issuedAt(new Date())
            .expiration(expirationDate)
            .signWith(key)
            .compact();
    }

    // Método para generar el Refresh Token (más largo)
    public String generateRefreshToken(String email) {
        Date expirationDate = new Date(System.currentTimeMillis() + refreshTokenExpiration);
        
        log.debug("Generando Refresh Token para el usuario '{}', que expirará en '{}'.", email, expirationDate);

        return Jwts.builder()
        	.subject(email)
        	.issuedAt(new Date())
        	.expiration(expirationDate)
            .signWith(key)
            .compact();
    }

    // Método para validar el token y obtener los datos del usuario
    public Claims extractClaims(String token) {
    	
    	log.debug("Extrayendo claims del token: {}", token);
    	
        return Jwts.parser()
        	.verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    // Obtener el username (subject) del token
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // Verificar si el token ha caducado
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // Validar si el token es correcto
    public boolean validateToken(String token, String email) {
        return (email.equals(extractEmail(token)) && !isTokenExpired(token));
    }
}
