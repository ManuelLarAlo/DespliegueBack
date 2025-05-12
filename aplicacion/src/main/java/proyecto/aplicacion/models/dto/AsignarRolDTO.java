package proyecto.aplicacion.models.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignarRolDTO {
	
	@Email
    private String email; 

    private Long rolId;     

}

