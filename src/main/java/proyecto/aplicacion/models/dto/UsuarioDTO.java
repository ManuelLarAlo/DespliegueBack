package proyecto.aplicacion.models.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {
	
	private Long userId;
	
	@Email
	private String email;
	
	private String username;
	
	private String password;
	
	private String rolNombre;

}
