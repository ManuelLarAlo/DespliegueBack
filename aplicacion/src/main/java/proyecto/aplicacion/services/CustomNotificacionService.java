package proyecto.aplicacion.services;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.dto.MarcarNotificacionesDTO;
import proyecto.aplicacion.models.dto.NotificacionBorradoDTO;
import proyecto.aplicacion.models.dto.NotificacionDTO;
import proyecto.aplicacion.models.dto.TareaDTO;
import proyecto.aplicacion.models.entities.Notificacion;
import proyecto.aplicacion.models.entities.Tarea;
import proyecto.aplicacion.models.entities.Usuario;
import proyecto.aplicacion.repositories.INotificacionRepository;
import proyecto.aplicacion.repositories.ITareaRepository;
import proyecto.aplicacion.repositories.IUsuarioRepository;
import proyecto.aplicacion.utils.exceptions.ResourceNotFoundException;

@Service
@Slf4j
public class CustomNotificacionService {
	
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private ITareaRepository iTareaRepository;
    
    @Autowired
    private INotificacionRepository iNotificacionRepository;
    
    @Autowired
    private IUsuarioRepository iUsuarioRepository;

    //Método que se ejecuta automáticamente cada hora para comprobar las tareas
    //si detecta que a una tarea le quedan 24 horas o menos hasta su fecha límite,
    //envía un correo al usuario responsable asignado a la tarea y a los demás
    //correos específicados en el método
    @Scheduled(cron = "0 0 * * * ?") // Se ejecuta cada hora
    public void verificarTareasYEnviarNotificaciones() throws MessagingException {
    	log.debug("Iniciando verificación de tareas cercanas a vencerse.");
    	
        // Obtener la fecha actual
        Date ahora = new Date();
        
        // Definir el límite de 24 horas antes de la fecha de fin de tarea
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ahora);
        calendar.add(Calendar.HOUR, 24);
        Date fechaLimite = calendar.getTime();

        // Buscar todas las tareas cuya fecha de fin es menor o igual a 24 horas
        List<Tarea> tareas = iTareaRepository.findTareasCercanasAFin2(fechaLimite);

