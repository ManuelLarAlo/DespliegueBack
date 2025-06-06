package proyecto.aplicacion.services;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.dto.TrabajoBorradoDTO;
import proyecto.aplicacion.models.dto.TrabajoDTO;
import proyecto.aplicacion.models.entities.Cliente;
import proyecto.aplicacion.models.entities.TipoTrabajo;
import proyecto.aplicacion.models.entities.Trabajo;
import proyecto.aplicacion.models.entities.Usuario;
import proyecto.aplicacion.repositories.IClienteRepository;
import proyecto.aplicacion.repositories.ITipoTrabajoRepository;
import proyecto.aplicacion.repositories.ITrabajoRepository;
import proyecto.aplicacion.repositories.IUsuarioRepository;
import proyecto.aplicacion.utils.exceptions.MandatoryResourceException;
import proyecto.aplicacion.utils.exceptions.ResourceAlreadyExistsException;
import proyecto.aplicacion.utils.exceptions.ResourceNotFoundException;

@Service
@Slf4j
public class CustomTrabajoService {
	
	@Autowired
	private ITrabajoRepository iTrabajoRepository;
	
	@Autowired
	private IClienteRepository iClienteRepository;
	
	@Autowired
	private ITipoTrabajoRepository iTipoTrabajoRepository;
	
	@Autowired
	private IUsuarioRepository iUsuarioRepository;
	
	public void crearTrabajo(TrabajoDTO trabajoDTO) throws MandatoryResourceException, ResourceNotFoundException, ResourceAlreadyExistsException {
		
	    // Crea el objeto trabajo
	    Trabajo trabajo = new Trabajo();
	    
	    //Se hacen 3 validaciones para comprobar que los datos que necesitan ser obligatorios
	    // no sean nulos o estén vacios
	    if (trabajoDTO.getOtTrabajo() == null || trabajoDTO.getOtTrabajo() < 0) {
	        String error = "Error - El OT del trabajo es obligatorio";
	        log.error(error);
	        throw new MandatoryResourceException(400, error);
	    }
	    
	    if (trabajoDTO.getTipoTrabajoId() == null) {
	        String error = "Error - El tipo de trabajo es obligatorio";
	        log.error(error);
	        throw new MandatoryResourceException(400, error);
	    }
	    
	    if (trabajoDTO.getClienteId() == null) {
	        String error = "Error - El cliente es obligatorio";
	        log.error(error);
	        throw new MandatoryResourceException(400, error);
	    }
	    
	    // Obtiene el cliente
	    Optional<Cliente> clienteOpt = this.iClienteRepository.findById(trabajoDTO.getClienteId());  
	    if (!clienteOpt.isPresent()) {
	        String error = "Error - Cliente no encontrado";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
	    }
	    
	    //Obtiene el tipo de trabajo
	    Optional<TipoTrabajo> tipoTrabajoOpt = this.iTipoTrabajoRepository.findById(trabajoDTO.getTipoTrabajoId());
	    if (!tipoTrabajoOpt.isPresent()) {
	        String error = "Error - Tipo de trabajo no encontrado";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
	    }
	    
	    //Setea los tres valores comprobados con anterioridad
	    trabajo.setOtTrabajo(trabajoDTO.getOtTrabajo());
	    trabajo.setTipoTrabajo(tipoTrabajoOpt.get());
	    trabajo.setCliente(clienteOpt.get());
	    
	    
	    //Esto podría no hacer falta ya que se puede gestionar en parte desde el front
	    // Crear código del trabajo a partir de los valores anteriores
	    String codigoCompleto = trabajoDTO.getOtTrabajo() + "-" + tipoTrabajoOpt.get().getTipoTrabajo() + "-" + clienteOpt.get().getNombre();
		
	    //Se verifica si el trabajo ya existe a partir del codigo que acabamos de crear
	    if (this.iTrabajoRepository.existsById(codigoCompleto)) {
	        String errorTrabajo = "Error - El Trabajo ya existe";
	        log.error(errorTrabajo);
	        throw new ResourceAlreadyExistsException(409, errorTrabajo);
	    }
	    
	    //Si todo ha ido bien, setea el codigo
	    trabajo.setCodigo(codigoCompleto);
	    
	    if (trabajoDTO.getFechaFinTrabajo().before(trabajoDTO.getFechaInicioTrabajo()) || trabajoDTO.getFechaFinTrabajo().equals(trabajoDTO.getFechaInicioTrabajo())) {
            String error = "Error - La fecha de finalización del trabajo no puede ser anterior a la fecha de inicio";
            log.error(error);
            throw new MandatoryResourceException(400, error); 
	    }
	    
	    //Setea los valores opcionales del DTO al objeto Trabajo 
	    trabajo.setFechaInicioTrabajo(trabajoDTO.getFechaInicioTrabajo());
	    trabajo.setFechaFinTrabajo(trabajoDTO.getFechaFinTrabajo());
	    trabajo.setPrioridadTrabajo(trabajoDTO.getPrioridadTrabajo());
	    trabajo.setDescripcion(trabajoDTO.getDescripcion());
	    
	    //Guarda el trabajo
	    this.iTrabajoRepository.saveAndFlush(trabajo);
	}
	
