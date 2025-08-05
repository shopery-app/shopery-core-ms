package az.shopery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ShoperyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShoperyApplication.class, args);
	}
}