        // Iterar sobre todas las tareas
        for (Tarea tarea : tareas) {
        	log.debug("Procesando tarea con ID: {}, Nombre: {}", tarea.getId(), tarea.getTarea());
        	
            // Verificar si la tarea tiene un responsable asignado y si no se ha enviado la notificación
            if (tarea.getResponsable() != null && !tarea.getNotificacionEnviada()) {
                String responsableEmail = tarea.getResponsable().getEmail();
                log.debug("Enviando notificación al responsable con correo: {}", responsableEmail);
                // Enviar el correo al responsable
                enviarNotificacion(tarea, responsableEmail);
            }

            // Enviar el correo al administrador específico (por ejemplo, manuellaraalos@gmail.com)
            String adminEmail = "manuellaraalos@gmail.com"; // Correo del administrador específico
            if (!tarea.getNotificacionEnviada() && !tarea.getResponsable().getEmail().equals(adminEmail)) { //Se comprueba que el correo del administrador no sea el mismo que el del responable para evitar
            	log.debug("Enviando notificación al administrador con correo: {}", adminEmail);                       //correos duplicados en caso de que el administrador sea el responsable de la tarea
                enviarNotificacion(tarea, adminEmail);
            }
            
            // Marcar la tarea como notificada
            tarea.setNotificacionEnviada(true); //Setea el valor de notificacionEnviada a true solo cuando el correo del responsable y el del administrador han sido enviados.
            iTareaRepository.save(tarea); // Guardamos el cambio en la base de datos
            log.debug("Tarea con ID: {} marcada como notificada.", tarea.getId());
        }
    }

    // Método auxiliar para enviar las notificaciones
    private void enviarNotificacion(Tarea tarea, String destinatarioEmail) throws MessagingException {
    	log.debug("Enviando notificación a: {}", destinatarioEmail);
    	
        String subject = "📌 Notificación: Tarea por vencer";
        String messageText = "<h2 style='color: #007bff;'>¡Hola!</h2>"
                             + "<p>La tarea '" + tarea.getTarea() + "' está por vencer en menos de 24 horas.</p>"
                             + "<p>Fecha límite: " + tarea.getFechaFinTarea().toString() + "</p>"
                             + "<a href='https://miaplicacion.com/tareas' "
                             + "style='display: inline-block; padding: 10px 15px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px;'>"
                             + "Revisar tarea</a>"
                             + "<br><br>"
                             + "<img src='https://miaplicacion.com/logo.png' width='100' alt='Mi Aplicación'>"
                             + "<p style='font-size: 12px; color: gray;'>Este es un correo automático, por favor no respondas.</p>";
        try {
            // Enviar el correo utilizando el servicio de notificación
            enviarCorreo(destinatarioEmail, subject, messageText);
        } catch (MailException e) {
            e.printStackTrace();
            log.error("Error al enviar la notificación al correo: {}", destinatarioEmail, e);
        }
    }
    
    // Método para enviar una notificación por correo electrónico
    public void enviarCorreo(String toEmail, String subject, String messageText) throws MailException, MessagingException {
    	log.debug("Preparando para enviar el correo a: {}", toEmail);
    	
        try {
            // Crear el mensaje MIME
            var mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            // Establecer los parámetros del correo
            helper.setFrom("manuellaraalos@gmail.com");  // Correo de quien envía
            helper.setTo(toEmail);  // Correo destinatario
            helper.setSubject(subject);  // Asunto
            helper.setText(messageText, true);  // Cuerpo en formato HTML
            // helper.addInline("logo", null); esto se puede usar para añadir una imagen al correo

            // Enviar el mensaje
            javaMailSender.send(mimeMessage);
            log.debug("Correo enviado a {}", toEmail);
        } catch (MailException e) {
        	e.printStackTrace();
        	log.error("Error al enviar el correo a {}", toEmail, e);
        }
    }
    
    /*
    public List<NotificacionDTO> listarNotificaciones(Long usuarioId) {
    	log.debug("Iniciando la búsqueda de notificaciones para el usuario con ID: {}", usuarioId);
    	
    	List<Notificacion> notificaciones = this.iNotificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    	
    	if (notificaciones.isEmpty()) {
    		log.debug("No se encontraron notificaciones para el usuario con ID: {}", usuarioId);
    		return Collections.emptyList();
    	}
    	
    	log.debug("Se encontraron {} notificaciones para el usuario con ID: {}", notificaciones.size(), usuarioId);
    	
    	List<NotificacionDTO> notificacionDTOs = notificaciones.stream()
    			.map(notificacion -> {
    				NotificacionDTO dto = new NotificacionDTO();
    				dto.setMensaje(notificacion.getMensaje());
    				dto.setLeida(notificacion.isLeida());
    				dto.setFechaCreacion(notificacion.getFechaCreacion());
    				return dto;
    				
    			}).collect(Collectors.toList());
    	
    	log.debug("Se han transformado {} notificaciones a DTOs", notificacionDTOs.size());
    	
    	return notificacionDTOs;
    }
    */
    
    //Método igual que el de arriba, pero con un pageable
    public Page<NotificacionDTO> listarNotificaciones2(Long usuarioId, Pageable pageable) {
    	log.debug("Iniciando la búsqueda de notificaciones para el usuario con ID: {}", usuarioId);
    	
    	Page<Notificacion> notificaciones = this.iNotificacionRepository.findByUsuarioIdOrderByFechaCreacionDescPage(usuarioId, pageable);
    	
    	if (notificaciones.isEmpty()) {
    		log.debug("No se encontraron notificaciones para el usuario con ID: {}", usuarioId);
    		return Page.empty();
    	}
    	
    	log.debug("Se encontraron {} notificaciones para el usuario con ID: {}", notificaciones.getTotalElements(), usuarioId);
    	
    	Page<NotificacionDTO> notificacionDTOs = notificaciones
    			.map(notificacion -> {
    				NotificacionDTO dto = new NotificacionDTO();
    				dto.setMensaje(notificacion.getMensaje());
    				dto.setLeida(notificacion.isLeida());
    				dto.setFechaCreacion(notificacion.getFechaCreacion());
    				return dto;
    				
    			});
    	
    	log.debug("Se han transformado {} notificaciones a DTOs", notificacionDTOs.getTotalElements());
    	
    	return notificacionDTOs;
    }
    
    public void borrarNotificacion(NotificacionBorradoDTO notificacionBorradoDTO) throws ResourceNotFoundException {
    	log.debug("Iniciando el proceso de borrado de notificaciones con los IDs: {}", notificacionBorradoDTO.getIds());
    	
    	List<Notificacion> notificaciones = this.iNotificacionRepository.findAllById(notificacionBorradoDTO.getIds());
    	
    	if (notificaciones.isEmpty()) {
    		log.warn("No se han encontrado notificaciones");
    		return; //Sale del método en vez de intentar borrar una lista vacía
    	}
    	
    	log.debug("Se encontraron {} notificaciones a borrar", notificaciones.size());
    	
    	this.iNotificacionRepository.deleteAll(notificaciones);
    	
    	log.debug("Se han borrado correctamente {} notificaciones", notificaciones.size());
    }
    
    public void marcarComoLeida(MarcarNotificacionesDTO marcarNotificacionesDTO) throws ResourceNotFoundException {
    	log.debug("Iniciando el proceso de marcar notificaciones como leídas con los IDs: {}", marcarNotificacionesDTO.getIds());
    	
    	//Se busca una lista de ids para que el método permita borrar varias ids de una
    	List<Notificacion> notificaciones = this.iNotificacionRepository.findAllById(marcarNotificacionesDTO.getIds());
    	
    	if (notificaciones.isEmpty()) {
    		log.warn("No se han encontrado notificaciones");
    		return; //Sale del método en vez de intentar iterar en una lista vacía
    	}
    	
    	log.debug("Se encontraron {} notificaciones para marcar como leídas", notificaciones.size());
    	
    	for (Notificacion notificacion : notificaciones) {
    		notificacion.setLeida(true);
    	}
    	//Se actualiza la base de datos una sola vez para menor carga
    	this.iNotificacionRepository.saveAllAndFlush(notificaciones);
    	
    	log.debug("Se han marcado como leídas {} notificaciones", notificaciones.size());
    }
    
    //Este método se necesita para llamarlo en los demás métodos, en caso de que se envíe una notificacion,
    //el método crea una notificación genérica, a la que se le añade el mensaje y el usuario en los métodos
    //en los que se llame
    public void crearNotificacionAutomatica(Long usuarioId, String mensaje) throws ResourceNotFoundException {
    	log.debug("Creando una notificación automática para el usuario con ID: {}", usuarioId);
    	
    	Notificacion notificacion = new Notificacion();
    	notificacion.setMensaje(mensaje);
    	notificacion.setLeida(false);
    	notificacion.setFechaCreacion(LocalDateTime.now());
    	
    	Optional<Usuario> usuarioOpt = this.iUsuarioRepository.findById(usuarioId);
	    if (!usuarioOpt.isPresent()) {
	        String errorCliente = "Error - Usuario no encontrado";
	        log.error(errorCliente);
	        throw new ResourceNotFoundException(404, errorCliente);
	    }
	    
	    notificacion.setUsuario(usuarioOpt.get());
	    this.iNotificacionRepository.saveAndFlush(notificacion);
	    
	    log.debug("Notificación creada para el usuario con ID: {}", usuarioId);
    }
    
    
    //Estos métodos son código repetitivo para cada caso en el que se quiera enviar una notificacion, pero como no tengo
    //forma de identificar las notificaciones distintas, no puedo hacerlo de otra forma que no sea hacer un métoodo
    //por cada caso de notificación y llamar al método en su caso correspondiente.
    
    public void enviarNotificacionTareaCreada(TareaDTO tareaDTO) throws ResourceNotFoundException {
    	log.debug("Enviando notificación por tarea creada: {}", tareaDTO.getTarea());
    	
        if (tareaDTO.getResponsableId() == null) {
        	String error = "Error - No se puede enviar notificación, usuario no encontrado.";
            log.error(error);
            throw new ResourceNotFoundException(404, error);
        }
    	
    	String mensaje = "Se ha creado la tarea con nombre " + tareaDTO.getTarea() + " y de tipo " + tareaDTO.getTipoTrabajoTarea();
    	crearNotificacionAutomatica(tareaDTO.getResponsableId(), mensaje);
    }
    
    public void enviarNotificacionTareaActualizada(TareaDTO tareaDTO) throws ResourceNotFoundException {
    	log.debug("Enviando notificación por tarea actualizada: {}", tareaDTO.getTarea());
    	
        if (tareaDTO.getResponsableId() == null) {
        	String error = "Error - No se puede enviar notificación, usuario no encontrado.";
            log.error(error);
            throw new ResourceNotFoundException(404, error);
        }
    	
    	String mensaje = "Se ha actualizado la tarea con nombre " + tareaDTO.getTarea() + " y de tipo " + tareaDTO.getTipoTrabajoTarea();
    	crearNotificacionAutomatica(tareaDTO.getResponsableId(), mensaje);
    }
    
    //Este método envía una notificacion al responsable de la tarea cuando esta expira,
    //no se llama en ningún sitio porque se comprueba automaticamente cada hora
    @Transactional
    @Scheduled(cron = "0 0 * * * ?")
    public void enviarNotificacionTareaExpirada() throws ResourceNotFoundException {
    	log.debug("Comprobando tareas expiradas...");
    	
    	List<Tarea> tareasExpiradas = iTareaRepository.findTareasExpiradas();

        if (tareasExpiradas.isEmpty()) {
            log.info("No hay tareas expiradas para notificar.");
            return;
        }
        
        for (Tarea tarea : tareasExpiradas) {
        	String mensaje = String.format("⚠️ La tarea '%s' ha expirado el %s. Por favor, revisa su estado.", 
                    						tarea.getTarea(), tarea.getFechaFinTarea());
        	
        	Usuario usuario = tarea.getResponsable();
        	
        	if (usuario != null) {
        		crearNotificacionAutomatica(usuario.getUserId(), mensaje);
        	}
        	
        	tarea.setExpirada(true);
        	iTareaRepository.saveAndFlush(tarea);
        	log.debug("Tarea expirada con ID: {} marcada como expirada.", tarea.getId());
        }
        
        log.info("Se enviaron notificaciones para {} tareas expiradas.", tareasExpiradas.size());
    }
}

/*
// Cuerpo del mensaje en HTML
String messageText = "<html>"
                     + "<body style='font-family: Arial, sans-serif;'>"
                     + "<h2 style='color: #007bff;'>¡Hola!</h2>"
                     + "<p>La tarea <strong>'" + tarea.getTarea() + "'</strong> está por vencer en menos de 24 horas.</p>"
                     + "<p><strong>Fecha límite:</strong> " + tarea.getFechaFinTarea().toString() + "</p>"
                     + "<p>Te invitamos a revisar los detalles de la tarea y tomar las acciones necesarias.</p>"
                     + "<a href='https://miaplicacion.com/tareas' "
                     + "style='display: inline-block; padding: 10px 15px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px;'>"
                     + "Revisar tarea</a>"
                     + "<br><br>"
                     + "<img src='https://miaplicacion.com/logo.png' width='100' alt='Mi Aplicación'/>"
                     + "<p style='font-size: 12px; color: gray;'>Este es un correo automático, por favor no respondas.</p>"
                     + "</body>"
                     + "</html>";
*/
