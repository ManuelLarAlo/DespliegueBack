package proyecto.aplicacion.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import proyecto.aplicacion.models.entities.Tarea;

@Repository
public interface ITareaRepository extends JpaRepository<Tarea, Long>{
	    
    // Consulta para ADMIN: obtiene todas las tareas con filtros opcionales
    @Query("SELECT t FROM Tarea t " +
           "WHERE (:clienteId IS NULL OR t.trabajo.cliente.clienteId = :clienteId) " +
           "AND (:responsableId IS NULL OR t.responsable.userId = :responsableId) " +
           "AND (:ot IS NULL OR t.trabajo.otTrabajo = :ot) " +
           "AND (:prioridad IS NULL OR t.prioridadTarea = :prioridad) " +
           "AND (:status IS NULL OR t.status = :status)")
    List<Tarea> findAllWithFilters(
        @Param("clienteId") Long clienteId,
        @Param("responsableId") Long responsableId,
        @Param("ot") Integer ot,
        @Param("prioridad") String prioridad,
        @Param("status") String status
    );

    // CLIENTE: solo ve tareas en las que está asignado como cliente en la entidad Tarea
    @Query("SELECT t FROM Tarea t " +
           "WHERE t.cliente.userId = :clienteId " +  // Filtra por el usuario cliente asignado a la tarea
           "AND (:clienteTrabajoId IS NULL OR t.trabajo.cliente.clienteId = :clienteTrabajoId) " +  // Filtro opcional para cliente del trabajo
           "AND (:responsableId IS NULL OR t.responsable.userId = :responsableId) " +
           "AND (:ot IS NULL OR t.trabajo.otTrabajo = :ot) " +
           "AND (:prioridad IS NULL OR t.prioridadTarea = :prioridad) " +
           "AND (:status IS NULL OR t.status = :status)")
    List<Tarea> findByClienteWithFilters(
        @Param("clienteId") Long clienteId,
        @Param("clienteTrabajoId") Long clienteTrabajoId,
        @Param("responsableId") Long responsableId,
        @Param("ot") Integer ot,
        @Param("prioridad") String prioridad,
        @Param("status") String status
    );
    
    // Consultas iguales que las de arriba, pero con pageable
    @Query("SELECT t FROM Tarea t " +
           "WHERE (:clienteId IS NULL OR t.trabajo.cliente.clienteId = :clienteId) " +
           "AND (:responsableId IS NULL OR t.responsable.userId = :responsableId) " +
           "AND (:ot IS NULL OR t.trabajo.otTrabajo = :ot) " +
           "AND (:prioridad IS NULL OR t.prioridadTarea = :prioridad) " +
           "AND (:status IS NULL OR t.status = :status)")
    Page<Tarea> findAllWithFiltersPage(
        @Param("clienteId") Long clienteId,
        @Param("responsableId") Long responsableId,
        @Param("ot") Integer ot,
        @Param("prioridad") String prioridad,
        @Param("status") String status,
        Pageable pageable
    );

    @Query("SELECT t FROM Tarea t " +
           "WHERE t.cliente.userId = :clienteId " +  // Filtra por el usuario cliente asignado a la tarea
           "AND (:clienteTrabajoId IS NULL OR t.trabajo.cliente.clienteId = :clienteTrabajoId) " +  // Filtro opcional para cliente del trabajo
           "AND (:responsableId IS NULL OR t.responsable.userId = :responsableId) " +
           "AND (:ot IS NULL OR t.trabajo.otTrabajo = :ot) " +
           "AND (:prioridad IS NULL OR t.prioridadTarea = :prioridad) " +
           "AND (:status IS NULL OR t.status = :status)")
    Page<Tarea> findByClienteWithFiltersPage(
        @Param("clienteId") Long clienteId,
        @Param("clienteTrabajoId") Long clienteTrabajoId,
        @Param("responsableId") Long responsableId,
        @Param("ot") Integer ot,
        @Param("prioridad") String prioridad,
        @Param("status") String status,
        Pageable pageable
    );
    
    // Método que encuentra todas las tareas cuyo fechaFinTarea es menor o igual a la fecha límite
    @Query("SELECT t FROM Tarea t WHERE t.fechaFinTarea <= :fechaLimite")
    List<Tarea> findTareasCercanasAFin2(@Param("fechaLimite") Date fechaLimite);
    
    // Método para obtener las tareas cuya fecha de fin está dentro de las próximas 24 horas
    @Query("SELECT t FROM Tarea t WHERE t.fechaFinTarea <= :fechaLimite AND t.fechaFinTarea >= :fechaActual")
    List<Tarea> findTareasCercanasAFin(@Param("fechaLimite") Date fechaLimite, @Param("fechaActual") Date fechaActual);
    
    @Query("SELECT t FROM Tarea t WHERE t.fechaFinTarea < CURRENT_TIMESTAMP AND t.status != 'COMPLETADO' AND t.expirada = false")
    List<Tarea> findTareasExpiradas();

}
