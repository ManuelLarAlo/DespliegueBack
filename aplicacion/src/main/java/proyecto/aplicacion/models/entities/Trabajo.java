package proyecto.aplicacion.models.entities;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Trabajos")
public class Trabajo {
	
	@Id
	@Column(name = "Código")
	private String codigo;
	
	/*
	 * Para hacer el codigo combinando otros campos de la entidad, se podria usar @prepersist
	 * para hacer un metodo que se ejecute antes de que la entidad persista en base de datos
	 * 
	 *     @PrePersist 
	 *     public void generarId() {
	 *       Lógica personalizada para generar un ID único
	 *       this.id = nombre + "-" + fechaCreacion.toString();
	 *     }
	 */
	
	@Column(name = "OT", nullable = false, unique = true)
	private Long otTrabajo;
	
	@Column(name = "Fecha de Inicio")
	private Date fechaInicioTrabajo;
	
	@Column(name = "Fecha Fin")
	private Date fechaFinTrabajo;
	
	@Column(name = "Prioridad")
	private String prioridadTrabajo;
	
	@Column(name = "Descripción")
	private String descripcion;
	
	@ManyToOne
	@JoinColumn(name = "Cliente", nullable = false)
	private Cliente cliente;
	
	@OneToMany(mappedBy = "trabajo", cascade = CascadeType.REMOVE) //Si se elimina un trabajo, se eliminan todas las tareas del trabajo en cascada
	private List<Tarea> tareas;
	
	@ManyToOne
	@JoinColumn(name = "Tipo de trabajo", nullable = true) 
	private TipoTrabajo tipoTrabajo;
	
}
