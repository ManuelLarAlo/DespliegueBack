package proyecto.aplicacion.models.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Clientes")
public class Cliente {
	
	@Id
	@Column
	private Long clienteId;
	
	@Column(name = "Nombre", nullable = false)
	private String nombre;
	
	@Column(name = "Persona de contacto")
	private String personaContacto;
	
	@Column(name = "Email de contacto")
	private String emailContacto;
	
	@Column(name = "Móvil de contacto")
	private String movil;
	
	@Column(name = "Teléfono de contacto")
	private String telefono;
	
	@Column(name = "Dirección")
	private String direccion;
	
	@OneToMany(mappedBy = "cliente", cascade = CascadeType.REMOVE) //Si se elimina un cliente, se eliminan todos los trabajos de ese cliente en cascada
	private List<Trabajo> trabajos;

}
