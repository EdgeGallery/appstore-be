DROP TABLE IF EXISTS catalog_package_table;
DROP TABLE IF EXISTS APP_TABLE;
DROP TABLE IF EXISTS CSAR_PACKAGE_SCORE;
DROP TABLE IF EXISTS app_store_table;
DROP TABLE IF EXISTS PUSHABLE_PACKAGE_TABLE;
DROP TABLE IF EXISTS message_table;

    create TABLE if not exists catalog_package_table (
    	PACKAGEID                VARCHAR(200)       NOT NULL,
    	PACKAGEADDRESS           VARCHAR(200)       NULL,
    	ICONADDRESS              VARCHAR(200)       NULL,
    	DEMOVIDEOADDRESS         VARCHAR(200)       NULL,
    	SIZE                     VARCHAR(100)       NULL,
    	FILESTRUCTURE            TEXT               NULL,
    	CREATETIME               TIMESTAMP          NULL,
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
        DEMOVIDEODOWNLOADURL     VARCHAR(255)       NULL,
        AFFINITY                 VARCHAR(100)       NULL,
        SHORTDESC                TEXT               NULL,
        INDUSTRY                 VARCHAR(100)       NULL,
        TYPE                     VARCHAR(50)        NULL,
        CONSTRAINT message_table_pkey PRIMARY KEY (MESSAGEID)
    );

    create TABLE if not exists app_store_table (
        APPSTOREID               VARCHAR(64)        NOT NULL,
        APPSTORENAME             VARCHAR(128)       NULL,
        APPSTOREVERSION          VARCHAR(64)        NOT NULL,
        COMPANY                  VARCHAR(128)       NULL,
        URL                      VARCHAR(256)       NOT NULL,
        SCHEMA                   VARCHAR(16)        NULL,
        APPPUSHINTF              VARCHAR(256)       NULL,
        APPDTRANSID               VARCHAR(64)        NULL,
        DESCRIPTION              VARCHAR(256)       NULL,
        ADDEDTIME                TIMESTAMP          NOT NULL,
        MODIFIEDTIME             TIMESTAMP          NULL,
        CONSTRAINT app_store_table_pkey PRIMARY KEY (APPSTOREID)
    );

    CREATE TABLE IF NOT EXISTS PUSHABLE_PACKAGE_TABLE (
        PACKAGEID  VARCHAR(64) NOT NULL,
        ATPTESTREPORTURL VARCHAR(100) NOT NULL,
        LATESTPUSHTIME TIMESTAMP NOT NULL,
        PUSHTIMES INTEGER NOT NULL DEFAULT 0,
        SOURCEPLATFORM VARCHAR(100) NOT NULL,
        CONSTRAINT PUSHABLE_PACKAGE_TABLE_PKEY PRIMARY KEY (PACKAGEID)
    );

INSERT INTO app_table(
	appid, appname, applicationtype, shortdesc, provider, appintroduction, downloadcount, affinity, industry, contact, userid, username, createtime, modifytime, score, STATUS)
	VALUES ('appid-test-0001', 'app-001', 'game', 'shortdesc', 'provider', 'appintroduction', 5, 'affinity', 'industry', 'contactcontact', 'test-userid-0001', 'test-username-0001', now(), now(), 3.2, 'Published');

INSERT INTO catalog_package_table(
	packageid, packageaddress, iconaddress, demovideoaddress, size, filestructure, createtime, shortdesc, appname,
	version, applicationtype, markdowncontent, affinity, industry, contact, appid, userid, username, status, TESTTASKID, PROVIDER)
	VALUES ('packageid-0003', '/package/test/face.csar', '/user/test/icon.png', '', 10004, 'file-trees', now(),
	'shortdesc', 'appname', 'version', 'game', 'markdowncontent', 'affinity', 'industry', 'contact', 'appid-test-0001',
	'test-userid-0001', 'test-username-0002', 'Published', 'apt-taskid-0002', 'PROVIDER');

INSERT INTO catalog_package_table(
	packageid, packageaddress, iconaddress, demovideoaddress, size, filestructure, createtime, shortdesc, appname,
	version, applicationtype, markdowncontent, affinity, industry, contact, appid, userid, username, status, TESTTASKID, PROVIDER)
	VALUES ('packageid-0002', '/package/test/face.csar', '/user/test/icon.png', '', 10002, 'file-trees', now(),
	'shortdesc', 'appname', 'version', 'game', 'markdowncontent', 'affinity', 'industry', 'contact', 'appid-test-0001',
	'test-userid-0001', 'test-username-0001', 'Published', 'apt-taskid-0001', 'PROVIDER');

INSERT INTO app_store_table(
	appstoreid, APPSTORENAME, APPSTOREVERSION, company, url, schema, apppushintf, APPDTRANSID, addedtime, modifiedtime, description)
	VALUES ('appstore-test-0001', 'liantong', 'v1.0', 'liantong', 'http://127.0.0.1:8099', 'http', '', '', now(), null, 'description-5555');

INSERT INTO app_store_table(
	appstoreid, APPSTORENAME, APPSTOREVERSION, company, url, schema, apppushintf, APPDTRANSID, addedtime, modifiedtime, description)
	VALUES ('appstore-test-0002', '移动', 'v1.0', '移动', 'http://127.0.0.1:8099', 'http', '', '', now(), null, 'description-5555');
