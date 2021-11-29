drop table IF EXISTS catalog_package_table;
drop table IF EXISTS APP_TABLE;
drop table IF EXISTS CSAR_PACKAGE_SCORE;
drop table IF EXISTS app_store_table;
drop table IF EXISTS PUSHABLE_PACKAGE_TABLE;
drop table IF EXISTS message_table;
DROP TABLE IF  EXISTS PACKAGE_UPLOAD_PROGRESS_TABLE;

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
    STARTEXPTIME             VARCHAR(100)       NULL,
    EXPERIENCEABLEIP         VARCHAR(100)       NULL,
    MECHOST                  VARCHAR(100)       NULL,
    EXPERIENCESTATUS         INT                NULL,
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
    ISFREE                   boolean            DEFAULT true,
    PRICE                    NUMERIC(10,2)      NULL,
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

create TABLE IF NOT EXISTS PUSHABLE_PACKAGE_TABLE (
    PACKAGEID  VARCHAR(64) NOT NULL,
    ATPTESTREPORTURL VARCHAR(100) NOT NULL,
    LATESTPUSHTIME TIMESTAMP NOT NULL,
    PUSHTIMES INTEGER NOT NULL DEFAULT 0,
    SOURCEPLATFORM VARCHAR(100) NOT NULL,
    CONSTRAINT PUSHABLE_PACKAGE_TABLE_PKEY PRIMARY KEY (PACKAGEID)
);

create TABLE IF NOT EXISTS tbl_service_host (
    host_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) DEFAULT NULL,
    name VARCHAR(100) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    architecture VARCHAR(100) DEFAULT NULL,
    status VARCHAR(20) DEFAULT NULL,
    protocol VARCHAR(20) DEFAULT NULL,
    lcm_ip VARCHAR(20) DEFAULT NULL,
    mec_host VARCHAR(20) DEFAULT NULL,
    os VARCHAR(255) DEFAULT NULL,
    port_range_min INTEGER DEFAULT 0,
    port_range_max INTEGER DEFAULT 0,
    port INTEGER DEFAULT 0,
    vnc_port INTEGER DEFAULT NULL,
    parameter text DEFAULT NULL,
    delete boolean DEFAULT NULL,
    ip_count INTEGER DEFAULT 0
);

create TABLE IF NOT EXISTS tbl_uploaded_file (
    file_id varchar(50)  NOT NULL DEFAULT NULL,
    file_name varchar(255)  DEFAULT NULL,
    is_temp int DEFAULT NULL,
    user_id varchar(50)  DEFAULT NULL,
    upload_date timestamp DEFAULT NULL,
    file_path varchar(255)  DEFAULT NULL,
    CONSTRAINT tbl_uploaded_file_pkey PRIMARY KEY (file_id)
);

