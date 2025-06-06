package proyecto.aplicacion.models.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "Usuarios")
public class Usuario {
	
	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;
	
	@Column(name = "Email", nullable = false, unique = true)
	private String email;
	
	@Column(name = "Nombre", nullable = false, unique = true)
	private String username;
	
	@Column(name = "Contraseña", nullable = false)
	private String password;
	
    @OneToMany(mappedBy = "responsable")
    @JsonIgnore // Evita la serialización recursiva
    private List<Tarea> tareasResponsable;
    
    @OneToMany(mappedBy = "cliente")
    @JsonIgnore  // Evita la serialización recursiva
    private List<Tarea> tareasCliente;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Rol rol;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.REMOVE)
    private List<Notificacion> notificaciones;
}
