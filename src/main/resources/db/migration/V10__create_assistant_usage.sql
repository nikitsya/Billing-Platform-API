CREATE TABLE assistant_usage
(
    id               INTEGER PRIMARY KEY CHECK (id = 1),
    minute_start     TIMESTAMP WITH TIME ZONE NOT NULL,
    day_start        DATE                     NOT NULL,
    month_start      DATE                     NOT NULL,
    minute_requests  INTEGER                  NOT NULL DEFAULT 0,
    daily_requests   INTEGER                  NOT NULL DEFAULT 0,
    monthly_requests INTEGER                  NOT NULL DEFAULT 0
);

INSERT INTO assistant_usage (id, minute_start, day_start, month_start)
VALUES (1, '2000-01-01 00:00:00+00', '2000-01-01', '2000-01-01');
