package proyecto.aplicacion.Rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.dto.MarcarNotificacionesDTO;
import proyecto.aplicacion.models.dto.NotificacionBorradoDTO;
import proyecto.aplicacion.models.dto.NotificacionDTO;
import proyecto.aplicacion.services.CustomNotificacionService;
import proyecto.aplicacion.utils.exceptions.ResourceNotFoundException;

@Controller
@Slf4j
@RequestMapping(path = "/notificaciones")
public class NotificationController {
	
	@Autowired
	private CustomNotificacionService customNotificacionService;
	
	/*
	@RequestMapping(value = "/listarNotificaciones", method = RequestMethod.GET)
	public ResponseEntity<?> listarNotificaciones(Long userId) {
		
		return ResponseEntity.ok().body(this.customNotificacionService.listarNotificaciones(userId));
	}
	*/
	
	//Igual que el de arriba, pero con un pageable
	@RequestMapping(value = "/listarNotificaciones", method = RequestMethod.GET)
	public ResponseEntity<?> listarNotificaciones2(Long userId, @RequestParam(defaultValue = "0") int page, 
																@RequestParam(defaultValue = "30") int size) {
		
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("fechaCreacion")));
		Page<NotificacionDTO> notificacionesPage = this.customNotificacionService.listarNotificaciones2(userId, pageable);
		return ResponseEntity.ok().body(notificacionesPage);
	}
	
	@RequestMapping(value = "/borrarNotificacion", method = RequestMethod.DELETE)
	public ResponseEntity<?> borrarNotificacion(NotificacionBorradoDTO notificacionBorradoDTO) throws ResourceNotFoundException {
		
		this.customNotificacionService.borrarNotificacion(notificacionBorradoDTO);
		
		//Si se borra solo una notificacion, da una respuesta en singular, si se borran varias, en plural
		if (notificacionBorradoDTO.getIds().size() == 1) {
			return ResponseEntity.ok().body("Se ha borrado la notificacion correctamente");
		} else if (notificacionBorradoDTO.getIds().size() > 1) {
			return ResponseEntity.ok().body("Se han borrado " + notificacionBorradoDTO.getIds().size() + " notificaciones correctamente");
		} else {
			return ResponseEntity.ok().body("No se encontraron notificaciones para borrar");
		}
	}
	
	@RequestMapping(value = "/marcarComoLeida", method = RequestMethod.PUT)
	public ResponseEntity<?> marcarComoLeida(@RequestBody MarcarNotificacionesDTO marcarNotificacionesDTO) throws ResourceNotFoundException {
		
		this.customNotificacionService.marcarComoLeida(marcarNotificacionesDTO);
		
		if (marcarNotificacionesDTO.getIds().size() == 1) {
			return ResponseEntity.ok().body("Se ha marcado la notificacion como leída");
		} else if (marcarNotificacionesDTO.getIds().size() > 1) {
			return ResponseEntity.ok().body("Se han marcado " + marcarNotificacionesDTO.getIds().size() + " notificaciones como leídas correctamente");
		} else {
			return ResponseEntity.ok().body("No se encontraron notificaciones para marcar como leídas");
		}
	}

}
