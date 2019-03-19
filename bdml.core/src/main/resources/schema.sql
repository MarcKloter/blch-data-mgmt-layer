CREATE TABLE DATA(
    identifier BINARY(32) PRIMARY KEY,
    capability BINARY(32) DEFAULT NULL,
    temporary BOOLEAN NOT NULL,
);

//Todo: Think of an isValid Cache -- is it worth it???

CREATE TABLE LINKS(
    source BINARY(32) NOT NULL,
    target BINARY(32) NOT NULL,
    temporary BOOLEAN NOT NULL,
    amend BOOLEAN NOT NULL,
);

CREATE TABLE FINALIZATION_LOG(
     identifier BINARY(32) PRIMARY KEY,
);

CREATE INDEX LINKS_INDEX ON LINKS(source);
CREATE INDEX LINKS_CHECK_INDEX ON LINKS(source, target);


CREATE TABLE VARIABLES(
    key VARCHAR(255) PRIMARY KEY,
    value VARCHAR(255)
);