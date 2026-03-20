package com.insightflow.usage;

import static org.assertj.core.api.Assertions.assertThat;

import com.insightflow.usage.repository.InMemoryUsageRecordRepository;
import com.insightflow.usage.repository.UsageEventSnapshotRepository;
import com.insightflow.usage.repository.UsageRecordRepository;
import com.insightflow.usage.service.UsageQueryService;
import java.sql.Connection;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:usage-service;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsageServiceApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UsageRecordRepository usageRecordRepository;

    @Test
    void contextStartsWithUsageFlywayAndJdbcRepositories() throws Exception {
        assertThat(applicationContext.getBean(UsageQueryService.class)).isNotNull();
        assertThat(applicationContext.containsBean("flyway")).isTrue();
        assertThat(usageRecordRepository).isNotInstanceOf(InMemoryUsageRecordRepository.class);
        assertThat(applicationContext.getBeanNamesForType(UsageEventSnapshotRepository.class)).hasSize(1);
        assertThat(applicationContext.getBean(UsageEventSnapshotRepository.class).count()).isZero();
        assertThat(usageRecordRepository.findAll()).isEmpty();

        try (Connection connection = dataSource.getConnection()) {
            assertThat(hasTable(connection, "usage_event_snapshots")).isTrue();
            assertThat(hasTable(connection, "usage_records")).isTrue();
        }
    }

    private boolean hasTable(Connection connection, String tableName) throws Exception {
        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, tableName, null)) {
            return resultSet.next();
        }
    }
}
