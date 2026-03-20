create table if not exists notification_preferences (
    user_id varchar(100) not null,
    team_id varchar(100) not null,
    event_type varchar(100) not null,
    channel varchar(50) not null,
    status varchar(50) not null,
    created_at timestamp default current_timestamp,
    primary key (user_id, team_id, event_type, channel)
);

create table if not exists internal_notifications (
    notification_id varchar(255) primary key,
    request_id varchar(255) not null,
    event_type varchar(100) not null,
    channel varchar(50) not null,
    recipient_type varchar(50) not null,
    recipient_id varchar(100) not null,
    title varchar(255) not null,
    message varchar(1000) not null,
    status varchar(50) not null,
    occurred_at timestamp not null,
    metadata_json text,
    created_at timestamp default current_timestamp
);

insert into notification_preferences (user_id, team_id, event_type, channel, status)
values ('u_demo_001', 't_demo', 'limit.exceeded', 'team_digest', 'active');

insert into notification_preferences (user_id, team_id, event_type, channel, status)
values ('u_demo_001', 't_demo', 'cost.calculated', 'team_digest', 'active');

insert into notification_preferences (user_id, team_id, event_type, channel, status)
values ('u_demo_001', 't_demo', 'limit.exceeded', 'user_inbox', 'active');

insert into internal_notifications (
    notification_id, request_id, event_type, channel, recipient_type, recipient_id,
    title, message, status, occurred_at, metadata_json
) values (
    'seed.limit.exceeded.team',
    'req_seed_limit_team',
    'limit.exceeded',
    'team_digest',
    'team',
    't_demo',
    'Limit exceeded for svc_doc_summary',
    'Request req_seed_limit_team exceeded DAILY_TOKEN threshold 10000 with observed value 12550',
    'OPEN',
    '2026-03-20 10:20:30',
    '{"service_id":"svc_doc_summary","workflow_id":"wf_monthly_report","limit_type":"DAILY_TOKEN","threshold":"10000","observed_value":"12550"}'
);

insert into internal_notifications (
    notification_id, request_id, event_type, channel, recipient_type, recipient_id,
    title, message, status, occurred_at, metadata_json
) values (
    'seed.limit.exceeded.user',
    'req_seed_limit_user',
    'limit.exceeded',
    'user_inbox',
    'user',
    'u_demo_001',
    'Limit exceeded for svc_doc_summary',
    'Request req_seed_limit_user exceeded DAILY_TOKEN threshold 10000 with observed value 12550',
    'OPEN',
    '2026-03-20 10:21:30',
    '{"service_id":"svc_doc_summary","workflow_id":"wf_monthly_report","limit_type":"DAILY_TOKEN","threshold":"10000","observed_value":"12550"}'
);

insert into internal_notifications (
    notification_id, request_id, event_type, channel, recipient_type, recipient_id,
    title, message, status, occurred_at, metadata_json
) values (
    'seed.cost.calculated.team',
    'req_seed_cost_team',
    'cost.calculated',
    'team_digest',
    'team',
    't_demo',
    'Cost alert for svc_doc_summary',
    'Request req_seed_cost_team exceeded cost threshold at KRW 1184.23 using gpt-4o-mini',
    'OPEN',
    '2026-03-20 10:15:30',
    '{"service_id":"svc_doc_summary","workflow_id":"wf_monthly_report","model":"gpt-4o-mini","currency":"KRW","cost":"1184.23"}'
);
