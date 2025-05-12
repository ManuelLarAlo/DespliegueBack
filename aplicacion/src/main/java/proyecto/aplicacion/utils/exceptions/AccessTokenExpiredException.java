package proyecto.aplicacion.utils.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class AccessTokenExpiredException extends Exception
{
    /**
     * Auto-generated ID
     */
    private static final long serialVersionUID = 8144321039123138736L;

    private int id;
    
    private String message;
    
    private Exception exception;

    // Constructor completo
    public AccessTokenExpiredException(int id, String message, Exception exception) {
        super();
        this.id = id;
        this.message = message;
        this.exception = exception;
    }

    // Constructor sin la Excepción
    public AccessTokenExpiredException(int id, String message) {
        super();
        this.id = id;
        this.message = message;
    }

    // Método que devuelve un mapa con la excepción propia
    public Map<String, String> getMapError()
    {
        Map<String, String> mapError = new HashMap<>();

        mapError.put("id", "" + id);
        mapError.put("message", message);

        if (this.exception != null) {
            String stacktrace = ExceptionUtils.getStackTrace(this.exception);
            mapError.put("exception", stacktrace);
        }

        return mapError;
    }
}