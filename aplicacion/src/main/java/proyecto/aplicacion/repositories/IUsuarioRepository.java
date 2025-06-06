package proyecto.aplicacion.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import proyecto.aplicacion.models.entities.Usuario;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long>{
	
	Usuario findByUsername(String username);
	
	Optional<Usuario> findByEmail(String email);

}
