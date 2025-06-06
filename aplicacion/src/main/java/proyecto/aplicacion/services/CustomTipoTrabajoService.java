package proyecto.aplicacion.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.dto.TipoTrabajoBorradoDTO;
import proyecto.aplicacion.models.dto.TipoTrabajoDTO;
import proyecto.aplicacion.models.entities.TipoTrabajo;
import proyecto.aplicacion.models.entities.Trabajo;
import proyecto.aplicacion.repositories.ITipoTrabajoRepository;
import proyecto.aplicacion.repositories.ITrabajoRepository;
import proyecto.aplicacion.utils.exceptions.MandatoryResourceException;
import proyecto.aplicacion.utils.exceptions.ResourceNotFoundException;

@Service
@Slf4j
public class CustomTipoTrabajoService {
	
	@Autowired
	private ITipoTrabajoRepository iTipoTrabajoRepository;
	
	@Autowired
	private ITrabajoRepository iTrabajoRepository;
	
	//Método para crear tipos de trabajo
	public void crearTipoTrabajo(TipoTrabajoDTO tipoTrabajoDTO) throws MandatoryResourceException {
		
		TipoTrabajo tipoTrabajo = new TipoTrabajo();
		
		//Se valida que el nombre no sea nulo ni vacío
		if (tipoTrabajoDTO.getTipoTrabajo() == null || tipoTrabajoDTO.getTipoTrabajo().isEmpty()) {
	        String error = "Error - El nombre del tipo de trabajo es obligatorio";
	        log.error(error);
	        throw new MandatoryResourceException(400, error);
		}
		
		tipoTrabajo.setTipoTrabajo(tipoTrabajoDTO.getTipoTrabajo());
		tipoTrabajo.setDescripcion(tipoTrabajoDTO.getDescripcion());
		
		this.iTipoTrabajoRepository.saveAndFlush(tipoTrabajo);
	}
	
	/*
	//Método para listar tipos de trabajo
	public List<TipoTrabajoDTO> listarTiposTrabajo() {
		
		List<TipoTrabajo> tiposTrabajo = this.iTipoTrabajoRepository.findAll();
		if (tiposTrabajo.isEmpty()) {
			log.info("No se encontraron tipos de trabajo");
			return Collections.emptyList();
		}
		
		List<TipoTrabajoDTO> tiposTrabajoDTOs = tiposTrabajo.stream()
				.map(tipoTrabajo -> {
					TipoTrabajoDTO dto = new TipoTrabajoDTO();
					dto.setId(tipoTrabajo.getId());
					dto.setTipoTrabajo(tipoTrabajo.getTipoTrabajo());
					dto.setDescripcion(tipoTrabajo.getDescripcion());
					return dto;
				})
				.collect(Collectors.toList());
		
		return tiposTrabajoDTOs;
	}
	*/
	
	//Método igual que el de arriba, pero con un pageable
	public Page<TipoTrabajoDTO> listarTiposTrabajo2(Pageable pageable) {
		
		Page<TipoTrabajo> tiposTrabajo = this.iTipoTrabajoRepository.findAll(pageable);
		if (tiposTrabajo.isEmpty()) {
			log.info("No se encontraron tipos de trabajo");
			return Page.empty();
		}
		
		Page<TipoTrabajoDTO> tiposTrabajoDTOs = tiposTrabajo
				.map(tipoTrabajo -> {
					TipoTrabajoDTO dto = new TipoTrabajoDTO();
					dto.setId(tipoTrabajo.getId());
					dto.setTipoTrabajo(tipoTrabajo.getTipoTrabajo());
					dto.setDescripcion(tipoTrabajo.getDescripcion());
					return dto;
				});
		
		return tiposTrabajoDTOs;
	}
	
	//Método para actualizar un tipo de trabajo
	public void actualizarTipoTrabajo(TipoTrabajoDTO tipoTrabajoDTO) throws ResourceNotFoundException {
		
		Optional<TipoTrabajo> tipoTrabajoOpt = this.iTipoTrabajoRepository.findById(tipoTrabajoDTO.getId());
		if (!tipoTrabajoOpt.isPresent()) {
	        String error = "Error - No existe un tipo de trabajo con esa ID";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
		}
		
		//Obtiene el tipo de trabajo
		TipoTrabajo tipoTrabajoExistente = tipoTrabajoOpt.get();
		
		if (tipoTrabajoDTO.getTipoTrabajo() != null && !tipoTrabajoDTO.getTipoTrabajo().isEmpty()) {
			
			tipoTrabajoExistente.setTipoTrabajo(tipoTrabajoDTO.getTipoTrabajo());
		}
		
		if (tipoTrabajoDTO.getDescripcion() != null && tipoTrabajoDTO.getDescripcion().isEmpty()) {
			
			tipoTrabajoExistente.setDescripcion(tipoTrabajoDTO.getDescripcion());
		}
		
		this.iTipoTrabajoRepository.saveAndFlush(tipoTrabajoExistente);
	}
	
	public void borrarTipoTrabajo(TipoTrabajoBorradoDTO tipoTrabajoBorradoDTO) throws ResourceNotFoundException {
		
		Optional<TipoTrabajo> tipoTrabajoOpt = this.iTipoTrabajoRepository.findById(tipoTrabajoBorradoDTO.getId());
		if (!tipoTrabajoOpt.isPresent()) {
	        String error = "Error - No se han encontrado tipos de trabajo con esa ID";
	        log.error(error);
	        throw new ResourceNotFoundException(404, error);
		}
		
		//Si se elimina un tipo de trabajo, se quita la referencia al tipo de trabajo para
		//evitar el borrado en cascada
		List<Trabajo> trabajos = tipoTrabajoOpt.get().getTrabajos();
		if (tipoTrabajoOpt.get().getTrabajos() != null && !tipoTrabajoOpt.get().getTrabajos().isEmpty()) {
			List<Trabajo> trabajosModificados = new ArrayList<Trabajo>();
			for (Trabajo trabajo : trabajos) {
				trabajo.setTipoTrabajo(null);
				trabajosModificados.add(trabajo);
			}
			this.iTrabajoRepository.saveAllAndFlush(trabajosModificados);
		}

		log.info("El tipo de trabajo se ha eliminado correctamente");
		this.iTipoTrabajoRepository.delete(tipoTrabajoOpt.get());
	}

}
