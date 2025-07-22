package muse_kopis.muse.auth.admin.infra;

import muse_kopis.muse.auth.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder();
    }
}
