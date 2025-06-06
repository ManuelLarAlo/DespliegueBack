package proyecto.aplicacion.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.dto.RolBorradoDTO;
import proyecto.aplicacion.models.dto.RolDTO;
import proyecto.aplicacion.models.entities.Rol;
import proyecto.aplicacion.models.entities.Usuario;
import proyecto.aplicacion.repositories.IRolRepository;
import proyecto.aplicacion.repositories.IUsuarioRepository;
import proyecto.aplicacion.utils.Constants;
import proyecto.aplicacion.utils.exceptions.MandatoryResourceException;
import proyecto.aplicacion.utils.exceptions.ResourceNotFoundException;

@Service
@Slf4j
public class CustomRolService {
	
	@Autowired
	private IRolRepository iRolRepository;
	
	@Autowired
	private IUsuarioRepository iUsuarioRepository;
	
	
	//Está clase probablemente se elimine junto con su controlador porque no creo que la gestión de roles en la aplicación sea necesaria más allá de
	//cambiar los roles de un usuario, el crud sobre los roles no es útil dentro de la aplicación, expcepto quizás el listado, para cargar los roles
	//en un combobox y usarlos en el caso de que un administrador quiera cambiar el rol de un usuario.
	
	//El método para crear un rol es inútil, ya que aunque se pueden crear roles y permisos a partir de métodos,
	//estos no servirán para nada porque no están gestionados en las rutas o en los métodos del sistema.
	//La única solución es inicializar muchos permisos específicos (como a nivel de método) en el sistema y mantener el método
	//de crear un rol añadiendole todos los permisos que se necesiten para tener acceso a un método u otro
	
	
	public void crearRol(RolDTO rolDTO) throws MandatoryResourceException {
		
		//Crea el objeto rol
		Rol rol = new Rol();
		
		//Valida que el nombre del rol no sea nulo o vacío
		if (rolDTO.getRolNombre() == null || rolDTO.getRolNombre().isEmpty()) {
	        String errorRol = "Error - El nombre del rol es obligatorio";
	        log.error(errorRol);
	        throw new MandatoryResourceException(400, errorRol);
		}
		
		//Setea el nombre del rol
		rol.setNombre(rolDTO.getRolNombre());
		
		this.iRolRepository.saveAndFlush(rol);
	}
	
	public List<RolDTO> listarRoles() throws ResourceNotFoundException {
		
		//Aquí, al contrario que en los demás listados, se lanza una excepción porque hay
		//roles que se generan y se guardan en base de datos cuando se lanza la aplicación
		List<Rol> roles = iRolRepository.findAll();
        if (roles.isEmpty()) {
	        String error = "Error - No se ha encontrado ningún rol";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
        }
        
        // Mapear la lista de Roles a una lista de RolesDTO
        List<RolDTO> rolesDTO = roles.stream()
                .map(rol -> {
                    // Crear y llenar el DTO
                    RolDTO dto = new RolDTO();
                    dto.setRolNombre(rol.getNombre());
                    return dto;
                })
                .collect(Collectors.toList());
		
		return rolesDTO;
	}
	
	public void actualizarRol(Long rolId, RolDTO rolDTO) throws ResourceNotFoundException {
		
		//Busca el rol por la id proporcionada
		Optional<Rol> rolOpt = iRolRepository.findById(rolId);
		if (!rolOpt.isPresent()) {
	        String error = "Error - No se han encontrado roles";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
		}
		
		//Abre el objeto Rol
		Rol rolExistente = rolOpt.get();
		
		if (rolDTO.getRolNombre() != null && !rolDTO.getRolNombre().isEmpty()) {
			rolExistente.setNombre(rolDTO.getRolNombre());
		}
		
		iRolRepository.saveAndFlush(rolExistente);
	}
	
	public void borrarRol(RolBorradoDTO rolBorradoDTO) throws ResourceNotFoundException {
		
		//Busca el rol por su ID
		Optional<Rol> rolOpt = this.iRolRepository.findById(rolBorradoDTO.getRolId());
		if (!rolOpt.isPresent()) {
	        String errorCliente = "Error - No se ha encontrado un rol con esa ID";
	        log.error(errorCliente);
	        throw new ResourceNotFoundException(404, errorCliente);
		}
		
		Rol rol = rolOpt.get();
		
	    // Buscar el rol de "cliente" en la base de datos
	    Rol rolCliente = this.iRolRepository.findByNombre(Constants.ROLE_USER);
	    if (rolCliente == null) {
	        String errorCliente = "Error - No se ha encontrado el rol de cliente";
	        log.error(errorCliente);
	        throw new ResourceNotFoundException(404, errorCliente);
	    }
	    
	    List<Usuario> usuarios = new ArrayList<Usuario>();
		
		for (Usuario usuario : rol.getUsuarios()) {
			usuario.setRol(rolCliente);
			usuarios.add(usuario);
		}
		//Se usa un saveall para menos carga de trabajo
		this.iUsuarioRepository.saveAllAndFlush(usuarios);
		
		//Borra el rol de la base de datos
		log.info("El rol se ha eliminado correctamente");
		this.iRolRepository.delete(rolOpt.get());
	}
}
