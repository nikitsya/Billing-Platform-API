package com.nikitsya.billing.assistant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig(ChatQuotaTest.Config.class)
class ChatQuotaTest {
    @Autowired
    ChatQuota quota;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    DataSource dataSource;

    @BeforeEach
    void reset() {
        jdbc.execute("DROP TABLE IF EXISTS assistant_usage");
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V10__create_assistant_usage.sql"))
                .execute(dataSource);
    }

    @Test
    void concurrentRequestsCannotExceedTheSharedAllowance() throws Exception {
        try (var executor = Executors.newFixedThreadPool(8)) {
            var results = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < 20; i++) {
                results.add(executor.submit(() -> {
                    try {
                        quota.reserve();
                        return true;
                    } catch (ChatException exception) {
                        return false;
                    }
                }));
            }
            int successes = 0;
            for (var result : results) if (result.get()) successes++;
            assertEquals(3, successes);
            assertEquals(3, jdbc.queryForObject("SELECT monthly_requests FROM assistant_usage", Integer.class));
        }
    }

    @Test
    void resetsExpiredWindowsButPreservesTheCurrentMonthlyCounter() {
        quota.reserve();
        jdbc.update("UPDATE assistant_usage SET minute_start = '2000-01-01 00:00:00+00', day_start = '2000-01-01'");
        quota.reserve();
        assertEquals(1, jdbc.queryForObject("SELECT daily_requests FROM assistant_usage", Integer.class));
        assertEquals(2, jdbc.queryForObject("SELECT monthly_requests FROM assistant_usage", Integer.class));
        jdbc.update("UPDATE assistant_usage SET month_start = '2000-01-01'");
        quota.reserve();
        assertEquals(1, jdbc.queryForObject("SELECT monthly_requests FROM assistant_usage", Integer.class));
    }

    @Test
    void dailyAndMonthlyLimitsApplyAfterMinuteRollover() {
        quota.reserve();
        jdbc.update("UPDATE assistant_usage SET minute_start = '2000-01-01 00:00:00+00', daily_requests = 5");
        assertThrows(ChatException.class, quota::reserve);
        jdbc.update("UPDATE assistant_usage SET daily_requests = 1, monthly_requests = 7");
        assertThrows(ChatException.class, quota::reserve);
    }

    @Configuration
    @EnableTransactionManagement
    static class Config {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:chatquota;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        }

        @Bean
        JdbcTemplate jdbc(DataSource source) {
            return new JdbcTemplate(source);
        }

        @Bean
        DataSourceTransactionManager transactionManager(DataSource source) {
            return new DataSourceTransactionManager(source);
        }

        @Bean
        ChatQuota quota(JdbcTemplate jdbc) {
            return new ChatQuota(jdbc, 3, 5, 7);
        }
    }
}