	/*
	 * Por cuestiones de escalabilidad de la aplicación, es recomendable que cambie los métodos
	 * de listado de todas las entidades por un pageable en vez de devolver simplemente listas
	 * porque, en caso de que hubiera, por ejemplo, 500 trabajos dados de alta e hicieramos un
	 * findAll, se cargarían los 500 trabajos en la aplicación de golpe y revienta la aplicación
	 */
	/*
	public List<TrabajoDTO> listarTrabajos() {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		
		boolean esAdmin = authentication.getAuthorities().stream()
				.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
		
		Usuario usuario = this.iUsuarioRepository.findByUsername(username);
		
		List<Trabajo> trabajos;
		
		if (esAdmin) {
			
			trabajos = this.iTrabajoRepository.findAll();
			
		} else {
			
			trabajos = this.iTrabajoRepository.findDistinctByTareas_ResponsableOrTareas_Cliente(usuario, usuario);
		}
		
		if (trabajos.isEmpty()) {
	        log.info("No se encontraron trabajos");
	        return Collections.emptyList(); //En el caso de no encontrar clientes, devuelve una lista vacía y no lanza
	        								//una excepción, porque puede ser que no haya clientes dado de alta
		}
		
        // Mapear la lista de trabajos a una lista de TrabajoDTO
        List<TrabajoDTO> trabajosListadoDTO = trabajos.stream()
                .map(trabajo -> {
                    // Crear y llenar el DTO
                    TrabajoDTO dto = new TrabajoDTO();
                    dto.setCodigo(trabajo.getCodigo());
                    dto.setOtTrabajo(trabajo.getOtTrabajo());
                    dto.setFechaInicioTrabajo(trabajo.getFechaInicioTrabajo());
                    dto.setFechaFinTrabajo(trabajo.getFechaFinTrabajo());
                    dto.setPrioridadTrabajo(trabajo.getPrioridadTrabajo());
                    dto.setDescripcion(trabajo.getDescripcion());
                    
                    //Solo se lista el nombre para no exponer toda la entidad
                    if (trabajo.getTipoTrabajo() != null) {
                    	dto.setTipoTrabajoId(trabajo.getTipoTrabajo().getId());
                    }

                    // Solo se lista el nombre para no exponer toda la entidad
                    if (trabajo.getCliente() != null) {
                        dto.setClienteId(trabajo.getCliente().getClienteId());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
		
		return trabajosListadoDTO;
	}
	*/
	
	//Método igual que el de arriba, pero con un pageable para limitar los elementos cargados
	public Page<TrabajoDTO> listarTrabajos2(Pageable pageable) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		
		boolean esAdmin = authentication.getAuthorities().stream()
				.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
		
		Usuario usuario = this.iUsuarioRepository.findByUsername(username);
		
		Page<Trabajo> trabajos;
		
		if (esAdmin) {
			
			trabajos = this.iTrabajoRepository.findAll(pageable);
			
		} else {
			
			trabajos = this.iTrabajoRepository.findDistinctByTareas_ResponsableOrTareas_Cliente(usuario, usuario, pageable);
		}
		
		if (trabajos.isEmpty()) {
	        log.info("No se encontraron trabajos");
	        return Page.empty(); //En el caso de no encontrar clientes, devuelve una lista vacía y no lanza
	        								//una excepción, porque puede ser que no haya clientes dado de alta
		}
		
        // Mapear la lista de trabajos a una lista de TrabajoDTO
        Page<TrabajoDTO> trabajosListadoDTO = trabajos
                .map(trabajo -> {
                    // Crear y llenar el DTO
                    TrabajoDTO dto = new TrabajoDTO();
                    dto.setCodigo(trabajo.getCodigo());
                    dto.setOtTrabajo(trabajo.getOtTrabajo());
                    dto.setFechaInicioTrabajo(trabajo.getFechaInicioTrabajo());
                    dto.setFechaFinTrabajo(trabajo.getFechaFinTrabajo());
                    dto.setPrioridadTrabajo(trabajo.getPrioridadTrabajo());
                    dto.setDescripcion(trabajo.getDescripcion());
                    
                    //Solo se lista el nombre para no exponer toda la entidad
                    if (trabajo.getTipoTrabajo() != null) {
                    	dto.setTipoTrabajoId(trabajo.getTipoTrabajo().getId());
                    }

                    // Solo se lista el nombre para no exponer toda la entidad
                    if (trabajo.getCliente() != null) {
                        dto.setClienteId(trabajo.getCliente().getClienteId());
                    }

                    return dto;
                });
		
