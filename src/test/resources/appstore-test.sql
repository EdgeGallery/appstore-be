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
        SHORTDESC                TEXT               NULL,
        APPNAME                  VARCHAR(100)       NULL,
        VERSION                  VARCHAR(20)        NULL,
        APPLICATIONTYPE          VARCHAR(300)       NULL,
        DEPLOYMODE               VARCHAR(100)       NULL,
        MARKDOWNCONTENT          TEXT               NULL,
        AFFINITY                 VARCHAR(100)       NULL,
        INDUSTRY                 VARCHAR(100)       NULL,
        CONTACT                  VARCHAR(100)       NULL,
        APPID                    VARCHAR(100)       NULL,
        USERID                   VARCHAR(100)       NULL,
        USERNAME                 VARCHAR(100)       NULL,
        TESTTASKID               VARCHAR(100)       NULL,
        STATUS                   VARCHAR(100)       NULL,
        PROVIDER                 VARCHAR(100)       NULL,
        SHOWTYPE                 VARCHAR(100)       NOT NULL DEFAULT 'public',
        EXPERIENCEABLE           boolean            DEFAULT false,
        INSTANCEPACKAGEID        VARCHAR(255)       NULL,
        INSTANCETENENTID         VARCHAR(100)       NULL,
        APPINSTANCEID            VARCHAR(100)       NULL,
        CONSTRAINT catalog_package_table_pkey PRIMARY KEY (PACKAGEID)
    );

    create TABLE if not exists app_table (
        APPID                    VARCHAR(200)       NOT NULL,
        APPNAME                  VARCHAR(100)       NULL,
        APPLICATIONTYPE          VARCHAR(300)       NULL,
        DEPLOYMODE               VARCHAR(100)       NULL,
        SHORTDESC                TEXT               NULL,
        PROVIDER                 VARCHAR(300)       NULL,
        APPINTRODUCTION          TEXT           NULL,
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
        SHOWTYPE                 VARCHAR(100)       NOT NULL DEFAULT 'public',
        ISHOTAPP                 boolean            DEFAULT false,
        EXPERIENCEABLE           boolean            DEFAULT false,
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


CREATE TABLE IF NOT EXISTS tbl_service_host (
  host_id varchar(50)  NOT NULL DEFAULT NULL,
  name varchar(100)  DEFAULT NULL,
  user_id varchar(100)  DEFAULT NULL,
  address varchar(255)  DEFAULT NULL,
  architecture varchar(100)  DEFAULT NULL,
  status varchar(20)  DEFAULT NULL,
  ip varchar(20)  DEFAULT NULL,
  os varchar(255)  DEFAULT NULL,
  port_range_min int DEFAULT '-1',
  port_range_max int DEFAULT '-1',
  port int DEFAULT '-1',
  protocol varchar(20)  DEFAULT NULL,
  delete int DEFAULT NULL,
  user_name varchar(100)  DEFAULT NULL,
  password varchar(255)  DEFAULT NULL,
  mec_host varchar(100)  DEFAULT NULL
)
;


CREATE TABLE IF NOT EXISTS tbl_host_log (
  log_id varchar(50) NOT NULL,
  host_ip varchar(50) NOT NULL,
  user_name varchar(50) DEFAULT NULL,
  user_id varchar(50) DEFAULT NULL,
  project_id varchar(50)  DEFAULT NULL,
  project_name varchar(50) DEFAULT NULL,
  app_instances_id varchar(50) DEFAULT NULL,
  deploy_time varchar(50) DEFAULT NULL,
  status varchar(50) NOT NULL,
  operation varchar(50) NOT NULL,
  host_id varchar(50) DEFAULT NULL
)
;

   CREATE TABLE IF NOT EXISTS tbl_uploaded_file (
  file_id varchar(50)  NOT NULL DEFAULT NULL,
  file_name varchar(255)  DEFAULT NULL,
  is_temp int DEFAULT NULL,
  user_id varchar(50)  DEFAULT NULL,
  upload_date timestamp DEFAULT NULL,
  file_path varchar(255)  DEFAULT NULL,
  CONSTRAINT tbl_uploaded_file_pkey PRIMARY KEY (file_id)
)
;

INSERT INTO app_table(
    appid, appname, applicationtype, shortdesc, provider, appintroduction, downloadcount, affinity, industry, contact, userid, username, createtime, modifytime, score, STATUS, ISHOTAPP)
    VALUES ('appid-test-0001', 'app-001', 'game', 'shortdesc', 'provider', 'appintroduction', 5, 'affinity', 'industry', 'contactcontact', 'test-userid-0001', 'test-username-0001', now(), now(), 3.2, 'Published', false );

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
    VALUES ('a09bca74-04cb-4bae-9ee2-9c5072ec9d4b', 'liantong', 'v1.0', 'liantong', 'http://127.0.0.1:8099', 'http', '', '', now(), null, 'description-5555');

INSERT INTO app_store_table(
    appstoreid, APPSTORENAME, APPSTOREVERSION, company, url, schema, apppushintf, APPDTRANSID, addedtime, modifiedtime, description)
    VALUES ('02ef9eeb-d50e-4835-8d05-e5fdb87b7596', '移动', 'v1.0', '移动', 'http://127.0.0.1:8099', 'http', '', '', now(), null, 'description-5555');


MERGE INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('7dd477d8-bcc0-4e2a-a48d-2b587a30026a', 'Face Recognition service plus.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/7dd477d8-bcc0-4e2a-a48d-2b587a30026a');
MERGE INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('d0f8fa57-2f4c-4182-be33-0a508964d04a', 'Face Recognition service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/d0f8fa57-2f4c-4182-be33-0a508964d04a');
MERGE INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('540e0817-f6ea-42e5-8c5b-cb2daf9925a3', 'Service Discovery.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/540e0817-f6ea-42e5-8c5b-cb2daf9925a3');
MERGE INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('7c544903-aa4f-40e0-bd8c-cf6e17c37c12', 'Bandwidth service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/7c544903-aa4f-40e0-bd8c-cf6e17c37c12');
MERGE INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('688f259e-48eb-407d-8604-7feb19cf1f44', 'Location service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/688f259e-48eb-407d-8604-7feb19cf1f44');
MERGE INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('9f1f13a0-8554-4dfa-90a7-d2765238fca7', 'Traffic service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/9f1f13a0-8554-4dfa-90a7-d2765238fca7');


MERGE INTO tbl_service_host (host_id, name, address, architecture, status, ip, os, port_range_min, port_range_max, port, protocol, delete) KEY(host_id) VALUES ('3c55ac26-60e9-42c0-958b-1bf7ea4da60a', 'Node1', 'XIAN', 'X86', 'NORMAL', '127.0.0.1', 'Ubuntu', 30000, 32767, 30201, 'http', null);

MERGE INTO tbl_service_host(host_id,name,address,architecture,status,ip,port,os,port_range_min,port_range_max, user_id) KEY(host_id) VALUES ('c8aac2b2-4162-40fe-9d99-0630e3245cf7', 'host-1', 'xian', 'ARM','NORMAL','10.1.12.1',8999,'liunx',30000,300001,'e111f3e7-90d8-4a39-9874-ea6ea6752ef6');
MERGE INTO tbl_service_host(host_id,name,address,architecture,status,ip,port,os,port_range_min,port_range_max, user_id) KEY(host_id) VALUES ('c8aac2b2-4162-40fe-9d99-0630e3245cdd', 'host-1', 'xian', 'ARM','NORMAL','10.1.12.1',8999,'liunx',30000,300001,'e111f3e7-90d8-4a39-9874-ea6ea6752eaa');

