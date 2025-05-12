package proyecto.aplicacion.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import proyecto.aplicacion.models.entities.Notificacion;

@Repository
public interface INotificacionRepository extends JpaRepository<Notificacion, Long> {
	
    // Método para obtener notificaciones de un usuario, ordenadas por fecha de creación
	@Query("SELECT n FROM Notificacion n WHERE n.usuario.userId = :usuarioId ORDER BY n.fechaCreacion DESC")
	List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(@Param("usuarioId") Long usuarioId);
	
    // Igual que la de arriba, pero con un pageable
	@Query("SELECT n FROM Notificacion n WHERE n.usuario.userId = :usuarioId ORDER BY n.fechaCreacion DESC")
	Page<Notificacion> findByUsuarioIdOrderByFechaCreacionDescPage(@Param("usuarioId") Long usuarioId, Pageable pageable);
	
    // Marcar todas las notificaciones de un usuario como leídas
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario.userId = :usuarioId")
    void marcarTodasLeidas(@Param("usuarioId") Long usuarioId);
}
