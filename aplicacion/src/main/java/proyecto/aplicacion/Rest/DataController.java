package proyecto.aplicacion.Rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.dto.ClienteBorradoDTO;
import proyecto.aplicacion.models.dto.ClienteDTO;
import proyecto.aplicacion.models.dto.RolBorradoDTO;
import proyecto.aplicacion.models.dto.RolDTO;
import proyecto.aplicacion.models.dto.TareaBorradoDTO;
import proyecto.aplicacion.models.dto.TareaDTO;
import proyecto.aplicacion.models.dto.TipoTrabajoBorradoDTO;
import proyecto.aplicacion.models.dto.TipoTrabajoDTO;
import proyecto.aplicacion.models.dto.TrabajoBorradoDTO;
import proyecto.aplicacion.models.dto.TrabajoDTO;
import proyecto.aplicacion.models.dto.UsuarioBorradoDTO;
import proyecto.aplicacion.models.dto.UsuarioDTO;
import proyecto.aplicacion.services.CustomClienteService;
import proyecto.aplicacion.services.CustomRolService;
import proyecto.aplicacion.services.CustomTareaService;
import proyecto.aplicacion.services.CustomTipoTrabajoService;
import proyecto.aplicacion.services.CustomTrabajoService;
import proyecto.aplicacion.services.CustomUserDetailsService;
import proyecto.aplicacion.utils.exceptions.ForbiddenException;
import proyecto.aplicacion.utils.exceptions.MandatoryResourceException;
import proyecto.aplicacion.utils.exceptions.ResourceAlreadyExistsException;
import proyecto.aplicacion.utils.exceptions.ResourceNotFoundException;

@Slf4j
@Controller
@RequestMapping(path = "/dashboard")
public class DataController {
	
	//Implementa Servicios
	@Autowired
	private CustomClienteService customClienteService;
	
	@Autowired
	private CustomTrabajoService customTrabajoService;
	
	@Autowired
	private CustomTareaService customTareaService;
	
	@Autowired
	private CustomRolService customRolService;
	
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	@Autowired
	private CustomTipoTrabajoService customTipoTrabajoService;
	
	
	@RequestMapping(value = "/Clientes", method = RequestMethod.POST)
	public ResponseEntity<?> crearCliente(@RequestBody ClienteDTO clienteDTO) throws MandatoryResourceException, ResourceAlreadyExistsException {
			
		this.customClienteService.crearCliente(clienteDTO);
		return new ResponseEntity<>("El cliente se ha creado correctamente", HttpStatus.CREATED);	
	}
	
	@RequestMapping(value = "/Trabajos", method = RequestMethod.POST)
	public ResponseEntity<?> crearTrabajo(@RequestBody TrabajoDTO trabajoDTO) throws MandatoryResourceException, ResourceNotFoundException, ResourceAlreadyExistsException {
	    
		this.customTrabajoService.crearTrabajo(trabajoDTO);
	    return new ResponseEntity<>("El trabajo se ha creado correctamente", HttpStatus.CREATED);
	}

