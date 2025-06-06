package proyecto.aplicacion.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.dto.TareaBorradoDTO;
import proyecto.aplicacion.models.dto.TareaDTO;
import proyecto.aplicacion.models.entities.Tarea;
import proyecto.aplicacion.models.entities.Trabajo;
import proyecto.aplicacion.models.entities.Usuario;
import proyecto.aplicacion.repositories.ITareaRepository;
import proyecto.aplicacion.repositories.ITrabajoRepository;
import proyecto.aplicacion.repositories.IUsuarioRepository;
import proyecto.aplicacion.utils.exceptions.MandatoryResourceException;
import proyecto.aplicacion.utils.exceptions.ResourceNotFoundException;

@Service
@Slf4j
public class CustomTareaService {
	
	@Autowired
	private ITareaRepository iTareaRepository;
	
	@Autowired
	private ITrabajoRepository iTrabajoRepository;
	
	@Autowired
	private IUsuarioRepository iUsuarioRepository;
	
	//Implementación del servicio para las notificaciones
	@Autowired
	private CustomNotificacionService customNotificacionService;
	
	public void crearTarea (TareaDTO tareaDTO) throws MandatoryResourceException, ResourceNotFoundException {
		
		//Creamos el objeto Tarea
		Tarea tarea = new Tarea();
	    
		//Validamos que los campos obligatorios no sean nulos ni vacios
		if (tareaDTO.getTarea() == null || tareaDTO.getTarea().isEmpty()) {
	        String error = "Error - El nombre de la tarea es obligatorio";
	        log.error(error);
	        throw new MandatoryResourceException(400, error);
		}
	    
	    //Seteamos los valores después de hacer las validaciones
        tarea.setTarea(tareaDTO.getTarea());
        tarea.setPrioridadTarea(tareaDTO.getPrioridadTarea());
        tarea.setStatus(tareaDTO.getStatus());
        tarea.setNotas(tareaDTO.getNotas());
        tarea.setMostrarCalendario(tareaDTO.getMostrarCalendario());
        tarea.setNotificacionEnviada(tareaDTO.getNotificacionEnviada()); //Se podría permitir que se cambie este valor por si se necesita que la tarea no envíe notificación
        
        
		//Buscamos el trabajo para ver si existe
		Optional<Trabajo> trabajoOpt = iTrabajoRepository.findById(tareaDTO.getTrabajoId());
	    if (!trabajoOpt.isPresent()) {
            String error = "Error - El trabajo con esa ID no existe ";
            log.error(error);
            throw new ResourceNotFoundException(404, error); 
	    }
	    
	    // Buscar el usuario responsable (OBLIGATORIO)
	    if (tareaDTO.getResponsableId() == null) {
	        String error = "Error - Se debe asignar un responsable a la tarea";
	        log.error(error);
	        throw new MandatoryResourceException(400, error);
	    }
		
	    Optional<Usuario> responsableOpt = iUsuarioRepository.findById(tareaDTO.getResponsableId());
	    if (!responsableOpt.isPresent()) {
	        String error = "Error - El usuario responsable con esa ID no existe";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
	    }
	    
	    // Buscar el usuario cliente (OPCIONAL)
	    Usuario cliente = null;
	    if (tareaDTO.getClienteId() != null) {
	        Optional<Usuario> clienteOpt = iUsuarioRepository.findById(tareaDTO.getClienteId());
	        if (!clienteOpt.isPresent()) {
	            String error = "Error - El usuario cliente con esa ID no existe";
	            log.error(error);
	            throw new ResourceNotFoundException(404, error);
	        }
	        cliente = clienteOpt.get();
	    }
	    
	    //Setea el trabajo y el usuario
	    tarea.setTrabajo(trabajoOpt.get());
	    tarea.setResponsable(responsableOpt.get());
	    tarea.setCliente(cliente); //En caso de que no se asigne un cliente, se definirá como null
        
        //Comprobamos que la fecha de inicio de la tarea no sea nula o anterior a la del trabajo asignado
	    if (tareaDTO.getFechaInicioTarea() == null || tareaDTO.getFechaInicioTarea().before(trabajoOpt.get().getFechaInicioTrabajo())) {
            String error = "Error - La fecha de inicio de la tarea no puede ser anterior a la del trabajo asignado";
            log.error(error);
            throw new MandatoryResourceException(400, error); 
	    }
	    //Setea el valor de la fecha de inicio
        tarea.setFechaInicioTarea(tareaDTO.getFechaInicioTarea());
        
        //Comprobamos que la fecha final de finalización de la tarea no sea nula o posterior a la del trabajo asignado
        if (tareaDTO.getFechaFinTarea() == null || tareaDTO.getFechaFinTarea().after(trabajoOpt.get().getFechaFinTrabajo())) {
            String error = "Error - La fecha de finalización de la tarea no puede ser posterior a la del trabajo asignado";
            log.error(error);
            throw new MandatoryResourceException(400, error); 
        }
        //Setea el valor de la fecha de finalización
        tarea.setFechaFinTarea(tareaDTO.getFechaFinTarea());
        
        this.iTareaRepository.saveAndFlush(tarea);
        
        //Se llama al método para envíar una notificación cuando se crea una tarea
        this.customNotificacionService.enviarNotificacionTareaCreada(tareaDTO);
	}
	