		return trabajosListadoDTO;
	}
	
	public void actualizarTrabajo(String codigo, TrabajoDTO trabajoDTO) throws ResourceNotFoundException, MandatoryResourceException, ResourceAlreadyExistsException {
		
		//Busca el trabajo por el codigo proporcionado
		Optional<Trabajo> trabajoOpt = this.iTrabajoRepository.findById(codigo);
		if (!trabajoOpt.isPresent())
		{
	        String errorTrabajo = "Error - El Trabajo con el código indicado no existe";
	        log.error(errorTrabajo);
	        throw new ResourceNotFoundException(404, errorTrabajo);
		}
		
		//Abre el objeto trabajo
		Trabajo trabajoExistente = trabajoOpt.get();
		
	    //Se hacen 3 validaciones para comprobar que los datos que necesitan ser obligatorios
	    // no sean nulos o estén vacios		
	    if (trabajoDTO.getOtTrabajo() == null || trabajoDTO.getOtTrabajo() < 0) {
	        String errorOtTrabajo = "Error - El OT del trabajo es obligatorio";
	        log.error(errorOtTrabajo);
	        throw new MandatoryResourceException(400, errorOtTrabajo);
	    }
	    
	    if (trabajoDTO.getClienteId() == null) {
	        String errorClienteId = "Error - El cliente es obligatorio";
	        log.error(errorClienteId);
	        throw new MandatoryResourceException(400, errorClienteId);
	    }
	    
	    if (trabajoDTO.getTipoTrabajoId() == null) {
	        String errorTipoTrabajo = "Error - El tipo de trabajo es obligatorio";
	        log.error(errorTipoTrabajo);
	        throw new MandatoryResourceException(400, errorTipoTrabajo);
	    }
	    
	    // Obtiene el cliente asociado al trabajo
	    Optional<Cliente> clienteOpt = iClienteRepository.findById(trabajoDTO.getClienteId());  
	    if (!clienteOpt.isPresent()) {
	        String errorCliente = "Error - Cliente no encontrado";
	        log.error(errorCliente);
	        throw new ResourceNotFoundException(404, errorCliente);
	    }
	    
	    //Obtiene el tipo de trabajo asociado al trabajo
	    Optional<TipoTrabajo> tipoTrabajoOpt = this.iTipoTrabajoRepository.findById(trabajoDTO.getTipoTrabajoId());
	    if (!tipoTrabajoOpt.isPresent()) {
	        String errorTipoTrabajo = "Error - Tipo de trabajo no encontrado";
	        log.error(errorTipoTrabajo);
	        throw new MandatoryResourceException(400, errorTipoTrabajo);
	    }
		
		//Seteamos los 3 valores obligatorios si no son nulos o vacios
	    if (trabajoDTO.getOtTrabajo() != null) {
	    	
	        trabajoExistente.setOtTrabajo(trabajoDTO.getOtTrabajo());
	    }
	    
	    if (trabajoDTO.getTipoTrabajoId() != null) {
	    	
	        trabajoExistente.setTipoTrabajo(tipoTrabajoOpt.get());
	    }
	    
	    if (trabajoDTO.getClienteId() != null) {
	    	
	    	trabajoExistente.setCliente(clienteOpt.get());
	    }
	    
	    //Se crea el nuevo codigo del trabajo a partir de los nuevos campos, si alguno ha cambiado
    	String nuevoCodigo = trabajoDTO.getOtTrabajo() + "-" + tipoTrabajoOpt.get().getTipoTrabajo() + "-" + clienteOpt.get().getNombre();
    	if (this.iTrabajoRepository.existsById(nuevoCodigo)) {
	        String errorTrabajo = "Error - El Trabajo ya existe";
	        log.error(errorTrabajo);
	        throw new ResourceAlreadyExistsException(409, errorTrabajo);
    	}
	    
    	trabajoExistente.setCodigo(nuevoCodigo);
	    
    	//Setea los demás valores opcionales de la tarea
	    if (trabajoDTO.getFechaInicioTrabajo() != null) {
	    	
	        trabajoExistente.setFechaInicioTrabajo(trabajoDTO.getFechaInicioTrabajo());
	    }
	    if (trabajoDTO.getFechaFinTrabajo() != null) {
	    	
	        trabajoExistente.setFechaFinTrabajo(trabajoDTO.getFechaFinTrabajo());
	    }
	    if (trabajoDTO.getPrioridadTrabajo() != null) {
	    	
	        trabajoExistente.setPrioridadTrabajo(trabajoDTO.getPrioridadTrabajo());
	    }
	    
	    this.iTrabajoRepository.saveAndFlush(trabajoExistente);
	}
	
	public void borrarTrabajo (TrabajoBorradoDTO trabajoBorradoDTO) throws ResourceNotFoundException {
		
		//Busca el trabajo por su id
		Optional<Trabajo> trabajoOpt = this.iTrabajoRepository.findById(trabajoBorradoDTO.getCodigo());
		if (!trabajoOpt.isPresent()) {
	        String errorCliente = "Error - No se ha encontrado un trabajo con esa ID";
	        log.error(errorCliente);
	        throw new ResourceNotFoundException(404, errorCliente);
		}
		
		//Borra el trabajo de la base de datos
		log.info("El trabajo se ha eliminado correctamente");
		this.iTrabajoRepository.delete(trabajoOpt.get());
	}
}
