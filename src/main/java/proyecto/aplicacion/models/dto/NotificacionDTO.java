package proyecto.aplicacion.models.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionDTO {
	
	private Long id;
	
	private String mensaje;
	
	private boolean leida;
	
	private LocalDateTime fechaCreacion;
	
	private Long usuarioId;

}