	/*
	public List<TareaDTO> listarTareas(Long clienteId, Long responsableId, Integer ot, String prioridad, String status) {
		
	    // Obtener el usuario autenticado
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String username = authentication.getName();
	    
	    //Verificar si el usuario es administrador
	    boolean esAdmin = authentication.getAuthorities().stream()
	                     .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
		
	    // Buscar el usuario autenticado
	    Usuario usuario = this.iUsuarioRepository.findByUsername(username);
	    
	    List<Tarea> tareas;
	    
	    if (esAdmin) {
	    	
	    	tareas = this.iTareaRepository.findAllWithFilters(clienteId, responsableId, ot, prioridad, status);
	    } else {
	    	
	    	tareas = this.iTareaRepository.findByClienteWithFilters(usuario.getUserId(), clienteId, responsableId, ot, prioridad, status);
	    }

	    // Si la lista de tareas está vacía, devolver una lista vacía
	    if (tareas.isEmpty()) {
	        log.info("No se encontraron tareas");
	        return Collections.emptyList();
	    }
		
        List<TareaDTO> tareasDTO = tareas.stream()
                .map(tarea -> {
                    // Mapear cada tarea a TareaDTO
                    TareaDTO tareaDTO = new TareaDTO();
                    tareaDTO.setId(tarea.getId());
                    tareaDTO.setTarea(tarea.getTarea());;
                    tareaDTO.setFechaInicioTarea(tarea.getFechaInicioTarea());
                    tareaDTO.setFechaFinTarea(tarea.getFechaFinTarea());
                    tareaDTO.setPrioridadTarea(tarea.getPrioridadTarea());
                    tareaDTO.setStatus(tarea.getStatus());
                    tareaDTO.setNotas(tarea.getNotas());
                    tareaDTO.setMostrarCalendario(tarea.getMostrarCalendario());
                    tareaDTO.setNotificacionEnviada(tarea.getNotificacionEnviada());
                    tareaDTO.setTrabajoId(tarea.getTrabajo().getCodigo()); 
                    tareaDTO.setResponsableId(tarea.getResponsable().getUserId());
                    tareaDTO.setClienteId(tarea.getCliente().getUserId());
                    return tareaDTO;
                })
                .collect(Collectors.toList());
		
		return tareasDTO;
	}
	*/
	
