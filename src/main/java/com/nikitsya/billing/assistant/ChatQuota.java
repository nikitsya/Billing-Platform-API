package com.nikitsya.billing.assistant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
public class ChatQuota {
    private final JdbcTemplate jdbc;
    private final int minuteLimit;
    private final int dailyLimit;
    private final int monthlyLimit;

    public ChatQuota(JdbcTemplate jdbc,
                     @Value("${assistant.requests-per-minute:10}") int minuteLimit,
                     @Value("${assistant.requests-per-day:100}") int dailyLimit,
                     @Value("${assistant.requests-per-month:500}") int monthlyLimit) {
        this.jdbc = jdbc;
        this.minuteLimit = minuteLimit;
        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
    }

    // Reserve before contacting OpenAI, including attempts that subsequently fail.
    @Transactional
    public void reserve() {
        Usage usage = jdbc.queryForObject("SELECT * FROM assistant_usage WHERE id = 1 FOR UPDATE",
                (row, index) -> new Usage(
                        row.getObject("minute_start", OffsetDateTime.class),
                        row.getObject("day_start", LocalDate.class),
                        row.getObject("month_start", LocalDate.class),
                        row.getInt("minute_requests"), row.getInt("daily_requests"), row.getInt("monthly_requests")));
        if (usage == null) throw ChatException.unavailable();

        OffsetDateTime minute = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);
        LocalDate day = minute.toLocalDate();
        LocalDate month = day.withDayOfMonth(1);
        int minuteCount = minute.toInstant().equals(usage.minute().toInstant()) ? usage.minuteCount() : 0;
        int dayCount = day.equals(usage.day()) ? usage.dayCount() : 0;
        int monthCount = month.equals(usage.month()) ? usage.monthCount() : 0;

        if (minuteCount >= minuteLimit || dayCount >= dailyLimit || monthCount >= monthlyLimit) {
            throw new ChatException(HttpStatus.TOO_MANY_REQUESTS, "ASSISTANT_LIMIT_REACHED",
                    "The assistant has reached its usage limit. Please try again later.");
        }

        jdbc.update("""
                UPDATE assistant_usage SET minute_start = ?, day_start = ?, month_start = ?,
                    minute_requests = ?, daily_requests = ?, monthly_requests = ? WHERE id = 1
                """, minute, day, month, minuteCount + 1, dayCount + 1, monthCount + 1);
    }

    private record Usage(OffsetDateTime minute, LocalDate day, LocalDate month,
                         int minuteCount, int dayCount, int monthCount) {}
}
