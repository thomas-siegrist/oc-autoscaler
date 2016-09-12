CREATE TABLE configuration
(
  id bigint NOT NULL,
  project character varying(255) NOT NULL,
  service character varying(255) NOT NULL,
  metric_name character varying(255) NOT NULL,
  metrics character varying(255) NOT NULL,
  scale_up bigint NOT NULL,
  scale_down bigint NOT NULL,
  CONSTRAINT configuration_pkey PRIMARY KEY (id),
  CONSTRAINT domainkey UNIQUE (project, service, metrics, metric_name)
);

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