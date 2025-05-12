package proyecto.aplicacion.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import proyecto.aplicacion.models.entities.Rol;

@Repository
public interface IRolRepository extends JpaRepository<Rol, Long>{
	
	Rol findByNombre(String nombre);
	
}
