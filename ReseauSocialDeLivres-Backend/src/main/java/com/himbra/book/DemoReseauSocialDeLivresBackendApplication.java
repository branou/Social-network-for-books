package com.himbra.book;

import com.himbra.book.book.Book;
import com.himbra.book.book.BookRepository;
import com.himbra.book.role.Role;
import com.himbra.book.role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
public class DemoReseauSocialDeLivresBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoReseauSocialDeLivresBackendApplication.class, args);
    }
    @Bean
    public CommandLineRunner runner(RoleRepository roleRepository , BookRepository bookRepository){
        return args -> {
            if(roleRepository.findByName("USER").isEmpty()) {
                roleRepository.save(Role.builder().name("USER").build());
            }
        };
    }

}
