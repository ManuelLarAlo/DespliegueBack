package proyecto.aplicacion.models.entities;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Tareas")
public class Tarea {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id")
	private Long id;
	
	@Column(name = "Tarea", nullable = false)
	private String tarea;
	
	@Column(name = "Fecha de Inicio")
	private Date fechaInicioTarea;
	
	@Column(name = "Fecha Fin")
	private Date fechaFinTarea;
	
	@Column(name = "Prioridad")
	private String prioridadTarea;
	
	@Column(name = "Status")
	private String status;
	
	@Column(name = "Notas")
	private String notas;
	
	//Se necesita que sean objetos Boolean en vez de tipos primitivos para poder usar el valor nulo en caso de actualización o si se necesita que la tarea no envíe una notificación
	@Column(name = "Mostrar en calendario")
	private Boolean mostrarCalendario;
	
    @Column(name = "Notificacion Enviada")
    private Boolean notificacionEnviada;
    
    @Column(name = "Expirada")
    private Boolean expirada;
	
	@ManyToOne
	@JoinColumn(name="Trabajo")
	private Trabajo trabajo;
	
	@ManyToOne
	@JoinColumn(name = "responsable_id") // El responsable es opcional
	private Usuario responsable;
	
	@ManyToOne
	@JoinColumn(name = "cliente_id") // El cliente es opcional
	private Usuario cliente;

}
