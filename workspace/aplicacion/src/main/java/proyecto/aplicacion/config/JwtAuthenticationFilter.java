package proyecto.aplicacion.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import proyecto.aplicacion.services.AuthService;
import proyecto.aplicacion.services.CustomUserDetailsService;
import proyecto.aplicacion.utils.JwtUtil;
import proyecto.aplicacion.utils.exceptions.SessionExpiredException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
    private final JwtUtil jwtUtil;
    
    private final AuthService authService;
    
    private final CustomUserDetailsService customUserDetailsService;


    // Constructor para inyectar JwtUtil y AuthService
    public JwtAuthenticationFilter(JwtUtil jwtUtil, AuthService authService, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.customUserDetailsService = customUserDetailsService;
    }
    
	//Ya se maneja con el permitAll() en el SecurityConfig
    /*Si la ruta es pública, continuamos sin validar el JWT
     if (request.getRequestURI().startsWith("/login") || request.getRequestURI().startsWith("/registro")) {
         filterChain.doFilter(request, response);
         return;
     }
     */
 	
 	/*
 	 * El filtro JWT se aplica por defecto a todas las rutas de la aplicación, aunque tengas un permitall() en la configuración de la seguridad,
 	 * con este código, aseguramos excluir las rutas a las que los usuarios deberían poder acceder sin autenticación, que son las rutas 
 	 * de login, registro y logout, todas las demás rutas se mantienen bajo el filtro.
 	 * */

    /*
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


    	String path = request.getRequestURI();
    	if (path.equals("/A3com/auth/login") || path.equals("/A3com/auth/registro") || path.equals("/A3com/auth/logout")) {
    	    filterChain.doFilter(request, response);
    	    return;
    	}
    	
        // Extraer el token de la cabecera 'Authorization'
        String accessToken = extractToken(request);

        if (accessToken != null && jwtUtil.validateToken(accessToken, jwtUtil.extractEmail(accessToken))) {
            // Si el token es válido, procedemos con la autenticación
            String email = jwtUtil.extractEmail(accessToken);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            // Si el token ha expirado, intentar refrescarlo usando el refresh token
            try {
                // Llamar al método refreshAccessToken para obtener un nuevo access token
                String newAccessToken = authService.refreshAccessToken(request, response);

                // Añadir el nuevo access token en la cabecera de la respuesta para el cliente
                response.setHeader("Authorization", "Bearer " + newAccessToken);

                // Reintentar la autenticación con el nuevo access token
                String email = jwtUtil.extractEmail(newAccessToken);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(email, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (SessionExpiredException e) {
                // Si el refresh token también ha expirado o hay un problema, devolver un error
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Refresh token expirado o inválido");
                return;
            }
        }

        // Continuamos con la cadena de filtros
        filterChain.doFilter(request, response);
    }
    */
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.equals("/A3com/auth/login") || path.equals("/A3com/auth/registro") || path.equals("/A3com/auth/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token de la cabecera 'Authorization'
        String accessToken = extractToken(request);

        if (accessToken != null && jwtUtil.validateToken(accessToken, jwtUtil.extractEmail(accessToken))) {
        	
        	if (jwtUtil.isTokenExpired(accessToken)) {
        		try {
					authService.refreshAccessToken(request, response);
					
				} catch (IOException | SessionExpiredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                //response.setStatus(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED); // Código 407
                //response.getWriter().write("Token expirado o inválido");
                //return;
        	}
        	
            // Si el token es válido, procedemos con la autenticación
            String email = jwtUtil.extractEmail(accessToken);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            // Si el token es nulo o inválido, devolver un 407 (Request Timeout)
            response.setStatus(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED); // Código 407
            response.getWriter().write("Token expirado o inválido");
            return;
        }

        // Continuamos con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // Obtener el token del header Authorization
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // Extraer token después de "Bearer "
        }
        return null;
    }
}
