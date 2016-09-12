-- Configurations:
INSERT INTO configuration (id, project, service, metrics, metric_name, scale_up, scale_down)
VALUES (10000, 'usecase', 'emailservice', 'QUEUE_DEPTH', 'email-queue', 10, 0);

INSERT INTO configuration (id, project, service, metrics, metric_name, scale_up, scale_down)
VALUES (10001, 'usecase', 'printservice', 'QUEUE_DEPTH', 'print-queue', 10, 0);

INSERT INTO configuration (id, project, service, metrics, metric_name, scale_up, scale_down)
VALUES (10002, 'usecase', 'frontendservice', 'NUMBER_OF_HTTP_CONNECTIONS', '', 20, 10);


-- Service-Limits:
INSERT INTO service_limit (id, project, service, min_pods, max_pods)
values (10000, 'usecase', 'frontendservice', 1, 5);