package proyecto.aplicacion.models.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import proyecto.aplicacion.models.entities.Trabajo;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteDTO {
	
	private Long clienteId;
	
    private String nombre;
    
    private String personaContacto;
    
    @Email
    private String emailContacto;
    
    private String movil;
    
    private String telefono;
    
    private String direccion;
       
    private List<Trabajo> trabajos; //Es más eficiente hacerlo con una lista de ids referenciando a los trabajos,
    								//Ahora mismo no se usa, pero si en un futuro lo necesito usar, seguramente sea mejor cambiarlo

}
