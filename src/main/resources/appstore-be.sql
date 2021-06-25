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
      parameter VARCHAR(500) DEFAULT 22,
      delete boolean DEFAULT NULL
    )

    ;


    CREATE TABLE IF NOT EXISTS tbl_uploaded_file (
      file_id VARCHAR(50) NOT NULL DEFAULT NULL,
      file_name VARCHAR(255) DEFAULT NULL,
      is_temp bool DEFAULT NULL,
      user_id VARCHAR(50) DEFAULT NULL,
      upload_date timestamptz(6) DEFAULT NULL,
      file_path VARCHAR(255) DEFAULT NULL,
      CONSTRAINT tbl_uploaded_file_pkey PRIMARY KEY (file_id)
    )
    ;


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