CREATE TABLE IF NOT EXISTS PACKAGE_SUBSCRIBE_TABLE (
    ID VARCHAR(100) NOT NULL,
    CALLER_ID VARCHAR(100) DEFAULT NULL,
    MEAO_ID VARCHAR(100) DEFAULT NULL,
    NOTIFICATION_URI VARCHAR(255) DEFAULT NULL,
    CONSTRAINT PACKAGE_SUBSCRIBE_TABLE_PKEY PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS PACKAGE_SUBSCRIBE_FILTER_TABLE (
    ID VARCHAR(100) NOT NULL,
    SUBSCRIBE_ID VARCHAR(100) DEFAULT NULL,
    VENDOR VARCHAR(100) DEFAULT NULL,
    TYPE_PACKAGE VARCHAR(100) DEFAULT NULL,
    TYPE_NE VARCHAR(100) DEFAULT NULL,
    CONSTRAINT PACKAGE_SUBSCRIBE_FILTER_TABLE_PKEY PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS THIRD_SYSTEM_TABLE (
    ID VARCHAR(100) NOT NULL,
    SYSTEM_NAME VARCHAR(100) DEFAULT NULL,
    SYSTEM_TYPE VARCHAR(100) DEFAULT NULL,
    VERSION VARCHAR(100) DEFAULT NULL,
    URL VARCHAR(100) DEFAULT NULL,
    IP VARCHAR(100) DEFAULT NULL,
    PORT VARCHAR(100) DEFAULT NULL,
    REGION VARCHAR(100) DEFAULT NULL,
    USERNAME VARCHAR(100) DEFAULT NULL,
    PASSWORD VARCHAR(100) DEFAULT NULL,
    PRODUCT VARCHAR(100) DEFAULT NULL,
    VENDOR VARCHAR(100) DEFAULT NULL,
    TOKEN_TYPE VARCHAR(100) DEFAULT NULL,
    ICON TEXT DEFAULT NULL,
    STATUS VARCHAR(100) DEFAULT NULL,
    CONSTRAINT THIRD_SYSTEM_TABLE_PKEY PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS PACKAGE_UPLOAD_PROGRESS_TABLE (
    ID VARCHAR(100) NOT NULL,
    PACKAGE_ID VARCHAR(100) DEFAULT NULL,
    MEAO_ID VARCHAR(100) DEFAULT NULL,
    STATUS VARCHAR(100) DEFAULT NULL,
    PROGRESS VARCHAR(100) DEFAULT NULL,
    CREATE_TIME TIMESTAMP DEFAULT NULL,
    CONSTRAINT PACKAGE_UPLOAD_PROGRESS_TABLE_PKEY PRIMARY KEY (ID)
);

create TABLE if not exists app_order (
    ORDERID                  VARCHAR(200)       NOT NULL,
    ORDERNUM                 VARCHAR(50)        NOT NULL,
    USERID                   VARCHAR(100)       NOT NULL,
    USERNAME                 VARCHAR(100)       NOT NULL,
    APPID                    VARCHAR(200)       NOT NULL,
    APPPACKAGEID             VARCHAR(200)       NOT NULL,
    ORDERTIME                TIMESTAMP          NOT NULL,
    OPERATETIME              TIMESTAMP          NULL,
    STATUS                   VARCHAR(50)        NOT NULL,
    MECM_HOSTIP              VARCHAR(1024)      NULL,
    MECM_APPID               VARCHAR(200)       NULL,
    MECM_APPPACKAGEID        VARCHAR(200)       NULL,
    MECM_INSTANCEID          VARCHAR(200)       NULL,
    DETAILCN                 text               DEFAULT NULL,
    DETAILEN                 text               DEFAULT NULL,
    CONSTRAINT app_order_pkey PRIMARY KEY (ORDERID),
    CONSTRAINT app_order_uniqueOrderNum UNIQUE (ORDERNUM)
);

insert into app_table(
    appid, appname, applicationtype, shortdesc, provider, appintroduction, downloadcount, affinity, industry, contact, userid, username, createtime, modifytime, score, STATUS, ISHOTAPP, ISFREE, PRICE)
    values ('appid-test-0001', 'app-001', 'game', 'shortdesc', 'provider', 'appintroduction', 5, 'affinity', 'industry', 'contactcontact', 'test-userid-0001', 'test-username-0001', now(), now(), 3.2, 'Published', false, false, 100);

insert into catalog_package_table(
    packageid, packageaddress, iconaddress, demovideoaddress, size, filestructure, createtime, shortdesc, appname,
    version, applicationtype, markdowncontent, affinity, industry, contact, appid, userid, username, status, TESTTASKID, PROVIDER, EXPERIENCESTATUS)
    values ('packageid-0003', '/package/test/face.csar', '/user/test/icon.png', '', 10004, 'file-trees', now(),
    'shortdesc', 'appname', 'version', 'game', 'markdowncontent', 'affinity', 'industry', 'contact', 'appid-test-0001',
    'test-userid-0001', 'test-username-0002', 'Published', 'apt-taskid-0002', 'PROVIDER', 25);

insert into catalog_package_table(
    packageid, packageaddress, iconaddress, demovideoaddress, size, filestructure, createtime, shortdesc, appname, instancetenentid,
    version, applicationtype, markdowncontent, affinity, industry, contact, appid, userid, username, status, TESTTASKID, PROVIDER)
    values ('packageid-0004', '/package/test/face.csar', '/user/test/icon.png', '', 10005, 'file-trees', now(),
    'shortdesc', 'appname', 'a8b4118a-4183-49a9-a915-1d37147c14d8','version', 'game', 'markdowncontent', 'affinity', 'industry', 'contact', 'appid-test-0001',
    'test-userid-0001', 'test-username-0002', 'Published', 'apt-taskid-0002', 'PROVIDER');

insert into catalog_package_table(
    packageid, packageaddress, iconaddress, demovideoaddress, size, filestructure, createtime, shortdesc, appname,
    version, applicationtype, markdowncontent, affinity, industry, contact, appid, userid, username, status, TESTTASKID, PROVIDER)
    values ('packageid-0002', '/package/test/face.csar', '/user/test/icon.png', '', 10002, 'file-trees', now(),
    'shortdesc', 'appname', 'version', 'game', 'markdowncontent', 'affinity', 'industry', 'contact', 'appid-test-0001',
    'test-userid-0001', 'test-username-0001', 'Published', 'apt-taskid-0001', 'PROVIDER');

insert into app_store_table(
    appstoreid, APPSTORENAME, APPSTOREVERSION, company, url, schema, apppushintf, APPDTRANSID, addedtime, modifiedtime, description)
    values ('a09bca74-04cb-4bae-9ee2-9c5072ec9d4b', 'liantong', 'v1.0', 'liantong', 'http://127.0.0.1:8099', 'http', '', '', now(), null, 'description-5555');

insert into app_store_table(
    appstoreid, APPSTORENAME, APPSTOREVERSION, company, url, schema, apppushintf, APPDTRANSID, addedtime, modifiedtime, description)
    values ('02ef9eeb-d50e-4835-8d05-e5fdb87b7596', '移动', 'v1.0', '移动', 'http://127.0.0.1:8099', 'http', '', '', now(), null, 'description-5555');

merge into message_table (MESSAGEID,RESULT,READED,NAME,PROVIDER,VERSION,MESSAGETYPE,SOURCEAPPSTORE,TARGETAPPSTORE,TIME
,DESCRIPTION,ATPTESTSTATUS,ATPTESTTASKID,ATPTESTREPORTURL,PACKAGEDOWNLOADURL,ICONDOWNLOADURL,AFFINITY,SHORTDESC,TYPE,DEMOVIDEODOWNLOADURL
)values ('j2417aef-c916-4c92-a518-d29c4804acdf','acept',true,'appname','laintong','1.1','NOTICE','EdgeGallery AppStore'
,'EdgeGallery AppStore','2021-08-31 16:54:49','tweest','success','apt-taskid-0001',
'http://127.0.0.1:8073/atpreport?taskId=apt-taskid-0001'
,'http://127.0.0.1:8099/mec/appstore/v1/packages/packageid-0002/action/download-package'
,'http://127.0.0.1:8099/mec/appstore/v1/packages/b415e520e00a48ed9721fefa99187f02/action/download-icon','test','test','game','');


merge into tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('7dd477d8-bcc0-4e2a-a48d-2b587a30026a', 'Face Recognition service plus.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/7dd477d8-bcc0-4e2a-a48d-2b587a30026a');
merge into tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('d0f8fa57-2f4c-4182-be33-0a508964d04a', 'Face Recognition service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/d0f8fa57-2f4c-4182-be33-0a508964d04a');
merge into tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('540e0817-f6ea-42e5-8c5b-cb2daf9925a3', 'Service Discovery.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/540e0817-f6ea-42e5-8c5b-cb2daf9925a3');
merge into tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('7c544903-aa4f-40e0-bd8c-cf6e17c37c12', 'Bandwidth service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/7c544903-aa4f-40e0-bd8c-cf6e17c37c12');
merge into tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('688f259e-48eb-407d-8604-7feb19cf1f44', 'Location service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/688f259e-48eb-407d-8604-7feb19cf1f44');
merge into tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('9f1f13a0-8554-4dfa-90a7-d2765238fca7', 'Traffic service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/9f1f13a0-8554-4dfa-90a7-d2765238fca7');


merge into tbl_service_host (host_id, name, address, architecture, status, lcm_ip, mec_host, os, port_range_min, port_range_max, port, protocol, delete) KEY(host_id)
VALUES ('3c55ac26-60e9-42c0-958b-1bf7ea4da777', 'Node2', 'XIAN', 'X86', 'NORMAL', 'localhost', 'localhost', 'K8S', 30000, 32767, 30201, 'http', null);
merge into tbl_service_host (host_id, name, address, architecture, status, lcm_ip, os, port_range_min, port_range_max, port, protocol, delete) KEY(host_id) VALUES ('3c55ac26-60e9-42c0-958b-1bf7ea4da60a', 'Node1', 'XIAN', 'X86', 'NORMAL', '127.0.0.1', 'Ubuntu', 30000, 32767, 30201, 'http', null);

merge into tbl_service_host(host_id,name,address,architecture,status,lcm_ip,port,os,port_range_min,port_range_max, user_id) KEY(host_id) VALUES ('c8aac2b2-4162-40fe-9d99-0630e3245cf7', 'host-1', 'xian', 'ARM','NORMAL','10.1.12.1',8999,'liunx',30000,300001,'e111f3e7-90d8-4a39-9874-ea6ea6752ef6');
merge into tbl_service_host(host_id,name,address,architecture,status,lcm_ip,port,os,port_range_min,port_range_max, user_id) KEY(host_id) VALUES ('c8aac2b2-4162-40fe-9d99-0630e3245cdd', 'host-1', 'xian', 'ARM','NORMAL','10.1.12.1',8999,'liunx',30000,300001,'e111f3e7-90d8-4a39-9874-ea6ea6752eaa');

merge into PACKAGE_UPLOAD_PROGRESS_TABLE(ID,PACKAGE_ID,MEAO_ID,STATUS,PROGRESS,CREATE_TIME) KEY(ID) VALUES ('c8aac2b2-4162-40fe-9d99-0630e3245fff', 'package-1', 'meao-1', 'NORMAL','start','2020-01-01 00:00:00.000000');
merge into PACKAGE_UPLOAD_PROGRESS_TABLE(ID,PACKAGE_ID,MEAO_ID,STATUS,PROGRESS,CREATE_TIME) KEY(ID) VALUES ('c8aac2b2-4162-40fe-9d99-0630e3245aaa', 'package-2', 'meao-2', 'NORMAL','start','2020-01-02 00:00:00.000000');
merge into PACKAGE_UPLOAD_PROGRESS_TABLE(ID,PACKAGE_ID,MEAO_ID,STATUS,PROGRESS,CREATE_TIME) KEY(ID) VALUES ('c8aac2b2-4162-40fe-9d99-0630e3245bbb', 'package-3', 'meao-3', 'NORMAL','start','2020-01-03 00:00:00.000000');

insert into app_order(ORDERID, ORDERNUM, USERID, USERNAME, APPID, APPPACKAGEID, ORDERTIME, OPERATETIME, STATUS, MECM_HOSTIP, MECM_APPID, MECM_APPPACKAGEID)
  values('7c555c26-2343-6456-958b-12f7ea4da971', 'ES0000000001', 'd0f8fa57-2f4c-4182-be33-0a508964d04a', 'test-username-fororder', 'appid-test-0001', 'packageid-0003', now(), now(), 'ACTIVATED', '127.0.0.1', 'mecm-appid-0001', 'mecm-packageid-0001');
insert into app_order(ORDERID, ORDERNUM, USERID, USERNAME, APPID, APPPACKAGEID, ORDERTIME, OPERATETIME, STATUS, MECM_HOSTIP)
  values('7c555c26-2343-6456-958b-12f7ea4da972', 'ES0000000002', 'd0f8fa57-2f4c-4182-be33-0a508964d04a', 'test-username-fororder', 'appid-test-0001', 'packageid-0003', now(), now(), 'DEACTIVATED', '127.0.0.1');
insert into app_order(ORDERID, ORDERNUM, USERID, USERNAME, APPID, APPPACKAGEID, ORDERTIME, OPERATETIME, STATUS, MECM_HOSTIP, MECM_APPID, MECM_APPPACKAGEID)
  values('7c555c26-2343-6456-958b-12f7ea4da973', 'ES0000000003', 'd0f8fa57-2f4c-4182-be33-0a508964d04a', 'test-username-fororder', 'appid-test-0001', 'packageid-0003', '2021-09-29 17:32:31.201', '2021-09-29 17:32:31.201', 'ACTIVATED', '127.0.0.1', 'mecm-appid-0002', 'mecm-packageid-0002');
insert into app_order(ORDERID, ORDERNUM, USERID, USERNAME, APPID, APPPACKAGEID, ORDERTIME, OPERATETIME, STATUS, MECM_HOSTIP)
  values('7c555c26-2343-6456-958b-12f7ea4da974', 'ES0000000004', 'd0f8fa57-2f4c-4182-be33-0a508964d04a', 'test-username-fororder', 'appid-test-0001', 'packageid-0004', now(), now(), 'DEACTIVATED', '127.0.0.1');
insert into app_order(ORDERID, ORDERNUM, USERID, USERNAME, APPID, APPPACKAGEID, ORDERTIME, OPERATETIME, STATUS, MECM_HOSTIP)
  values('7c555c26-2343-6456-958b-12f7ea4da975', 'ES0000000005', 'd0f8fa57-2f4c-4182-be33-0a508964d04a', 'test-username-fororder', 'appid-test-0001', 'packageid-0005', now(), now(), 'DEACTIVATED', '127.0.0.1');