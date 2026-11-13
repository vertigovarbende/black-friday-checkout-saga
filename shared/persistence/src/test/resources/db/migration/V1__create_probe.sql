-- BaseEntityIT için test tablosu. BaseEntity kolonları: version, created_at, updated_at.
CREATE TABLE probe
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(100)             NOT NULL,
    version    BIGINT                   NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
