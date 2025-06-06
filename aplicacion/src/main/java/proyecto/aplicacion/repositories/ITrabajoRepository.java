package proyecto.aplicacion.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import proyecto.aplicacion.models.entities.Trabajo;
import proyecto.aplicacion.models.entities.Usuario;

@Repository
public interface ITrabajoRepository extends JpaRepository<Trabajo, String>{
	List<Trabajo> findByClienteEmailContacto(String email);
	
	//Dame todos los trabajos en los que el usuario actual esté como responsable o como cliente
	//List<Trabajo> findDistinctByTareas_ResponsableOrTareas_Cliente(Usuario responsable, Usuario cliente);
	
	//Igual que la de arriba pero con un pageable para limitar los resultados a 30
	Page<Trabajo> findDistinctByTareas_ResponsableOrTareas_Cliente(Usuario responsable, Usuario cliente, Pageable pageable);
	
	//Esto es un ejemplo de la consulta de arriba pero hecha con una query, será necesario
	//hacerla de una forma parecida a la de abajo para poder añadir filtros opcionales,
	//en el caso de la consulta de abajo tiene dos, por status y por tipo de trabajo
	/*
	@Query("SELECT DISTINCT t " +
		       "FROM Trabajo t " +
		       "JOIN t.tareas ta " +
		       "WHERE (ta.responsable = :usuario OR ta.cliente = :usuario) " +
		       "AND (:status IS NULL OR ta.status = :status) " +
		       "AND (:tipoTrabajoId IS NULL OR t.tipoTrabajo.id = :tipoTrabajoId)")
		List<Trabajo> buscarTrabajosPorUsuarioYFiltros(
		       @Param("usuario") Usuario usuario,
		       @Param("status") String status,
		       @Param("tipoTrabajoId") Long tipoTrabajoId);
		       */
}
