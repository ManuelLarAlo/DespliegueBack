package proyecto.aplicacion.services;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.dto.ClienteBorradoDTO;
import proyecto.aplicacion.models.dto.ClienteDTO;
import proyecto.aplicacion.models.entities.Cliente;
import proyecto.aplicacion.repositories.IClienteRepository;
import proyecto.aplicacion.utils.exceptions.MandatoryResourceException;
import proyecto.aplicacion.utils.exceptions.ResourceAlreadyExistsException;
import proyecto.aplicacion.utils.exceptions.ResourceNotFoundException;

@Service
@Slf4j
public class CustomClienteService {
	
	@Autowired
	private IClienteRepository iClienteRepository;
	
	
	public void crearCliente(@Valid ClienteDTO clienteDTO) throws MandatoryResourceException, ResourceAlreadyExistsException {
		
	    // Valida si la id del cliente es nula
	    if (clienteDTO.getClienteId() == null) {
	        String errorCliente = "Error - La ID del cliente es obligatoria";
	        log.error(errorCliente);
	        throw new MandatoryResourceException(400, errorCliente);
	    }
	    
	    //Valida si el nombre del cliente es nulo o vacio
	    if (clienteDTO.getNombre() == null) {
	        String errorCliente = "Error - El nombre del cliente es obligatoria";
	        log.error(errorCliente);
	        throw new MandatoryResourceException(400, errorCliente);
	    }
	    
	    // Verificar si el cliente ya existe
	    Optional<Cliente> clienteExistente = iClienteRepository.findById(clienteDTO.getClienteId());
	    if (clienteExistente.isPresent()) {
	        String errorCliente = "Error - El cliente con este ID ya existe";
	        log.error(errorCliente);
	        throw new ResourceAlreadyExistsException(409, errorCliente);
	    }
	    
	    // Crear y guardar el nuevo cliente
	    Cliente cliente = new Cliente();
	    cliente.setClienteId(clienteDTO.getClienteId());
	    cliente.setNombre(clienteDTO.getNombre());
	    cliente.setPersonaContacto(clienteDTO.getPersonaContacto());
	    cliente.setEmailContacto(clienteDTO.getEmailContacto());
	    cliente.setMovil(clienteDTO.getMovil());
	    cliente.setTelefono(clienteDTO.getTelefono());
	    cliente.setDireccion(clienteDTO.getDireccion());
	    
	    iClienteRepository.saveAndFlush(cliente);
		
	}
	
	/*
	public List<ClienteDTO> listarClientes() {
		
		List<Cliente> clientes = iClienteRepository.findAll();
		
		if (clientes.isEmpty()) {
	        log.info("No se encontraron clientes"); //En el caso de no encontrar clientes, devuelve una lista vacía y no lanza
	        return Collections.emptyList();  		//una excepción, porque puede ser que no haya clientes dados de alta
		}
		
		//Mapear los clientes a DTOs
		List<ClienteDTO> clienteDTOs = clientes.stream()
                .map(cliente -> {
                    ClienteDTO dto = new ClienteDTO();
                    dto.setClienteId(cliente.getClienteId());
                    dto.setNombre(cliente.getNombre());
                    dto.setPersonaContacto(cliente.getPersonaContacto());
                    dto.setEmailContacto(cliente.getEmailContacto());
                    dto.setMovil(cliente.getMovil());
                    dto.setTelefono(cliente.getTelefono());
                    dto.setDireccion(cliente.getDireccion());
                    return dto;
                })
                .collect(Collectors.toList());
		
		return clienteDTOs;
	}
	*/
	
	//Igual que el de arriba, pero con un pageable
	public Page<ClienteDTO> listarClientes2(Pageable pageable) {
		
		Page<Cliente> clientes = iClienteRepository.findAll(pageable);
		
		if (clientes.isEmpty()) {
	        log.info("No se encontraron clientes"); //En el caso de no encontrar clientes, devuelve una lista vacía y no lanza
	        return Page.empty();	//una excepción, porque puede ser que no haya clientes dados de alta
		}
		
		//Mapear los clientes a DTOs
		Page<ClienteDTO> clienteDTOs = clientes
                .map(cliente -> {
                    ClienteDTO dto = new ClienteDTO();
                    dto.setClienteId(cliente.getClienteId());
                    dto.setNombre(cliente.getNombre());
                    dto.setPersonaContacto(cliente.getPersonaContacto());
                    dto.setEmailContacto(cliente.getEmailContacto());
                    dto.setMovil(cliente.getMovil());
                    dto.setTelefono(cliente.getTelefono());
                    dto.setDireccion(cliente.getDireccion());
                    return dto;
                });
		
		return clienteDTOs;
	}
	
	public void actualizarCliente (@Valid Long clienteId, ClienteDTO clienteDTO) throws ResourceNotFoundException {
		
		//Busca el cliente a actualizar por su id
		Optional<Cliente> clienteOpt = this.iClienteRepository.findById(clienteId);
		if (!clienteOpt.isPresent()) {
	        String error = "Error - No existe un cliente con esa ID";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
		}
		
		//Abre el objeto Cliente
		Cliente clienteExistente = clienteOpt.get();
		
		//Validación de nombre
		if (clienteDTO.getNombre() != null && !clienteDTO.getNombre().isEmpty()) {
			
			clienteExistente.setNombre(clienteDTO.getNombre());
		}
		
		//Validación de personaContacto
		if (clienteDTO.getPersonaContacto() != null && !clienteDTO.getPersonaContacto().isEmpty()) {
			
			clienteExistente.setPersonaContacto(clienteDTO.getPersonaContacto());
		}
		
		//Validación de emailContacto
		if (clienteDTO.getEmailContacto() != null && !clienteDTO.getEmailContacto().isEmpty()) {
			
			clienteExistente.setEmailContacto(clienteDTO.getEmailContacto());
		}
		
		//Validación de móvil
		if (clienteDTO.getMovil() != null && !clienteDTO.getMovil().isEmpty()) {
			
			clienteExistente.setMovil(clienteDTO.getMovil());
		}
		
		//Validación de telefono
		if (clienteDTO.getTelefono() != null && !clienteDTO.getTelefono().isEmpty()) {
			
			clienteExistente.setTelefono(clienteDTO.getTelefono());
		}
		
		//Validación de Dirección
		if (clienteDTO.getDireccion() != null && !clienteDTO.getDireccion().isEmpty()) {
			
			clienteExistente.setDireccion(clienteDTO.getDireccion());
		}
		
		this.iClienteRepository.saveAndFlush(clienteExistente);
	}
	
	public void borrarCliente(ClienteBorradoDTO clienteBorradoDTO) throws ResourceNotFoundException {
		
		Optional<Cliente> clienteOpt = this.iClienteRepository.findById(clienteBorradoDTO.getClienteId());
		if (!clienteOpt.isPresent()) {
	        String error = "Error - No se han encontrado clientes con esa ID";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
		}
		
		log.info("El Cliente se ha eliminado correctamente");
		this.iClienteRepository.delete(clienteOpt.get());
	}
}
