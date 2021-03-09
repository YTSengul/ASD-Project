package nl.han.asd.submarine;

import nl.han.asd.submarine.models.Chatter;
import nl.han.asd.submarine.controllers.dto.ChatterRegistrationDTO;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ChatterDirectoryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatterDirectoryServerApplication.class, args);
    }


    @Bean
    public ModelMapper getMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper //ChatterRegistrationDTO for chatterRegistration mapping to Chatter. Password must be manually converted to password hash and salt.
                .typeMap(ChatterRegistrationDTO.class, Chatter.class)
                .addMappings(mapper -> mapper.skip(Chatter::setPasswordSalt))
                .addMapping(ChatterRegistrationDTO::getPassword, Chatter::setPasswordHash);

        return modelMapper;
    }
}