	//Método igual que el de arriba, pero con un pageable
	public Page<TareaDTO> listarTareas2(Long clienteId, Long responsableId, Integer ot, String prioridad, String status, Pageable pageable) {
		
	    // Obtener el usuario autenticado
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String username = authentication.getName();
	    
	    //Verificar si el usuario es administrador
	    boolean esAdmin = authentication.getAuthorities().stream()
	                     .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
		
	    // Buscar el usuario autenticado
	    Usuario usuario = this.iUsuarioRepository.findByUsername(username);
	    
	    Page<Tarea> tareas;
	    
	    if (esAdmin) {
	    	
	    	tareas = this.iTareaRepository.findAllWithFiltersPage(clienteId, responsableId, ot, prioridad, status, pageable);
	    } else {
	    	
	    	tareas = this.iTareaRepository.findByClienteWithFiltersPage(usuario.getUserId(), clienteId, responsableId, ot, prioridad, status, pageable);
	    }

	    // Si la lista de tareas está vacía, devolver una lista vacía
	    if (tareas.isEmpty()) {
	        log.info("No se encontraron tareas");
	        return Page.empty();
	    }
		
        Page<TareaDTO> tareasDTO = tareas
                .map(tarea -> {
                    // Mapear cada tarea a TareaDTO
                    TareaDTO tareaDTO = new TareaDTO();
                    tareaDTO.setId(tarea.getId());
                    tareaDTO.setTarea(tarea.getTarea());;
                    tareaDTO.setFechaInicioTarea(tarea.getFechaInicioTarea());
                    tareaDTO.setFechaFinTarea(tarea.getFechaFinTarea());
                    tareaDTO.setPrioridadTarea(tarea.getPrioridadTarea());
                    tareaDTO.setStatus(tarea.getStatus());
                    tareaDTO.setNotas(tarea.getNotas());
                    tareaDTO.setMostrarCalendario(tarea.getMostrarCalendario());
                    tareaDTO.setNotificacionEnviada(tarea.getNotificacionEnviada());
                    tareaDTO.setTrabajoId(tarea.getTrabajo().getCodigo()); 
                    tareaDTO.setResponsableId(tarea.getResponsable().getUserId());
                    tareaDTO.setClienteId(tarea.getCliente().getUserId());
                    return tareaDTO;
                });
		
		return tareasDTO;
	}
	
