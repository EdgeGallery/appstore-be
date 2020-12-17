DROP TABLE IF EXISTS catalog_package_table;
DROP TABLE IF EXISTS APP_TABLE;
DROP TABLE IF EXISTS CSAR_PACKAGE_SCORE;
DROP TABLE IF EXISTS app_store_table;

    create TABLE if not exists catalog_package_table (
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

    create TABLE if not exists app_table (
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

    create TABLE if not exists csar_package_score (
        COMMENTID                serial,
        USERID                   VARCHAR(100)       NULL,
        USERNAME                 VARCHAR(100)       NULL,
        APPID                    VARCHAR(200)       NOT NULL,
        COMMENTS                 TEXT               NULL,
        SCORE                    NUMERIC(2,1)       NULL,
        COMMENTTIME              TIMESTAMP          NULL,
        CONSTRAINT csar_package_score_pkey PRIMARY KEY (COMMENTID)
    );

    create TABLE if not exists message_table (
        MESSAGEID                VARCHAR(100)       NOT NULL,
        RESULT                   VARCHAR(100)       NULL,
        READED                   boolean            default false,
        NAME                     VARCHAR(100)       NULL,
        PROVIDER                 VARCHAR(100)       NULL,
        VERSION                  VARCHAR(100)       NULL,
        MESSAGETYPE              VARCHAR(20)        NULL,
        SOURCEAPPSTORE           VARCHAR(100)       NULL,
        TARGETAPPSTORE           VARCHAR(100)       NULL,
        TIME                     VARCHAR(100)       NULL,
        DESCRIPTION              VARCHAR(255)       NULL,
        ATPTESTSTATUS            VARCHAR(50)        NULL,
        ATPTESTTASKID            VARCHAR(100)       NULL,
        ATPTESTREPORTURL         VARCHAR(255)       NULL,
        PACKAGEDOWNLOADURL       VARCHAR(255)       NULL,
        ICONDOWNLOADURL          VARCHAR(255)       NULL,
        AFFINITY                 VARCHAR(100)       NULL,
        SHORTDESC                TEXT               NULL,
        INDUSTRY                 VARCHAR(100)       NULL,
        TYPE                     VARCHAR(50)        NULL,
        CONSTRAINT message_table_pkey PRIMARY KEY (MESSAGEID)
    );

    create TABLE if not exists app_store_table (
        APPSTOREID              VARCHAR(64)         NOT NULL,
        NAME                    VARCHAR(128)        NOT NULL,
        VERSION                 VARCHAR(64)         NOT NULL,
        COMPANY                 VARCHAR(128)        NULL,
        URL                     VARCHAR(256)        NOT NULL,
        SCHEMA                  VARCHAR(15)         NULL,
        APPPUSHINTF             VARCHAR(256)        NULL,
        APPTRANSID              VARCHAR(64)         NULL,
        ADDEDTIME               TIMESTAMP           NULL,
        MODIFIEDTIME            TIMESTAMP           NULL,
        DESCRIPTION             VARCHAR(256)        NULL,
        CONSTRAINT app_store_table_pkey PRIMARY KEY (APPSTOREID)
    );

INSERT INTO catalog_package_table(
	packageid, packageaddress, iconaddress, appname, version, applicationtype, appid, userid, username, PROVIDER, SHORTDESC)
	VALUES ('packageid-0002', '/package/test/face.csar', '/user/test/icon.png', 'face game', 'v1.0', 'game', 'appid-0001', 'userid-0001', 'test-user01', 'xxxx', 'game');

INSERT INTO app_store_table(
	appstoreid, name, version, company, url, schema, apppushintf, apptransid, addedtime, modifiedtime, description)
	VALUES ('appstore-test-0001', 'liantong', 'v1.0', 'liantong', 'http://127.0.0.1:8099', 'http', '', '', null, null, 'description-5555');
INSERT INTO app_store_table(
	appstoreid, name, version, company, url, schema, apppushintf, apptransid, addedtime, modifiedtime, description)
	VALUES ('appstore-test-0002', '移动', 'v1.0', '移动', 'http://127.0.0.1:8099', 'http', '', '', null, null, 'description-5555');
