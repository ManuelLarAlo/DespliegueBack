package proyecto.aplicacion.models.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import proyecto.aplicacion.models.entities.Trabajo;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TipoTrabajoDTO {
	
    private Long id;
    
    private String tipoTrabajo;
    
    private String descripcion;
    
    private List<Trabajo> trabajos;
}
