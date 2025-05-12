package proyecto.aplicacion.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import proyecto.aplicacion.models.entities.Cliente;

@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Long>{
	
	Page<Cliente> findAll(Pageable pageable);
	
	Cliente findByNombre(String nombre);

}
