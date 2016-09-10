INSERT INTO configuration (id, project, service, metrics, metric_name, scale_up, scale_down)
VALUES (2, 'usecase', 'frontendservice', 'NUMBER_OF_HTTP_CONNECTIONS', '', 20, 10);

CREATE TABLE service_limit
(
  id bigint NOT NULL,
  project character varying(255) NOT NULL,
  service character varying(255) NOT NULL,
  min_pods bigint NOT NULL,
  max_pods bigint NOT NULL,
  CONSTRAINT servicelimit_pkey PRIMARY KEY (id),
  CONSTRAINT servicelimit_domainkey UNIQUE (project, service)
);


INSERT INTO service_limit (id, project, service, min_pods, max_pods)
values (1, 'usecase', 'frontendservice', 1, 5);