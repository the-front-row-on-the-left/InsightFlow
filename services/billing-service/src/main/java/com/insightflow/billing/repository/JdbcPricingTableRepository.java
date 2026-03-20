package com.insightflow.billing.repository;

import com.insightflow.billing.domain.PriceTableEntry;
import com.insightflow.billing.domain.PricingModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPricingTableRepository implements PricingTableRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPricingTableRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PriceTableEntry> findAll() {
        return jdbcTemplate.query(
                """
                select price_table_version, service_id, model, pricing_model, currency, unit_price_input,
                       unit_price_output, unit_price_request, effective_from, effective_to, status
                from pricing_tables
                order by created_at asc, effective_from asc
                """,
                this::mapEntry
        );
    }

    @Override
    public List<PriceTableEntry> findByVersion(String version) {
        return jdbcTemplate.query(
                """
                select price_table_version, service_id, model, pricing_model, currency, unit_price_input,
                       unit_price_output, unit_price_request, effective_from, effective_to, status
                from pricing_tables
                where price_table_version = ?
                order by created_at asc, effective_from asc
                """,
                this::mapEntry,
                version
        );
    }

    @Override
    public void saveAll(List<PriceTableEntry> entries) {
        entries.forEach(entry -> jdbcTemplate.update(
                """
                insert into pricing_tables (
                    price_table_version, service_id, model, pricing_model, currency, unit_price_input,
                    unit_price_output, unit_price_request, effective_from, effective_to, status, created_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)
                """,
                entry.priceTableVersion(),
                entry.serviceId(),
                entry.model(),
                entry.pricingModel().code(),
                entry.currency(),
                entry.unitPriceInput(),
                entry.unitPriceOutput(),
                entry.unitPriceRequest(),
                toTimestamp(entry.effectiveFrom()),
                toTimestamp(entry.effectiveTo()),
                entry.status()
        ));
    }

    @Override
    public long count() {
        Long count = jdbcTemplate.queryForObject("select count(*) from pricing_tables", Long.class);
        return count == null ? 0 : count;
    }

    private PriceTableEntry mapEntry(ResultSet rs, int rowNum) throws SQLException {
        return new PriceTableEntry(
                rs.getString("price_table_version"),
                rs.getString("service_id"),
                rs.getString("model"),
                PricingModel.fromCode(rs.getString("pricing_model")),
                rs.getString("currency"),
                rs.getBigDecimal("unit_price_input"),
                rs.getBigDecimal("unit_price_output"),
                rs.getBigDecimal("unit_price_request"),
                toInstant(rs.getTimestamp("effective_from")),
                toInstant(rs.getTimestamp("effective_to")),
                rs.getString("status")
        );
    }

    private Timestamp toTimestamp(Instant value) {
        return value == null ? null : Timestamp.from(value);
    }

    private Instant toInstant(Timestamp value) {
        return value == null ? null : value.toInstant().atOffset(ZoneOffset.UTC).toInstant();
    }
}
