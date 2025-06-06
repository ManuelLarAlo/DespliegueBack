package proyecto.aplicacion.Rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.dto.UsuarioDTO;

@Controller
@Slf4j
@RequestMapping(path = "/test")
public class WebController {
	
	// ------------------ RUTAS TEST  --------------------
	
	//Ruta para testear en general
	@GetMapping(value = "/testGet")
	public String getTestPage() {
	    log.debug("{} - Redirigiendo el cliente hacia '/test'.");
	    return "test";
	}
	
	//Ruta para testear el rol admin
	@GetMapping(value = "/adminGet")
	public String getAdminPage() {
		log.debug("{} - Redirigiendo el cliente hacia '/admin'.");
		return "admin";
	}
	
	//Ruta para testear el rol user
	@GetMapping(value="/userGet")
	public String getUserPage() {
		log.debug("{} - Redirigiendo el cliente hacia '/user'.");
		return "user";
	}
	
	/*
	 * Método para rellenar una lista de usuarios con dos modelos de usuario
	 * Uno para el rol admin y otro para el rol user
	 * 
	 * Falta aplicar los roles a los usuarios porque el rol no está en el DTO
	 * */
	@GetMapping(value = "/test-items")
	public String getTestItemsPage( Model model ) {
	    log.debug("{} - Redirigiendo el cliente hacia '/test-items'.");
	
	    UsuarioDTO usuarioAdminTest = new UsuarioDTO();
	    usuarioAdminTest.setEmail("correoAdmin@gmail.com");
	    usuarioAdminTest.setPassword("1234");
	    usuarioAdminTest.setUsername("UserAdmin");
	    
	    UsuarioDTO usuarioUserTest = new UsuarioDTO();
	    usuarioUserTest.setEmail("correoUser@gmail.com");
	    usuarioUserTest.setPassword("1234");
	    usuarioUserTest.setUsername("UserUser");
	    
	    List<UsuarioDTO> listaUsuarios = new ArrayList<>();
	    listaUsuarios.add(usuarioAdminTest);
	    listaUsuarios.add(usuarioUserTest);
	   	   
	   model.addAttribute("listaUsuarios", listaUsuarios);

	   // esta vista recibe el modelo con la lista de mapas.
	    return "test-items";
	}
	
	/**
	 * Muestra la página de error personalizada para el código HTTP 403 (Prohibido).
	 * 
	 * @return la vista correspondiente al error 403.
	 */
	@GetMapping(value = "/forbidden")
	public String getForbiddenErrorPage() {
	    log.debug("{} - Redirigiendo el cliente hacia '/forbidden'.");
	    return "forbidden";
	}

	/**
	 * Muestra la página de error personalizada para el código HTTP 404 (No encontrado).
	 * 
	 * @return la vista correspondiente al error 404.
	 */
	@GetMapping(value = "/not-found-error")
	public String getNotFoundErrorPage() {
	    log.debug("{} - Redirigiendo el cliente hacia '/not-found-error'.");
	    return "not-found-error";
	}

	@GetMapping(value = "/login")
	public String getFailedLoginErrorPage() {
	    log.debug("{} - Redirigiendo el cliente hacia '/login'.");
	    return "login";
	}
	
	// ------------------ RUTAS PLATAFORMA  --------------------
	
	/*
	 * Aquí debajo se harán los metodos que gestionan las rutas de la aplicación
	 */

}
