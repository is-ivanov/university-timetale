package ua.com.foxminded.university.springconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:db.properties")
@ComponentScan({"ua.com.foxminded.university.dao", "ua.com.foxminded.university.domain"})
public class RootConfig {
    private static final String DRIVER = "db.driver";
    private static final String URL = "db.url";
    private static final String LOGIN = "db.login";
    private static final String PASSWORD = "db.password";

    @Bean
    public DataSource dataSource(Environment env) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getRequiredProperty(DRIVER));
        dataSource.setUrl(env.getRequiredProperty(URL));
        dataSource.setUsername(env.getRequiredProperty(LOGIN));
        dataSource.setPassword(env.getRequiredProperty(PASSWORD));
        return dataSource;
    }

    @Autowired
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
