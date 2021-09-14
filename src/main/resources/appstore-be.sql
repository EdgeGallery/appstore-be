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
        APPINSTANCEID            VARCHAR(100)       NULL,
        INSTANCETENENTID         VARCHAR(100)       NULL,
        INSTANCEPACKAGEID        VARCHAR(255)       NULL,
        EXPERIENCEABLE           boolean            DEFAULT false,
        STARTEXPTIME             VARCHAR(100)       NULL,
        EXPERIENCEABLEIP         VARCHAR(100)       NULL,
        MECHOST                  VARCHAR(100)       NULL,
        CONSTRAINT catalog_package_table_pkey PRIMARY KEY (PACKAGEID)
    );

    create TABLE if not exists app_table (
        APPID                    VARCHAR(200)       NOT NULL,
        APPNAME                  VARCHAR(100)       NULL,
        APPLICATIONTYPE          VARCHAR(300)       NULL,
        DEPLOYMODE               VARCHAR(100)       NULL,
        SHORTDESC                TEXT               NULL,
        PROVIDER                 VARCHAR(300)       NULL,
        APPINTRODUCTION          TEXT               NULL,
        DOWNLOADCOUNT            INT                NULL,
        AFFINITY                 VARCHAR(100)       NULL,
        INDUSTRY                 VARCHAR(100)       NULL,
        CONTACT                  VARCHAR(100)       NULL,
        USERID                   VARCHAR(100)       NULL,
        USERNAME                 VARCHAR(100)       NULL,
        CREATETIME               TIMESTAMP          NULL,
        MODIFYTIME               TIMESTAMP          NULL,
        SCORE                    NUMERIC(2,1)       NULL,
        STATUS                   VARCHAR(50)        NULL,
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

    CREATE TABLE IF NOT EXISTS PUSHABLE_PACKAGE_TABLE (
        PACKAGEID  VARCHAR(64) NOT NULL,
        ATPTESTREPORTURL VARCHAR(100) NOT NULL,
        LATESTPUSHTIME TIMESTAMP NOT NULL,
        PUSHTIMES INTEGER NOT NULL DEFAULT 0,
        SOURCEPLATFORM VARCHAR(100) NOT NULL,
        CONSTRAINT PUSHABLE_PACKAGE_TABLE_PKEY PRIMARY KEY (PACKAGEID)
    );

    CREATE TABLE IF NOT EXISTS TBL_SERVICE_HOST (
        HOST_ID VARCHAR(50) NOT NULL,
        USER_ID VARCHAR(50) DEFAULT NULL,
        NAME VARCHAR(100) DEFAULT NULL,
        ADDRESS VARCHAR(255) DEFAULT NULL,
        ARCHITECTURE VARCHAR(100) DEFAULT NULL,
        STATUS VARCHAR(20) DEFAULT NULL,
        PROTOCOL VARCHAR(20) DEFAULT NULL,
        LCM_IP VARCHAR(20) DEFAULT NULL,
        MEC_HOST VARCHAR(20) DEFAULT NULL,
        OS VARCHAR(255) DEFAULT NULL,
        PORT_RANGE_MIN INTEGER DEFAULT 0,
        PORT_RANGE_MAX INTEGER DEFAULT 0,
        PORT INTEGER DEFAULT 0,
        VNC_PORT INTEGER DEFAULT NULL,
        PARAMETER text DEFAULT NULL,
        DELETE BOOLEAN DEFAULT NULL
    );

    CREATE TABLE IF NOT EXISTS TBL_UPLOADED_FILE (
        FILE_ID VARCHAR(50) NOT NULL DEFAULT NULL,
        FILE_NAME VARCHAR(255) DEFAULT NULL,
        IS_TEMP BOOL DEFAULT NULL,
        USER_ID VARCHAR(50) DEFAULT NULL,
        UPLOAD_DATE TIMESTAMPTZ(6) DEFAULT NULL,
        FILE_PATH VARCHAR(255) DEFAULT NULL,
        CONSTRAINT TBL_UPLOADED_FILE_PKEY PRIMARY KEY (FILE_ID)
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
        CONSTRAINT app_order_pkey PRIMARY KEY (ORDERID),
        CONSTRAINT app_order_uniqueOrderNum UNIQUE (ORDERNUM)
    );

    create TABLE if not exists app_bill (
        BILLID                  VARCHAR(200)       NOT NULL,
        ORDERID                 VARCHAR(200)       NOT NULL,
        CREATETIME              TIMESTAMP          NOT NULL,
        OPERATORFEE             NUMERIC(10,2)      NULL,
        SUPPLIERFEE             NUMERIC(10,2)      NULL,
        CONSTRAINT app_bill_pkey PRIMARY KEY (BILLID),
        CONSTRAINT app_bill_fk_orderid foreign key(ORDERID) references app_order(ORDERID)
    );

    create TABLE if not exists app_split_config (
        APPID                 VARCHAR(200)       NOT NULL,
        SPLITRATIO            NUMERIC(3,2)       NULL,
        CONSTRAINT app_split_config_pkey PRIMARY KEY (APPID)
    );

    create INDEX app_order_idx_userid on app_order (USERID);

    alter table catalog_package_table add column IF NOT EXISTS DEMOVIDEOADDRESS VARCHAR(200) NULL;

    alter table message_table add column IF NOT EXISTS DEMOVIDEODOWNLOADURL VARCHAR(255) NULL;

    alter table catalog_package_table add column IF NOT EXISTS DEPLOYMODE VARCHAR(100) NULL;

    alter table app_table add column IF NOT EXISTS DEPLOYMODE VARCHAR(100) NULL;

    update catalog_package_table set DEPLOYMODE = 'container' where DEPLOYMODE is NULL;

    update app_table set DEPLOYMODE = 'container' where DEPLOYMODE is NULL;

    alter table catalog_package_table add column if NOT EXISTS SHOWTYPE varchar(100) NOT NULL DEFAULT 'public';

    alter table app_table add column if NOT EXISTS SHOWTYPE varchar(100) NOT NULL DEFAULT 'public';

    alter table app_table add column if NOT EXISTS ISHOTAPP boolean DEFAULT false;

    alter table catalog_package_table add column IF NOT EXISTS APPINSTANCEID VARCHAR(100) DEFAULT NULL;

    alter table catalog_package_table add column IF NOT EXISTS INSTANCETENENTID VARCHAR(100) DEFAULT NULL;

    alter table catalog_package_table add column IF NOT EXISTS INSTANCEPACKAGEID VARCHAR(255) DEFAULT NULL;

    alter table catalog_package_table add column IF NOT EXISTS EXPERIENCEABLE boolean DEFAULT false;

    alter table app_table add column IF NOT EXISTS EXPERIENCEABLE boolean DEFAULT false;

    alter table catalog_package_table add column IF NOT EXISTS STARTEXPTIME VARCHAR(100) DEFAULT NULL;

    alter table tbl_service_host alter parameter type text USING parameter::VARCHAR(500);

    alter table catalog_package_table add column IF NOT EXISTS EXPERIENCEABLEIP varchar(100) NULL;

    alter table catalog_package_table add column IF NOT EXISTS MECHOST varchar(100) NULL;