	@RequestMapping(value = "/Tareas", method = RequestMethod.POST)
	public ResponseEntity<?> crearTarea(@RequestBody TareaDTO tareaDTO) throws MandatoryResourceException, ResourceNotFoundException {
        
		this.customTareaService.crearTarea(tareaDTO);
        return new ResponseEntity<>("La tarea se ha creado correctamente", HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "/Roles", method = RequestMethod.POST)
	public ResponseEntity<?> crearRol(@RequestBody RolDTO rolDTO) throws MandatoryResourceException, ResourceAlreadyExistsException {
		
		this.customRolService.crearRol(rolDTO);
		return new ResponseEntity<>("El rol se ha creado correctamente", HttpStatus.CREATED);
	}
	
	/*
	@RequestMapping(value = "/Clientes", method = RequestMethod.GET)
	public ResponseEntity<?> listarClientes() {
		
		return ResponseEntity.ok().body(this.customClienteService.listarClientes());
	}
	*/
	
	//Método igual que el de arriba, pero con pageable
	@RequestMapping(value = "/Clientes", method = RequestMethod.GET)
	public ResponseEntity<?> listarClientes2(@RequestParam(defaultValue = "0") int pageNumber,
											 @RequestParam(defaultValue = "30") int size) {
		
		Pageable pageable = PageRequest.of(pageNumber, size);
		Page<ClienteDTO> clientePage = this.customClienteService.listarClientes2(pageable);
		return ResponseEntity.ok().body(clientePage);
	}
	
	/*
	@PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
	@RequestMapping(value = "/Trabajos", method = RequestMethod.GET)
	public ResponseEntity<?> listarTrabajos() {

        return ResponseEntity.ok().body(this.customTrabajoService.listarTrabajos());
	}
	*/
	
	//Método igual que el de arriba, pero con un pageable
	@PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
	@RequestMapping(value = "/Trabajos", method = RequestMethod.GET)
	public ResponseEntity<?> listarTrabajos2(@RequestParam(defaultValue = "0") int pageNumber,
											 @RequestParam(defaultValue = "30") int size) {
		
		Pageable pageable = PageRequest.of(pageNumber, size);
		Page<TrabajoDTO> trabajoPage = this.customTrabajoService.listarTrabajos2(pageable);
        return ResponseEntity.ok().body(trabajoPage);
	}
	
	/*
	@PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
	@RequestMapping(value = "/Tareas", method = RequestMethod.GET)
	public ResponseEntity<?> listarTareas(@RequestParam(required = false) Long clienteId, 
									      @RequestParam(required = false) Long responsableId,
									      @RequestParam(required = false) Integer ot,
									      @RequestParam(required = false) String prioridad,
									      @RequestParam(required = false) String status) {

		return ResponseEntity.ok().body(this.customTareaService.listarTareas(clienteId, responsableId, ot, prioridad, status));		
	}
	*/
	
	//Igual que el de arriba, pero con un pageable
	@PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
	@RequestMapping(value = "/Tareas", method = RequestMethod.GET)
	public ResponseEntity<?> listarTareas2(@RequestParam(required = false) Long clienteId, 
									      @RequestParam(required = false) Long responsableId,
									      @RequestParam(required = false) Integer ot,
									      @RequestParam(required = false) String prioridad,
									      @RequestParam(required = false) String status,
									      @RequestParam(defaultValue = "0") int pageNumber,
									      @RequestParam(defaultValue = "30") int size) {
		
		Pageable pageable = PageRequest.of(pageNumber, size);
		Page <TareaDTO> tareasPage = this.customTareaService.listarTareas2(clienteId, responsableId, ot, prioridad, status, pageable);
		return ResponseEntity.ok().body(tareasPage);		
	}

	@RequestMapping(value = "/Roles", method = RequestMethod.GET)
	public ResponseEntity<?> listarRoles() throws ResourceNotFoundException {
		
		return ResponseEntity.ok().body(this.customRolService.listarRoles());
	}
	
	@RequestMapping(value = "/Clientes/{clienteId}", method = RequestMethod.PUT)
	public ResponseEntity<?> actualizarCliente(@PathVariable Long clienteId, 
											   @RequestBody ClienteDTO clienteDTO) throws ResourceNotFoundException {
		
		this.customClienteService.actualizarCliente(clienteId, clienteDTO);
		return ResponseEntity.ok().body("El cliente se ha actualizado correctamente");
	}
	
	@RequestMapping(value = "/Trabajos/{codigo}", method = RequestMethod.PUT)
	public ResponseEntity<?> actualizarTrabajo(@PathVariable("codigo") String codigo,
	                                           @RequestBody TrabajoDTO trabajoDTO) throws MandatoryResourceException, ResourceNotFoundException, ResourceAlreadyExistsException {
		
		this.customTrabajoService.actualizarTrabajo(codigo, trabajoDTO);
		return ResponseEntity.ok().body("El trabajo se ha actualizado correctamente");
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
	@RequestMapping(value = "/Tareas/{id}", method = RequestMethod.PUT)
	public ResponseEntity<?> actualizarTarea(@PathVariable Long id, 
	                                         @RequestBody TareaDTO tareaDTO) throws MandatoryResourceException, ResourceNotFoundException {
	    
		this.customTareaService.actualizarTarea(id, tareaDTO);
		return ResponseEntity.ok().body("la tarea se ha actualizado correctamente");
	}
	
	@RequestMapping(value = "/Roles/{rolId}", method = RequestMethod.PUT)
	public ResponseEntity<?> actualizarRol(@PathVariable Long rolId, 
										   @RequestBody RolDTO rolDTO) throws ResourceNotFoundException {
		
		this.customRolService.actualizarRol(rolId, rolDTO);
		return ResponseEntity.ok().body("El rol se ha actualizado correctamente ");
	}
	
	@RequestMapping(value = "/Clientes", method = RequestMethod.DELETE)
	public ResponseEntity<?> borrarCliente(@RequestBody ClienteBorradoDTO clienteBorradoDTO) throws ResourceNotFoundException {
		
		this.customClienteService.borrarCliente(clienteBorradoDTO);
		return ResponseEntity.ok().body("El cliente ha sido borrado correctamente");
	}
	
	@RequestMapping(value = "/Trabajos", method = RequestMethod.DELETE)
	public ResponseEntity<?> borrarTrabajo(@RequestBody TrabajoBorradoDTO trabajoBorradoDTO) throws ResourceNotFoundException {
		
		this.customTrabajoService.borrarTrabajo(trabajoBorradoDTO);
		return ResponseEntity.ok().body("El trabajo ha sido borrado correctamente");
	}
	
	@RequestMapping(value = "/Tareas", method = RequestMethod.DELETE)
	public ResponseEntity<?> borrarTarea(@RequestBody TareaBorradoDTO tareaBorradoDTO) throws ResourceNotFoundException {
		
		this.customTareaService.borrarTarea(tareaBorradoDTO);
		return ResponseEntity.ok().body("La tarea ha sido borrada correctamente");
	}
	
	@RequestMapping(value = "/Roles",method = RequestMethod.DELETE)
	public ResponseEntity<?> borrarRol(@RequestBody RolBorradoDTO rolBorradoDTO) throws ResourceNotFoundException {
		
		this.customRolService.borrarRol(rolBorradoDTO);
		return ResponseEntity.ok().body("El rol ha sido borrado correctamente");
	}
	
	//---------------------------METODOS DE USUARIO------------------------------------
	/*
	@RequestMapping(value = "/Usuarios", method = RequestMethod.GET)
	public ResponseEntity<?> listarUsuarios() throws ResourceNotFoundException {
		
		return ResponseEntity.ok().body(this.customUserDetailsService.listarUsuarios());
	}
	*/
	
	//Método igual que el de arriba, pero con un pageable
	@RequestMapping(value = "/Usuarios", method = RequestMethod.GET)
	public ResponseEntity<?> listarUsuarios2(@RequestParam(defaultValue = "0") int pageNumber,
											 @RequestParam(defaultValue = "30") int size) throws ResourceNotFoundException {
		
		Pageable pageable = PageRequest.of(pageNumber, size);
		Page<UsuarioDTO> usuariosPage = customUserDetailsService.listarUsuarios2(pageable);
		return ResponseEntity.ok().body(usuariosPage);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
	@RequestMapping(value = "/Usuarios/{email}", method = RequestMethod.PUT)
	public ResponseEntity<?> actualizarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) throws MandatoryResourceException, ResourceNotFoundException, ForbiddenException {
		
		this.customUserDetailsService.actualizarUsuario(usuarioDTO);
		return ResponseEntity.ok().body("El usuario se ha actualizado correctamente");
	}
	
	@RequestMapping(value = "/Usuarios", method = RequestMethod.DELETE)
	public ResponseEntity<?> borrarUsuarios(UsuarioBorradoDTO usuarioBorradoDTO) throws ResourceNotFoundException, ForbiddenException {
		
		this.customUserDetailsService.borrarUsuario(usuarioBorradoDTO);
		return ResponseEntity.ok().body("El usuario se ha borrado correctamente");
	}
	
	//---------------------------METODOS DE TIPO DE TRABAJO------------------------------------
	
	@RequestMapping(value = "/TiposTrabajo", method = RequestMethod.POST)
	public ResponseEntity<?> crearTipoTrabajo(@RequestBody TipoTrabajoDTO tipoTrabajoDTO) throws MandatoryResourceException {
		
		this.customTipoTrabajoService.crearTipoTrabajo(tipoTrabajoDTO);
		return ResponseEntity.ok().body("El tipo de trabajo se ha creado correctamente");
	}
	
	/*
	@RequestMapping(value = "/TiposTrabajo", method = RequestMethod.GET)
	public ResponseEntity<?> listarTiposTrabajo() {
		
		return ResponseEntity.ok().body(this.customTipoTrabajoService.listarTiposTrabajo());
	}
	*/
	
	//Método igual que el de arriba, pero con pageable
	@RequestMapping(value = "/TiposTrabajo", method = RequestMethod.GET)
	public ResponseEntity<?> listarTiposTrabajo(@RequestParam(defaultValue = "0") int pageNumber,
												@RequestParam(defaultValue = "30") int size) {
		
		Pageable pageable = PageRequest.of(pageNumber, size);
		Page<TipoTrabajoDTO> tiposTrabajoPage = this.customTipoTrabajoService.listarTiposTrabajo2(pageable);
		return ResponseEntity.ok().body(tiposTrabajoPage);
	}
	
	@RequestMapping(value = "/TiposTrabajo", method = RequestMethod.PUT)
	public ResponseEntity<?> actualizarTipoTrabajo(TipoTrabajoDTO tipoTrabajoDTO) throws ResourceNotFoundException {
		
		this.customTipoTrabajoService.actualizarTipoTrabajo(tipoTrabajoDTO);
		return ResponseEntity.ok().body("El tipo de trabajo se ha actualizado correctamente");
	}
	
	@RequestMapping(value = "/TiposTrabajo", method = RequestMethod.DELETE)
	public ResponseEntity<?> borrarTipoTrabajo(TipoTrabajoBorradoDTO tipoTrabajoBorradoDTO) throws ResourceNotFoundException {
		
		this.customTipoTrabajoService.borrarTipoTrabajo(tipoTrabajoBorradoDTO);
		return ResponseEntity.ok().body("El tipo de trabajo se ha actualizado correctamente");
	}
	
	
}
