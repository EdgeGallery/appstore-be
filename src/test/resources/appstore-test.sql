DROP TABLE IF EXISTS catalog_package_table;

CREATE TABLE catalog_package_table (
	VERSIONID                VARCHAR(200)       NOT NULL,
	PACKAGEADDRESS           VARCHAR(200)       NULL,
	ICONADDRESS              VARCHAR(200)       NULL,
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
	CONSTRAINT catalog_package_table_pkey PRIMARY KEY (VERSIONID)
);

INSERT INTO catalog_package_table VALUES ('44a00b12c13b43318d21840793549337',
'home/appstore/30ec10f4a43041e6a6198ba824311af2/30ec10f4a43041e6a6198ba824311af2.csar',
'home/appstore/30ec10f4a43041e6a6198ba824311af2/30ec10f4a43041e6a6198ba824311af2.png',
'0', '{"name":"ccd309397d904122af2eb14c9f2a7195","childs":[]}', '2020-03-13 11:40:32.171795', 'adf',
'car_networking_app', '0.8', 'Video', 'face_recognition_service...', 'GPU','Smart Campus','xxx@xxx.xxx', '30ec10f4a43041e6a6198ba824311af2',
'63c79ce8-5511-4360-9ebf-615f4ada48cb','testuser001');


INSERT INTO catalog_package_table VALUES ('44a00b12c13b43318d21840793549338',
'home/appstore/30ec10f4a43041e6a6198ba824311af2/30ec10f4a43041e6a6198ba824311af2.csar',
'home/appstore/30ec10f4a43041e6a6198ba824311af2/30ec10f4a43041e6a6198ba824311af2.png',
'0', '{"name":"ccd309397d904122af2eb14c9f2a7195","childs":[]}', '2020-03-13 11:40:32.171795', 'adf',
'car_networking_app', '0.8', 'Video', 'face_recognition_service...', 'GPU','Smart Campus','xxx@xxx.xxx', '30ec10f4a43041e6a6198ba824311af3',
'001','testuser001');


INSERT INTO catalog_package_table VALUES ('30ec10f4a43041e6a6198ba824311af5',
'home/appstore/30ec10f4a43041e6a6198ba824311af2/30ec10f4a43041e6a6198ba824311af4.csar',
'home/appstore/30ec10f4a43041e6a6198ba824311af2/30ec10f4a43041e6a6198ba824311af4.png',
'0', '{"name":"ccd309397d904122af2eb14c9f2a7195","childs":[]}', '2020-03-13 11:40:32.171795', 'adf',
'car_networking_app', '0.8', 'Video', 'face_recognition_service...', 'GPU','Smart Campus','xxx@xxx.xxx', '30ec10f4a43041e6a6198ba824311af2',
'63c79ce8-5511-4360-9ebf-615f4ada48cb','testuser001');

DROP TABLE IF EXISTS APP_TABLE;

CREATE TABLE APP_TABLE (
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
	CONSTRAINT app_table_pkey PRIMARY KEY (APPID)
);

INSERT INTO app_table VALUES ('30ec10f4a43041e6a6198ba824311af2', 'car_networking_app', 'Video', 'Desc',
                                     'Company', 'content', '0', 'GPU','Smart Campus','xxx@xxx.xxx', '001', 'testuser001', '2020-03-13 12:05:17.255272',
                                     '2020-03-13 12:05:17.255272', '5.0');
INSERT INTO app_table VALUES ('30ec10f4a43041e6a6198ba824311af3', 'AR', 'Video', 'Desc',
                                     'Company', 'content', '0', 'GPU','Smart Campus', 'xxx@xxx.xxx', '5abdd29d-b281-4f96-8339-b5621a67d217', 'testuser001', '2020-03-13 12:05:17.255272',
                                     '2020-03-13 12:05:17.255272', '5.0');

DROP TABLE IF EXISTS CSAR_PACKAGE_SCORE;

CREATE TABLE CSAR_PACKAGE_SCORE (
    COMMENTID                serial,
    USERID                   VARCHAR(100)       NULL,
    USERNAME                 VARCHAR(100)       NULL,
    APPID                    VARCHAR(200)       NOT NULL,
    COMMENTS                 TEXT               NULL,
    SCORE                    NUMERIC(2,1)       NULL,
    COMMENTTIME              TIMESTAMP          NULL,
    CONSTRAINT csar_package_score_pkey PRIMARY KEY (COMMENTID)
);
