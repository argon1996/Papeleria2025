package me.parzibyte.sistemaventasspringboot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.info("⚙️ Configurando seguridad...");

        http
            .authorizeRequests()
                // ✅ Rutas públicas (tienda, login, recursos, imágenes)
                .antMatchers(
                    "/", "/shop/**", "/ecommerce/**",
                    "/escolar", "/escolar/**",
                    "/public/**", "/login", "/logout",
                    "/css/**", "/js/**", "/img/**", "/favicon.ico",
                    "/uploads/**", "/drawer/**"
                ).permitAll()

                // ✅ Solo ADMIN puede eliminar productos
                // (Las ventas se controlan desde el controlador)
                .antMatchers("/productos/eliminar/**").hasRole("ADMIN")

                // ✅ Todo lo demás requiere autenticación
                .anyRequest().authenticated()
                .and()

            // ✅ Página de login personalizada
            .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/vender/", true)
                .permitAll()
                .and()

            // ✅ Logout limpio
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
                .and()

            // ✅ Recordar sesión (1 hora)
            .rememberMe()
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds(60 * 60) // 1 hora
                .alwaysRemember(true) // activa sin marcar checkbox
                .and()

            // ✅ Desactivar CSRF solo para el cajón (hardware local)
            .csrf()
                .ignoringAntMatchers("/drawer/**");

        log.info("✅ Seguridad configurada correctamente.");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            // Usuario normal
            .withUser("user")
                .password(passwordEncoder().encode("123"))
                .roles("USER")
            .and()
            // Admin (puede eliminar)
            .withUser("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("USER", "ADMIN");

        log.info("👥 Usuarios configurados en memoria: user/123, admin/admin123");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
