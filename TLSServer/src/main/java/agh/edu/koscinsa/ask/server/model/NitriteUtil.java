package agh.edu.koscinsa.ask.server.model;

import org.dizitart.no2.Nitrite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

@Repository
public class NitriteUtil {

    @Value("$db.username")
    String username;
    @Value("$db.password")
    String password;

    @Bean
    public Nitrite createDb(){
        return Nitrite.builder()
                .compressed()
                .filePath("TLSServer/src/main/resources/server.db")
                .openOrCreate(username, password);
    }
}
