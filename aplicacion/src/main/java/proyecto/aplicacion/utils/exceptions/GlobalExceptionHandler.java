package proyecto.aplicacion.utils.exceptions;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Maneja la excepción personalizada 'ResourceNotFoundException'
     */ 
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        // Obtener detalles del error de la excepción
        Map<String, String> errorDetails = ex.getMapError();

        // Retornar la respuesta con el código de estado 404 y los detalles del error
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }
    
    /*
     * Maneja la excepción personalizada 'ResourceAlreadyExistsException'
     */ 
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        // Obtener detalles del error de la excepción
        Map<String, String> errorDetails = ex.getMapError();

        // Retornar la respuesta con el código de estado 409 y los detalles del error
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }
    
    /*
     * Maneja la excepción personalizada 'MandatoryResourceException'
     */ 
    @ExceptionHandler(MandatoryResourceException.class)
    public ResponseEntity<Map<String, String>> handleMandatoryResourceException(MandatoryResourceException ex) {
        // Obtener detalles del error de la excepción
        Map<String, String> errorDetails = ex.getMapError();

        // Retornar la respuesta con el código de estado 400 y los detalles del error
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
    
    /*
     * Maneja la excepción personalizada 'ForbiddenException'
     */ 
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbiddenException(ForbiddenException ex) {
        // Obtener detalles del error de la excepción
        Map<String, String> errorDetails = new HashMap<>();

        // Retornar la respuesta con el código de estado 403 y los detalles del error
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }
    
    /*
     * Maneja la excepción personalizada 'UnauthorizedException'
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedException(UnauthorizedException ex) {
        // Obtener detalles del error de la excepción
        Map<String, String> errorDetails = ex.getMapError();

        // Retornar la respuesta con el código de estado 401 (Unauthorized) y los detalles del error
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
    
    /*
     * Maneja la excepción personalizada 'SessionExpiredException'
     */
    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<Map<String, String>> handleSessionExpiredException(SessionExpiredException ex) {
        // Obtener detalles del error de la excepción
        Map<String, String> errorDetails = ex.getMapError();

        // Retornar la respuesta con el código de estado 419 (Session expired) y los detalles del error
        return new ResponseEntity<>(errorDetails, HttpStatus.valueOf(419));
    }
    
    /*
     * Maneja la excepción personalizada 'AccessTokenExpiredException'
     */
    @ExceptionHandler(AccessTokenExpiredException.class)
    public ResponseEntity<Map<String, String>> handleSessionExpiredException(AccessTokenExpiredException ex) {
        // Obtener detalles del error de la excepción
        Map<String, String> errorDetails = ex.getMapError();

        // Retornar la respuesta con el código de estado 407 (Access token expired) y los detalles del error
        return new ResponseEntity<>(errorDetails, HttpStatus.valueOf(407));
    }

    /*
     * Maneja excepciones generales (cualquier otra excepción no específica)
     */ 
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("message", "Ocurrió un error interno en el servidor.");
        errorDetails.put("error", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
