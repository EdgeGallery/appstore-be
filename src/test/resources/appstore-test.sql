DROP TABLE IF EXISTS catalog_package_table;
DROP TABLE IF EXISTS APP_TABLE;
DROP TABLE IF EXISTS CSAR_PACKAGE_SCORE;

    CREATE TABLE if not exists catalog_package_table (
    	PACKAGEID                VARCHAR(200)       NOT NULL,
    	PACKAGEADDRESS           VARCHAR(200)       NULL,
    	ICONADDRESS              VARCHAR(200)       NULL,
    	SIZE                     VARCHAR(100)       NULL,
    	FILESTRUCTURE            TEXT               NULL,
    	CREATETIME               VARCHAR(100)       NULL,
    	SHORTDESC	             TEXT		        NULL,
    	APPNAME                  VARCHAR(100)       NULL,
    	VERSION                  VARCHAR(20)        NULL,
    	APPLICATIONTYPE          VARCHAR(300)       NULL,
    	MARKDOWNCONTENT          TEXT			    NULL,
    	AFFINITY                 VARCHAR(100)       NULL,
    	INDUSTRY                 VARCHAR(100)       NULL,
    	CONTACT                  VARCHAR(100)       NULL,
    	APPID                    VARCHAR(100)       NULL,
    	USERID                   VARCHAR(100)       NULL,
    	USERNAME                 VARCHAR(100)       NULL,
    	TESTTASKID               VARCHAR(100)       NULL,
    	STATUS                   VARCHAR(100)       NULL,
    	PROVIDER                 VARCHAR(100)       NULL,
    	CONSTRAINT catalog_package_table_pkey PRIMARY KEY (PACKAGEID)
    );

    CREATE TABLE if not exists app_table (
    	APPID                    VARCHAR(200)       NOT NULL,
    	APPNAME                  VARCHAR(100)       NULL,
    	APPLICATIONTYPE          VARCHAR(300)       NULL,
    	SHORTDESC	             TEXT		        NULL,
    	PROVIDER                 VARCHAR(300)       NULL,
    	APPINTRODUCTION		     TEXT			    NULL,
    	DOWNLOADCOUNT            INT                NULL,
    	AFFINITY                 VARCHAR(100)       NULL,
    	INDUSTRY                 VARCHAR(100)       NULL,
    	CONTACT                  VARCHAR(100)       NULL,
    	USERID                   VARCHAR(100)       NULL,
    	USERNAME                 VARCHAR(100)       NULL,
    	CREATETIME               TIMESTAMP          NULL,
    	MODIFYTIME               TIMESTAMP          NULL,
        SCORE                    NUMERIC(2,1)       NULL,
        STATUS                   VARCHAR(50)       NULL,
    	CONSTRAINT app_table_pkey PRIMARY KEY (APPID)
    );

    CREATE TABLE if not exists csar_package_score (
        COMMENTID                serial,
        USERID                   VARCHAR(100)       NULL,
        USERNAME                 VARCHAR(100)       NULL,
        APPID                    VARCHAR(200)       NOT NULL,
        COMMENTS                 TEXT               NULL,
        SCORE                    NUMERIC(2,1)       NULL,
        COMMENTTIME              TIMESTAMP          NULL,
        CONSTRAINT csar_package_score_pkey PRIMARY KEY (COMMENTID)
    );

INSERT INTO catalog_package_table(
	packageid, packageaddress, iconaddress, appname, version, applicationtype, appid, userid, username)
	VALUES ('packageid-0002', '/package/test/face.csar', '/user/test/icon.png', 'face game', 'v1.0', 'game', 'appid-0001', 'userid-0001', 'test-user01');