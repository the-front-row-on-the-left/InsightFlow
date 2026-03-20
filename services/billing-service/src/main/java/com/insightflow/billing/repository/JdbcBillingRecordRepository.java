package com.insightflow.billing.repository;

import com.insightflow.billing.domain.BillingRecord;
import com.insightflow.billing.domain.PricingModel;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcBillingRecordRepository implements BillingRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcBillingRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<BillingRecord> findAll() {
        return jdbcTemplate.query(
                """
                select request_id, user_id, team_id, workflow_id, service_id, model, status, pricing_model, currency,
                       price_table_version, prompt_tokens, completion_tokens, total_tokens, billable, input_unit_price,
                       output_unit_price, request_unit_price, cost_before_rounding, total_cost, occurred_at
                from billing_records
                order by occurred_at desc, request_id
                """,
                this::mapRecord
        );
    }

    @Override
    public void save(String eventId, BillingRecord billingRecord) {
        if (existsByRequestId(billingRecord.requestId())) {
            jdbcTemplate.update(
                    """
                    update billing_records
                    set event_id = ?, user_id = ?, team_id = ?, workflow_id = ?, service_id = ?, model = ?,
                        status = ?, pricing_model = ?, currency = ?, price_table_version = ?, prompt_tokens = ?,
                        completion_tokens = ?, total_tokens = ?, billable = ?, input_unit_price = ?,
                        output_unit_price = ?, request_unit_price = ?, cost_before_rounding = ?, total_cost = ?,
                        occurred_at = ?, calculated_at = current_timestamp
                    where request_id = ?
                    """,
                    eventId,
                    billingRecord.userId(),
                    billingRecord.teamId(),
                    billingRecord.workflowId(),
                    billingRecord.serviceId(),
                    billingRecord.model(),
                    billingRecord.status(),
                    billingRecord.pricingModel().code(),
                    billingRecord.currency(),
                    billingRecord.priceTableVersion(),
                    billingRecord.promptTokens(),
                    billingRecord.completionTokens(),
                    billingRecord.totalTokens(),
                    billingRecord.billable(),
                    billingRecord.inputUnitPrice(),
                    billingRecord.outputUnitPrice(),
                    billingRecord.requestUnitPrice(),
                    billingRecord.costBeforeRounding(),
                    billingRecord.totalCost(),
                    toTimestamp(billingRecord.occurredAt()),
                    billingRecord.requestId()
            );
            return;
        }

        jdbcTemplate.update(
                """
                insert into billing_records (
                    request_id, event_id, user_id, team_id, workflow_id, service_id, model, status, pricing_model,
                    currency, price_table_version, prompt_tokens, completion_tokens, total_tokens, billable,
                    input_unit_price, output_unit_price, request_unit_price, cost_before_rounding, total_cost,
                    occurred_at, calculated_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)
                """,
                billingRecord.requestId(),
                eventId,
                billingRecord.userId(),
                billingRecord.teamId(),
                billingRecord.workflowId(),
                billingRecord.serviceId(),
                billingRecord.model(),
                billingRecord.status(),
                billingRecord.pricingModel().code(),
                billingRecord.currency(),
                billingRecord.priceTableVersion(),
                billingRecord.promptTokens(),
                billingRecord.completionTokens(),
                billingRecord.totalTokens(),
                billingRecord.billable(),
                billingRecord.inputUnitPrice(),
                billingRecord.outputUnitPrice(),
                billingRecord.requestUnitPrice(),
                billingRecord.costBeforeRounding(),
                billingRecord.totalCost(),
                toTimestamp(billingRecord.occurredAt())
        );
    }

    @Override
    public boolean existsByRequestId(String requestId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from billing_records where request_id = ?",
                Integer.class,
                requestId
        );
        return count != null && count > 0;
    }

    private BillingRecord mapRecord(ResultSet rs, int rowNum) throws SQLException {
        return new BillingRecord(
                rs.getString("request_id"),
                rs.getString("user_id"),
                rs.getString("team_id"),
                rs.getString("workflow_id"),
                rs.getString("service_id"),
                rs.getString("model"),
                rs.getString("status"),
                PricingModel.fromCode(rs.getString("pricing_model")),
                rs.getString("currency"),
                rs.getString("price_table_version"),
                rs.getInt("prompt_tokens"),
                rs.getInt("completion_tokens"),
                rs.getInt("total_tokens"),
                rs.getBoolean("billable"),
                rs.getBigDecimal("input_unit_price"),
                rs.getBigDecimal("output_unit_price"),
                rs.getBigDecimal("request_unit_price"),
                rs.getBigDecimal("cost_before_rounding"),
                rs.getBigDecimal("total_cost"),
                toInstant(rs.getTimestamp("occurred_at"))
        );
    }

    private Timestamp toTimestamp(Instant value) {
        return value == null ? null : Timestamp.from(value);
    }

    private Instant toInstant(Timestamp value) {
        return value == null ? null : value.toInstant().atOffset(ZoneOffset.UTC).toInstant();
    }
}
