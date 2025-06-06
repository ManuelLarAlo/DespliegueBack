package proyecto.aplicacion.models.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrabajoDTO {
    
	private String codigo;
	
    private Long otTrabajo;
    
    private Date fechaInicioTrabajo;
    
    private Date fechaFinTrabajo;
    
    private String prioridadTrabajo;
    
    private String herramienta;
    
    private String tipoCliente;
    
    private String descripcion;
    
    private String comercial;
    
    private Long clienteId;
    
    private Long tipoTrabajoId;

}
