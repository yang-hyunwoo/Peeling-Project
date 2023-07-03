package peeling.project.basic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

@Configuration
public class passwordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
//        return bcryptPasswordEncoder();
        return pbkdf2PasswordEncoder();
    }

    public BCryptPasswordEncoder bcryptPasswordEncoder() {
        return new BCryptPasswordEncoder(25);
    }

    public Pbkdf2PasswordEncoder pbkdf2PasswordEncoder() {
        String passwordSecret = "SECRET_KEY";
        return new Pbkdf2PasswordEncoder(passwordSecret, 128, 310000, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
    }

    /**
     * 비밀번호 앞에 알고리즘 이름이 들어간다.
     * 위에 있는 방법은 비밀번호 뒤에 알고리즘은 들어 가지 않는다.
     * 아래 방법은 알고리즘 변경시에 유용할거 같다.
     */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        String passwordSecret = "SECRET_KEY";
//        Map<String, PasswordEncoder> encoders = new HashMap<>();
//        encoders.put("bcrypt",new BCryptPasswordEncoder());
//        encoders.put("pbkdf2",new Pbkdf2PasswordEncoder(passwordSecret,128,185000, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256));
//        return new DelegatingPasswordEncoder("bcrypt", encoders);
//    }




}