	public void actualizarTarea(Long id, TareaDTO tareaDTO) throws ResourceNotFoundException {
		
		  // Obtener el usuario autenticado
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String username = authentication.getName();
	    boolean esAdmin = authentication.getAuthorities().stream()
	                     .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
	    boolean esUsuario = authentication.getAuthorities().stream()
	                     .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USUARIO"));
		
	    // Busca la tarea a actualizar por su id
	    Optional<Tarea> tareaOpt = iTareaRepository.findById(id);
	    if (!tareaOpt.isPresent()) {
	        String error = "Error - No se ha encontrado una tarea con esa ID";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
	    }
		
	    //Abre el objeto Tarea
	    Tarea tareaExistente = tareaOpt.get();
		
	 // Si el usuario es ADMIN, puede modificar todos los campos
	    if (esAdmin) {
	        // Modificar todos los campos si es ADMIN
	        if (tareaDTO.getTarea() != null && !tareaDTO.getTarea().isEmpty()) {
	            tareaExistente.setTarea(tareaDTO.getTarea());
	        }

	        if (tareaDTO.getFechaInicioTarea() != null) {
	            tareaExistente.setFechaInicioTarea(tareaDTO.getFechaInicioTarea());
	        }

	        if (tareaDTO.getFechaFinTarea() != null) {
	            tareaExistente.setFechaFinTarea(tareaDTO.getFechaFinTarea());
	        }

	        if (tareaDTO.getPrioridadTarea() != null && !tareaDTO.getPrioridadTarea().isEmpty()) {
	            tareaExistente.setPrioridadTarea(tareaDTO.getPrioridadTarea());
	        }

	        if (tareaDTO.getStatus() != null && !tareaDTO.getStatus().isEmpty()) {
	            tareaExistente.setStatus(tareaDTO.getStatus());
	        }

	        if (tareaDTO.getNotas() != null && !tareaDTO.getNotas().isEmpty()) {
	            tareaExistente.setNotas(tareaDTO.getNotas());
	        }

	        //El objeto Boolean si puede tener valor nulo, por eso tenemos que validarlo
	        if (tareaDTO.getMostrarCalendario() != null) {
	            tareaExistente.setMostrarCalendario(tareaDTO.getMostrarCalendario());
	        }
	        
	        //Se podría permitir actualizar este campo en caso de que no querer que la tarea envíe una notificación
	        if (tareaDTO.getNotificacionEnviada() != null) {
	        	tareaExistente.setNotificacionEnviada(tareaDTO.getNotificacionEnviada());
	        }

	        // Actualiza el trabajo asociado a la tarea
	        if (tareaDTO.getTrabajoId() != null) {
	            Optional<Trabajo> trabajoOpt = iTrabajoRepository.findById(tareaDTO.getTrabajoId());
	            if (!trabajoOpt.isPresent()) {
	                String error = "Error - No se han encontrado trabajos";
	                log.error(error);
	                throw new ResourceNotFoundException(404, error);
	            }
	            tareaExistente.setTrabajo(trabajoOpt.get());
	        }

	        // Actualiza el usuario responsable de la tarea
	        if (tareaDTO.getResponsableId() != null) {
	            Optional<Usuario> responsableOpt = iUsuarioRepository.findById(tareaDTO.getResponsableId());
	            if (!responsableOpt.isPresent()) {
	                String error = "Error - No se ha encontrado ningún usuario con esa ID";
	                log.error(error);
	                throw new ResourceNotFoundException(404, error);
	            }
	            tareaExistente.setResponsable(responsableOpt.get());
	        }

	        // Actualiza el usuario cliente de la tarea
	        if (tareaDTO.getClienteId() != null) {
	            Optional<Usuario> clienteOpt = iUsuarioRepository.findById(tareaDTO.getClienteId());
	            if (!clienteOpt.isPresent()) {
	                String error = "Error - No se ha encontrado ningún usuario con esa ID";
	                log.error(error);
	                throw new ResourceNotFoundException(404, error);
	            }
	            tareaExistente.setCliente(clienteOpt.get());
	        }

	    } 
	    // Si el usuario es USUARIO, solo puede modificar el status de las tareas asignadas a él
	    else if (esUsuario) {
	        // Verificar si el cliente es el responsable de la tarea antes de permitir la modificación
	        if (!tareaExistente.getCliente().getUsername().equals(username)) {
	            String error = "Error - Este cliente no tiene acceso para modificar esta tarea.";
	            log.error(error);
	            throw new ResourceNotFoundException(403, error); // 403: Forbidden
	        }

	        // Solo permite modificar el status
	        if (tareaDTO.getStatus() != null && !tareaDTO.getStatus().isEmpty()) {
	            tareaExistente.setStatus(tareaDTO.getStatus());
	        } else {
	            String error = "Error - El status es obligatorio para los clientes.";
	            log.error(error);
	            throw new ResourceNotFoundException(400, error); // 400: Bad Request
	        }
	    }
    	
    	this.iTareaRepository.saveAndFlush(tareaExistente);
    	
    	//Se llama al método para enviar una notificación cuandose actualiza una tarea
    	customNotificacionService.enviarNotificacionTareaCreada(tareaDTO);
	}
	
	public void borrarTarea(TareaBorradoDTO tareaBorradoDTO) throws ResourceNotFoundException {
		
		//Busca la tarea por su id
		Optional<Tarea> tareaOpt = this.iTareaRepository.findById(tareaBorradoDTO.getId());
		if (!tareaOpt.isPresent()) {
	        String error = "Error - No se ha encontrado una tarea con esa ID";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
		}
		log.info("La tarea se ha eliminado correctamente");
		this.iTareaRepository.delete(tareaOpt.get());
	}
	
}
