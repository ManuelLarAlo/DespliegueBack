package proyecto.aplicacion.models.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TareaDTO {
	
	private Long id;

    private String tarea;
    
    private String tipoTrabajoTarea;
    
    private Date fechaInicioTarea;
    
    private Date fechaFinTarea;
    
    private String prioridadTarea;
    
    private String status;
    
    private String proveedor;
    
    private String notas;
    
    private Boolean mostrarCalendario;
    
    private Boolean notificacionEnviada; 
    
    private String trabajoId;
    
    private Long responsableId;
    
    private Long clienteId;     

}

