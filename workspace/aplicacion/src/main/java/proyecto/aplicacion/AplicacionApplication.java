package proyecto.aplicacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "proyecto.aplicacion")
public class AplicacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AplicacionApplication.class, args);
	}

}
