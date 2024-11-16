CREATE TABLE OAUTH2_CLIENT_REGISTERED (
    REGISTRATION_ID VARCHAR(100) PRIMARY KEY,
    CLIENT_ID VARCHAR(100),
    CLIENT_SECRET VARCHAR(100),
    CLIENT_AUTHENTICATION_METHOD VARCHAR(100),
    AUTHORIZATION_GRANT_TYPE VARCHAR(100),
    CLIENT_NAME VARCHAR(100),
    REDIRECT_URI VARCHAR(100),
    SCOPES VARCHAR(100),
    AUTHORIZATION_URI VARCHAR(100),
    TOKEN_URI VARCHAR(100),
    JWK_SET_URI VARCHAR(100),
    ISSUER_URI VARCHAR(100),
    USER_INFO_URI VARCHAR(100),
    USER_INFO_AUTHENTICATION_METHOD VARCHAR(100),
    USER_NAME_ATTRIBUTE_NAME VARCHAR(100),
    CONFIGURATION_METADATA  VARCHAR(100)
);