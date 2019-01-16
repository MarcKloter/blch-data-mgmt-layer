CREATE TABLE DATA(
    identifier BINARY(32) PRIMARY KEY,
    capability BINARY(32) NOT NULL,
    attachment BOOLEAN DEFAULT FALSE,
    recursively_parsed BOOLEAN DEFAULT FALSE
);

CREATE TABLE ATTACHMENTS(
    identifier BINARY(32),
    attached_to BINARY(32),
    FOREIGN KEY (identifier) REFERENCES DATA(identifier),
    FOREIGN KEY (attached_to) REFERENCES DATA(identifier),
    PRIMARY KEY(identifier, attached_to)
);

CREATE TABLE VARIABLES(
    key VARCHAR(255) PRIMARY KEY,
    value VARCHAR(255)
);