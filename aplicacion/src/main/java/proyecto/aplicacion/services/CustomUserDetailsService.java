package proyecto.aplicacion.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.dto.AsignarRolDTO;
import proyecto.aplicacion.models.dto.UsuarioBorradoDTO;
import proyecto.aplicacion.models.dto.UsuarioDTO;
import proyecto.aplicacion.models.entities.Rol;
import proyecto.aplicacion.models.entities.Tarea;
import proyecto.aplicacion.models.entities.Usuario;
import proyecto.aplicacion.repositories.IRolRepository;
import proyecto.aplicacion.repositories.ITareaRepository;
import proyecto.aplicacion.repositories.IUsuarioRepository;
import proyecto.aplicacion.utils.Constants;
import proyecto.aplicacion.utils.exceptions.ForbiddenException;
import proyecto.aplicacion.utils.exceptions.MandatoryResourceException;
import proyecto.aplicacion.utils.exceptions.ResourceAlreadyExistsException;
import proyecto.aplicacion.utils.exceptions.ResourceNotFoundException;

@Service
@Slf4j
@ComponentScan
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private IUsuarioRepository iUsuarioRepository;
    
    @Autowired
    private IRolRepository iRolRepository;
    
    @Autowired
    private ITareaRepository iTareaRepository;
    
	@Autowired
	private PasswordEncoder passwordEncoder;
    
	//Metodo deprecado
	/*
    public boolean existsByUsername(String correo)
    {
        // Buscar el usuario en la base de datos
        Optional<Usuario> usuarioOpt = this.iUsuarioRepository.findByEmail(correo);
        return usuarioOpt.isPresent();
    }
    */
	
    public void validarEmail(String email) throws ResourceAlreadyExistsException {
    	log.debug("Verificando si el correo {} ya está registrado.", email);
        // Verificar si el correo ya está registrado
        if (this.iUsuarioRepository.findByEmail(email).isPresent()) {
            String error = "Error - El correo electrónico ya está registrado.";
            log.error(error);
            throw new ResourceAlreadyExistsException(409, error); // Lanza una excepción si ya existe el correo
        }
        log.debug("El correo {} está disponible para registro.", email);
    }
    
    //Metodo para registro del usuario
    public void crearUsuario(@Valid UsuarioDTO usuarioDTO) throws MandatoryResourceException, ResourceNotFoundException{
    	log.debug("Iniciando el proceso de creación de usuario con nombre de usuario: {}", usuarioDTO.getUsername());
    	
    	//Crea el objeto Usuario
    	Usuario usuario = new Usuario();
    	
    	//Valida que el correo no sea nulo ni vacio
    	if (usuarioDTO.getEmail() == null || usuarioDTO.getEmail().isEmpty()) {
		    String error = "Error - El correo electrónico del usuario es obligatorio";
		    log.error(error);
		    throw new MandatoryResourceException(400, error); 
    	}
    	
    	//Valida que el nombre de usuario no sea nulo ni vacio
    	if (usuarioDTO.getUsername() == null || usuarioDTO.getUsername().isEmpty()) {
		    String error = "Error - El nombre de usuario es obligatorio";
		    log.error(error);
		    throw new ResourceNotFoundException(404, error); 
    	}
		
    	//Valida que la contraseña cumpla con los requisitos de seguridad
		if (!validarPass(usuarioDTO.getPassword())) {
		    String error = "Error - La contraseña no cumple con los requisitos de seguridad.";
		    log.error(error);
		    throw new MandatoryResourceException(404, error); 
		}
		
		//Cifra la contraseña con BCrypt
		String encodedPass = this.passwordEncoder.encode(usuarioDTO.getPassword());
		log.debug("La contraseña ha sido cifrada correctamente para el usuario {}", usuarioDTO.getUsername());
		
		//Se setean los valores comprobados con anterioridad
		usuario.setEmail(usuarioDTO.getEmail());
		usuario.setUsername(usuarioDTO.getUsername());
		usuario.setPassword(encodedPass);
		
		//A cada usuario que se registra, se le añade el rol "USUARIO" por defecto
		//solo los usuarios con el rol administrador tienen permisos para cambiar
		//el rol de los usuarios
		 
		log.debug("Verificando existencia del rol 'USUARIO'");
		Rol rol = this.iRolRepository.findByNombre(Constants.ROLE_USER);
		if (rol == null) {
	        String error = "Error - El rol 'USUARIO' no existe.";
	        log.error(error);
	        throw new MandatoryResourceException(400, error);
		}
	    
		usuario.setRol(rol);
		log.debug("Rol 'Usuario' asignado al usuario {} correctamente", usuarioDTO.getUsername());
		
		this.iUsuarioRepository.saveAndFlush(usuario);
		log.debug("Usuario {} registrado correctamente", usuarioDTO.getUsername());
		
    }
    
    /*
    public List<UsuarioDTO> listarUsuarios() throws ResourceNotFoundException {
    	log.debug("Iniciando el proceso de listado de usuarios");
    	
    	List<Usuario> usuarios = iUsuarioRepository.findAll();
    	
		//Esta validación, al contrario que en los otros métodos de listado, si debe lanzar
		//una excepción, ya que debe de haber, al menos, un usuario dado de alta en la aplicación
		//para que se pueda usar
		if (usuarios == null || usuarios.isEmpty()) {
	        String error = "Error - No se ha encontrado ningún usuario dado de alta en la aplicación";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
		}
		
		log.debug("Se han encontrado {} usuarios en la base de datos", usuarios.size());
		
		List<UsuarioDTO> usuariosDTO = usuarios.stream()
				.map(usuario -> {
					UsuarioDTO dto = new UsuarioDTO();
					dto.setCorreo(usuario.getEmail());
					dto.setUsername(usuario.getUsername());
					dto.setRolNombre(usuario.getRol().getNombre());
					return dto;
				})
				.collect(Collectors.toList());
		
		log.debug("Listado de usuarios creado correctamente con {} elementos", usuariosDTO.size());
    	return usuariosDTO;
    }
    */
    
    //Método igual que el de arriba, pero este usa un pageable para paginar los resultados de 30 en 30
    public Page<UsuarioDTO> listarUsuarios2(Pageable pageable) throws ResourceNotFoundException {
    	log.debug("Iniciando el proceso de listado de usuarios con paginación");
    	
    	//Se obtienen los usuarios paginados
    	Page<Usuario> usuarios = iUsuarioRepository.findAll(pageable);
    	
		//Esta validación, al contrario que en los otros métodos de listado, si debe lanzar
		//una excepción, ya que debe de haber, al menos, un usuario dado de alta en la aplicación
		//para que se pueda usar
		if (usuarios == null || usuarios.isEmpty()) {
	        String error = "Error - No se ha encontrado ningún usuario dado de alta en la aplicación";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
		}
		
		log.debug("Se han encontrado {} usuarios en la base de datos", usuarios.getTotalElements());
		
		Page<UsuarioDTO> usuariosDTO = usuarios
				.map(usuario -> {
					UsuarioDTO dto = new UsuarioDTO();
					dto.setEmail(usuario.getEmail());
					dto.setUsername(usuario.getUsername());
					dto.setRolNombre(usuario.getRol().getNombre());
					return dto;
				});
		
		log.debug("Listado de usuarios creado correctamente con {} elementos", usuariosDTO.getTotalElements());
    	return usuariosDTO;
    }
    
    public void actualizarUsuario(@Valid UsuarioDTO usuarioDTO) throws MandatoryResourceException, ResourceNotFoundException, ForbiddenException {
    	log.debug("Iniciando el proceso de actualización del usuario {}", usuarioDTO.getUsername());
    	
    	String correoAutenticado = getCorreoAutenticado();
    	
        // Verificar que el correo del DTO no es nulo ni vacío
    	if (usuarioDTO.getEmail() == null || usuarioDTO.getEmail().isEmpty()) {
            String error = "Error - El correo es obligatorio";
            log.error(error);
            throw new MandatoryResourceException(400, error);
    	}
    	
        // Verificar si el correo del DTO es el mismo que el correo autenticado o si el usuario es un administrador.
        if (!correoAutenticado.equals(usuarioDTO.getEmail()) && !esAdministrador(correoAutenticado)) {
            String error = "Error - No tienes permisos para modificar este usuario.";
            log.error(error);
            throw new ForbiddenException(403, error);
        }
    	
    	//Busca al usuario por el correo
    	Optional <Usuario> usuarioOpt = this.iUsuarioRepository.findByEmail(usuarioDTO.getEmail());
    	if (!usuarioOpt.isPresent()) {
		    String error = "Error - No existe un usuario con ese correo.";
		    log.error(error);
		    throw new ResourceNotFoundException(404, error); 
    	}
    	
    	//Abre el objeto Usuario
    	Usuario usuarioExistente = usuarioOpt.get();
    	
    	//Si se pasa un nombre de usuario, se actualiza
    	if (usuarioDTO.getUsername() != null && !usuarioDTO.getUsername().isEmpty()) {
    		log.info("Actualizando el nombre de usuario a {}",usuarioDTO.getUsername());
    		usuarioExistente.setUsername(usuarioDTO.getUsername());
    	}
    	
    	 //Si se pasa una contraseña, se valida para que cumpla los requisitos de seguridad
    	 //y si todo va bien, se actualiza
    	if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()) {
    		if (!validarPass(usuarioDTO.getPassword())) {
    		    String error = "Error - La contraseña no cumple los requisitos de seguridad";
    		    log.error(error);
    		    throw new MandatoryResourceException(400, error); 
    		}
    		
    		//Se cifra la contraseña
    		String encodedPass = this.passwordEncoder.encode(usuarioDTO.getPassword());
    		log.info("Actualizando la contraseña del usuario {}", usuarioDTO.getUsername());
    		usuarioExistente.setPassword(encodedPass);
    	}
        
        this.iUsuarioRepository.saveAndFlush(usuarioExistente);
        log.info("Usuario con correo {} actualizado correctamente", usuarioDTO.getEmail());
    }
    
    //Arreglar el flujo de borrado de este método
    public void borrarUsuario(UsuarioBorradoDTO usuarioBorradoDTO) throws ResourceNotFoundException, ForbiddenException {
    	log.debug("Iniciando el proceso de eliminación para el usuario con ID: {}", usuarioBorradoDTO.getUserId());
    	
    	String correoAutenticado = getCorreoAutenticado();
    	
    	//Busca el usuario por su ID
    	Optional<Usuario> usuarioOpt = this.iUsuarioRepository.findById(usuarioBorradoDTO.getUserId());
    	if (!usuarioOpt.isPresent()) {
	        String error = "Error - No se ha encontrado un usuario con esa ID";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
    	}
    	
    	log.info("Usuario con ID {} encontrado. Procediendo con la eliminación.", usuarioBorradoDTO.getUserId());
    	Usuario usuario = usuarioOpt.get();
    	
    	if (!correoAutenticado.equals(usuario.getEmail())) {
            String error = "Error - No tienes permisos para eliminar este usuario.";
            log.error(error);
            throw new ForbiddenException(403, error);
    	}
    	
    	if (usuario.getTareasResponsable() != null) {
        	for (Tarea tarea : usuarioOpt.get().getTareasResponsable()) {
        		tarea.setResponsable(null);
        		iTareaRepository.saveAndFlush(tarea);
        	}
    	}
    	if (usuario.getTareasCliente() != null) {
    		for (Tarea tarea : usuarioOpt.get().getTareasCliente()) {
    			tarea.setCliente(null);
    			iTareaRepository.saveAndFlush(tarea);
    		}
    	}
    	
    	//Borra al usuario de la base de datos
    	log.info("Usuario con ID {} eliminado correctamente", usuarioBorradoDTO.getUserId());
    	this.iUsuarioRepository.delete(usuarioOpt.get());
    }
    
    /*
     * Método solo para administradores
     * 
     * Permite cambiar el rol de un usuario concreto
     */
    private void asignarRolAdmin(@Valid AsignarRolDTO asignarRolDTO) throws MandatoryResourceException, ResourceNotFoundException {
    	log.debug("Iniciando el proceso de asignación del rol para el usuario con correo {}", asignarRolDTO.getEmail());
    	
    	//Se verifica que el rol especificado existe
    	Optional<Rol> rolOpt = this.iRolRepository.findById(asignarRolDTO.getRolId());
    	if (!rolOpt.isPresent()) {
            String error = "Error - El rol especificado no existe.";
            log.error(error);
            throw new MandatoryResourceException(400, error);
    	}
    	
    	log.info("Rol con ID {} encontrado: {}", asignarRolDTO.getRolId(), rolOpt.get().getNombre());
    	
    	//Se verifica que el usuario existe a partir del correo
    	Optional<Usuario> usuarioOpt = this.iUsuarioRepository.findByEmail(asignarRolDTO.getEmail());
    	if (!usuarioOpt.isPresent()) {
            String error = "Error - El correo electrónico no está registrado";
            log.error(error);
            throw new ResourceNotFoundException(404, error);
    	}
    	
    	log.info("Usuario con correo {} encontrado.", asignarRolDTO.getEmail());
    	
    	Usuario usuario = usuarioOpt.get();
    	usuario.setRol(rolOpt.get());
    	this.iUsuarioRepository.saveAndFlush(usuario);
    	
    	log.info("Rol {} asignado correctamente al usuario con correo {}", rolOpt.get().getNombre(), asignarRolDTO.getEmail());
    }
    
 // Método para obtener el correo del usuario autenticado desde Spring Security.
    private String getCorreoAutenticado() {
    	log.debug("Intentando obtener el correo del usuario autenticado.");
    	
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
        	log.info("Correo del usuario autenticado {}", authentication.getName());
        	return authentication.getName();
        } else {
        	log.warn("No se pudo obtener el correo del usuario autenticado");
        	return null;
        }
    }

    // Método para verificar si el usuario autenticado es un administrador.
    private boolean esAdministrador(String email) throws ResourceNotFoundException {
    	log.debug("Verificando si el usuario con correo {} es un administrador", email);
    	
        // Obtén el usuario por correo y verifica si tiene el rol de administrador.
        Optional<Usuario> usuarioOpt = iUsuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Rol rol = usuarioOpt.get().getRol();
            if (rol == null) {
    		    String error = "Error - El usuario no tiene un rol específicado";
    		    log.error(error);
    		    throw new ResourceNotFoundException(404, error); 
            }
            
            boolean isAdmin = Constants.ROLE_ADMIN.equals(rol.getNombre());
            log.info("El usuario con correo {} es administrador", email);
            return isAdmin;
        } else {
            String error = "Error - No se encontró un usuario con el correo " + email;
            log.error(error);
            throw new ResourceNotFoundException(404, error);
        }
    }
    
    /**
     * Método para validar que la contraseña tenga la longitud adecuada.
     * @param pass La contraseña a validar.
     * @return true si la contraseña tiene al menos 8 caracteres, false en caso contrario.
     */
	private boolean validarPass(String pass) {
		log.debug("Validando la contraseña proporcionada");
		
	    String regex = "^(?=.*\\d).{8,}$";  // La contraseña debe tener al menos 8 caracteres y al menos un número.
	    boolean esValida = pass.matches(regex);
	    
	    if (esValida) {
	    	log.info("La contraseña es válida, tiene al menos 8 caracteres.");
	    } else {
	    	log.warn("La contraseña debe tener al menos 8 caracteres.");
	    }
	    
	    return esValida;
	}

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    	log.debug("Intentando cargar el usuario con correo {}", email);
    	
    	//Encuentra a un usuario por el correo
    	Optional<Usuario> usuarioOpt = iUsuarioRepository.findByEmail(email);
    	
    	//Si no lo encuentra, lanza un error
        if (usuarioOpt.isEmpty() ) {
		    String error = "Error - Usuario con el correo " + email + " no encontrado";
		    log.error(error); 
            throw new UsernameNotFoundException(error);
        }
        
        //Abre el objeto Usuario
        Usuario usuario = usuarioOpt.get();
        
        log.debug("Usuario encontrado: {}, Correo: {}, Rol: {}", usuario.getUsername(), usuario.getEmail(), usuario.getRol().getNombre());
        
        //Obtiene el nombre del rol que tiene el usuario
        String rolUsuario = usuario.getRol().getNombre();
        log.info("Rol encontrado para el usuario {}: {}", usuario.getUsername(), rolUsuario);
        
        // Convertir los roles en autoridades (autoridades son roles en este caso)
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + rolUsuario)); // Ejemplo: ROLE_ADMIN

        // Puedes agregar más autoridades si es necesario
        if (Constants.ROLE_ADMIN.equals(rolUsuario)) {
        	log.info("El rol del usuario es ADMIN. Se asignan privilegios adicionales.");
            authorities.add(new SimpleGrantedAuthority(Constants.WRITE_PRIVILEGES));
            authorities.add(new SimpleGrantedAuthority(Constants.READ_PRIVILEGES));
            authorities.add(new SimpleGrantedAuthority(Constants.DELETE_PRIVILEGES));
            authorities.add(new SimpleGrantedAuthority(Constants.UPDATE_PRIVILEGES));
            authorities.add(new SimpleGrantedAuthority(Constants.EXECUTE_PRIVILEGES));
        } else if (Constants.ROLE_TEAM.equals(rolUsuario)) {
        	log.info("El rol del usuario es EQUIPO. Se asignan privilegios adicionales.");
            authorities.add(new SimpleGrantedAuthority(Constants.WRITE_PRIVILEGES));
            authorities.add(new SimpleGrantedAuthority(Constants.READ_PRIVILEGES));
            authorities.add(new SimpleGrantedAuthority(Constants.UPDATE_PRIVILEGES));
        }
        else if (Constants.ROLE_USER.equals(rolUsuario)) {
        	log.info("El rol del usuario es CLIENTE, se asignan privilegios de lectura y escritura de estado");
        	authorities.add(new SimpleGrantedAuthority(Constants.STATUS_WRITE_PRIVILEGES));
        	authorities.add(new SimpleGrantedAuthority(Constants.READ_PRIVILEGES));
        }

        // Devolver el objeto UserDetails
        log.debug("Devolviendo detalles del usuario {}", usuario.getUsername());
        return this.getUserDetails(usuario, authorities);
    }
    
    private UserDetails getUserDetails(Usuario usuario, List<GrantedAuthority> authorities) {
    	log.debug("Creando objeto UserDetails para el usuario {}", usuario.getUsername());
    	
    	if (authorities == null || authorities.isEmpty()) {
    		log.warn("El usuario {} no tiene autoridades asignadas", usuario.getUsername());
    	} else {
    		log.debug("EL usuario {} tiene un total de {} autoridades asignadas", authorities.size());
    	}
    	
    	//Creamos un objeto de spring a partir de un objeto usuario propio
    	UserDetails userDetails = new org.springframework.security.core.userdetails.User(
        		usuario.getUsername(),
        		usuario.getPassword(),
                authorities // Pasamos las autoridades al constructor
        );
    	
    	log.debug("Objeto userDetails creado exitosamente para el usuario {}", usuario.getUsername());
    	return userDetails;
    }
}
