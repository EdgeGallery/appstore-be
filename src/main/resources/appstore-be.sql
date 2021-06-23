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

    CREATE TABLE IF NOT EXISTS "tbl_service_host" (
      "host_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL::character varying,
      "user_id" varchar(50) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
      "name" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
      "address" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
      "architecture" varchar(100) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
      "status" varchar(20) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
      "protocol" varchar(20) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
      "lcm_ip" varchar(20) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
      "mec_host" varchar(20) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
      "os" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
      "port_range_min" int DEFAULT '-1'::integer,
      "port_range_max" int DEFAULT '-1'::integer,
      "port" int4 DEFAULT '-1'::integer,
      "user_name" varchar(50) DEFAULT NULL,
      "password" varchar(50) DEFAULT NULL,
      "vnc_port" int4 DEFAULT NULL,
      "parameter" varchar(500) DEFAULT 22,
      "delete" bool DEFAULT NULL
    )

    ;
      CREATE TABLE IF NOT EXISTS "tbl_host_log" (
      "log_id" varchar(50) NOT NULL,
      "host_ip" varchar(50) NOT NULL,
      "user_name" varchar(50) DEFAULT NULL,
      "user_id" varchar(50) DEFAULT NULL,
      "project_id" varchar(50) DEFAULT NULL,
      "project_name" varchar(50) DEFAULT NULL,
      "app_instances_id" varchar(50) DEFAULT NULL,
      "deploy_time" varchar(50) DEFAULT NULL,
      "status" varchar(50) DEFAULT NULL,
      "operation" varchar(50) DEFAULT NULL,
      "host_id" varchar(50) DEFAULT NULL
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_uploaded_file" (
      "file_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL DEFAULT NULL,
      "file_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
      "is_temp" bool DEFAULT NULL,
      "user_id" varchar(50) COLLATE "pg_catalog"."default" DEFAULT NULL,
      "upload_date" timestamptz(6) DEFAULT NULL,
      "file_path" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL,
      CONSTRAINT "tbl_uploaded_file_pkey" PRIMARY KEY ("file_id")
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