CREATE TABLE accounts (
  id UUID NOT NULL,
   broker VARCHAR(100) NOT NULL,
   amount DECIMAL(9, 2) NOT NULL DEFAULT 0,
   deleted boolean NOT NULL DEFAULT FALSE,
   CONSTRAINT pk_accounts PRIMARY KEY (id)
);

ALTER TABLE accounts ADD CONSTRAINT uc_accounts_broker UNIQUE (broker);

INSERT INTO accounts (id, broker) values ('3f2794e5-f74a-46c4-9792-12308e740f97', 'C6 BANK');