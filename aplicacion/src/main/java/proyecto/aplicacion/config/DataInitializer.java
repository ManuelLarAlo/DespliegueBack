/*
package proyecto.aplicacion.config;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import proyecto.aplicacion.models.entities.Cliente;
import proyecto.aplicacion.models.entities.Rol;
import proyecto.aplicacion.models.entities.Tarea;
import proyecto.aplicacion.models.entities.TipoTrabajo;
import proyecto.aplicacion.models.entities.Trabajo;
import proyecto.aplicacion.models.entities.Usuario;
import proyecto.aplicacion.repositories.IClienteRepository;
import proyecto.aplicacion.repositories.IRolRepository;
import proyecto.aplicacion.repositories.ITareaRepository;
import proyecto.aplicacion.repositories.ITipoTrabajoRepository;
import proyecto.aplicacion.repositories.ITrabajoRepository;
import proyecto.aplicacion.repositories.IUsuarioRepository;
import proyecto.aplicacion.utils.Constants;

@Component
@Slf4j
public class DataInitializer {
	
    @Autowired
    private IUsuarioRepository iUsuarioRepository;

    @Autowired
    private IRolRepository iRolRepository;
    
    @Autowired
    private IClienteRepository iClienteRepository;
    
    @Autowired
    private ITipoTrabajoRepository iTipoTrabajoRepository;
    
    @Autowired
    private ITrabajoRepository iTrabajoRepository;
    
    @Autowired
    private ITareaRepository iTareaRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Transactional
    @PostConstruct
    public void init() {

        // Crear roles si no existen
        if (iRolRepository.count() == 0) {
            Rol adminRole = new Rol();
            adminRole.setNombre(Constants.ROLE_ADMIN);
            iRolRepository.save(adminRole);

            Rol userRole = new Rol();
            userRole.setNombre(Constants.ROLE_USER);
            iRolRepository.save(userRole);

            Rol equipoRole = new Rol();
            equipoRole.setNombre(Constants.ROLE_TEAM);
            iRolRepository.save(equipoRole);

            log.info("Roles iniciales creados.");
        }

        // Crear clientes si no existen
        if (iClienteRepository.count() == 0) {
            Cliente cliente1 = new Cliente();
            cliente1.setClienteId(1L);
            cliente1.setNombre("Cliente 1");
            cliente1.setPersonaContacto("Juan Pérez");
            cliente1.setEmailContacto("juan@cliente.com");
            cliente1.setMovil("123456789");
            cliente1.setTelefono("987654321");
            cliente1.setDireccion("Calle Falsa 123");
            iClienteRepository.save(cliente1);

            Cliente cliente2 = new Cliente();
            cliente2.setClienteId(2L);
            cliente2.setNombre("Cliente 2");
            cliente2.setPersonaContacto("Ana Gómez");
            cliente2.setEmailContacto("ana@cliente2.com");
            cliente2.setMovil("234567890");
            cliente2.setTelefono("876543210");
            cliente2.setDireccion("Avenida Siempre Viva 742");
            iClienteRepository.save(cliente2);

            log.info("Clientes iniciales creados.");
        }

        // Crear tipos de trabajo si no existen
        if (iTipoTrabajoRepository.count() == 0) {
            TipoTrabajo tipo1 = new TipoTrabajo();
            tipo1.setTipoTrabajo("Instalación");
            iTipoTrabajoRepository.save(tipo1);

            TipoTrabajo tipo2 = new TipoTrabajo();
            tipo2.setTipoTrabajo("Mantenimiento");
            iTipoTrabajoRepository.save(tipo2);

            log.info("Tipos de trabajo iniciales creados.");
        }

        // Crear usuarios si no existen
        if (iUsuarioRepository.count() == 0) {
            Rol rolAdmin = iRolRepository.findByNombre(Constants.ROLE_ADMIN);
            Rol rolEquipo = iRolRepository.findByNombre(Constants.ROLE_TEAM);
            Rol rolUser = iRolRepository.findByNombre(Constants.ROLE_USER);

            Usuario usuarioAdmin = new Usuario();
            usuarioAdmin.setEmail("correoAdmin@gmail.com");
            usuarioAdmin.setUsername("UserAdmin");
            usuarioAdmin.setPassword(passwordEncoder.encode("1234"));
            usuarioAdmin.setRol(rolAdmin);
            iUsuarioRepository.save(usuarioAdmin);

            Usuario usuarioTeam = new Usuario();
            usuarioTeam.setEmail("correoTeam@gmail.com");
            usuarioTeam.setUsername("UserTeam");
            usuarioTeam.setPassword(passwordEncoder.encode("1234"));
            usuarioTeam.setRol(rolEquipo);
            iUsuarioRepository.save(usuarioTeam);

            Usuario usuarioUser = new Usuario();
            usuarioUser.setEmail("correoUser@gmail.com");
            usuarioUser.setUsername("UserUser");
            usuarioUser.setPassword(passwordEncoder.encode("1234"));
            usuarioUser.setRol(rolUser);
            iUsuarioRepository.save(usuarioUser);

            log.info("Usuarios iniciales creados.");
        }

        // Crear trabajos y tareas si no existen
        if (iTrabajoRepository.count() == 0 && iTareaRepository.count() == 0) {
            Cliente cliente1 = iClienteRepository.findById(1L).orElseThrow();
            Cliente cliente2 = iClienteRepository.findById(2L).orElseThrow();

            TipoTrabajo tipo1 = iTipoTrabajoRepository.findAll().get(0);
            TipoTrabajo tipo2 = iTipoTrabajoRepository.findAll().get(1);

            Usuario responsable = iUsuarioRepository.findByUsername("UserAdmin");
            Usuario clienteUser = iUsuarioRepository.findByUsername("UserUser");

            Trabajo trabajo1 = new Trabajo();
            trabajo1.setCodigo("T-001");
            trabajo1.setOtTrabajo(1001L);
            trabajo1.setTipoTrabajo(tipo1);
            trabajo1.setFechaInicioTrabajo(new Date());
            trabajo1.setFechaFinTrabajo(new Date());
            trabajo1.setPrioridadTrabajo("Alta");
            trabajo1.setDescripcion("Instalación de equipos de aire acondicionado");
            trabajo1.setCliente(cliente1);
            iTrabajoRepository.save(trabajo1);

            Trabajo trabajo2 = new Trabajo();
            trabajo2.setCodigo("T-002");
            trabajo2.setOtTrabajo(1002L);
            trabajo2.setTipoTrabajo(tipo2);
            trabajo2.setFechaInicioTrabajo(new Date());
            trabajo2.setFechaFinTrabajo(new Date());
            trabajo2.setPrioridadTrabajo("Media");
            trabajo2.setDescripcion("Mantenimiento de sistemas de climatización");
            trabajo2.setCliente(cliente2);
            iTrabajoRepository.save(trabajo2);

            // Crear tareas
            Tarea tarea1 = new Tarea();
            tarea1.setTarea("Instalar unidades interiores");
            tarea1.setFechaInicioTarea(new Date());
            tarea1.setFechaFinTarea(new Date());
            tarea1.setPrioridadTarea("Alta");
            tarea1.setStatus("Pendiente");
            tarea1.setNotas("Requiere equipo especializado");
            tarea1.setMostrarCalendario(true);
            tarea1.setNotificacionEnviada(false);
            tarea1.setExpirada(false);
            tarea1.setTrabajo(trabajo1);
            tarea1.setResponsable(responsable);
            tarea1.setCliente(clienteUser);
            iTareaRepository.save(tarea1);

            // Repite para tareas 2, 3, 4 si lo deseas...
            log.info("Trabajos y tareas iniciales creadas.");
        }
    }
}
*/
