-- Configurations:
INSERT INTO autoscale_configuration (id, project, service, metrics, metric_name, scale_up, scale_down)
VALUES (0, 'usecase', 'emailservice', 'QUEUE_DEPTH', 'email-queue', 10, 0);

INSERT INTO autoscale_configuration (id, project, service, metrics, metric_name, scale_up, scale_down)
VALUES (1, 'usecase', 'printservice', 'QUEUE_DEPTH', 'print-queue', 10, 0);

INSERT INTO autoscale_configuration (id, project, service, metrics, metric_name, scale_up, scale_down)
VALUES (2, 'usecase', 'frontendservice', 'NUMBER_OF_HTTP_CONNECTIONS', '', 20, 10);

-- Service-Limits:
INSERT INTO service_limit (id, project, service, min_pods, max_pods)
VALUES (0, 'usecase', 'frontendservice', 1, 5);