package proyecto.aplicacion.models.entities;

import java.time.LocalDateTime;

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
@Table(name = "Notificaciones")
public class Notificacion {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column
	private Long id;
	
	@Column
	private String mensaje;
	
	@Column
	private boolean leida = false; //Aquí no me interesa usar el Objeto Boolean porque no necesito el estado null
	
	@Column 
	private LocalDateTime fechaCreacion;
	
	@ManyToOne
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;
}
