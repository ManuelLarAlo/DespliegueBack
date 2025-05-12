package proyecto.aplicacion.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import proyecto.aplicacion.models.entities.TipoTrabajo;

public interface ITipoTrabajoRepository extends JpaRepository<TipoTrabajo, Long>{
	
	//Consulta findAll, pero con paginación
	Page<TipoTrabajo> findAll(Pageable pageable);

}
