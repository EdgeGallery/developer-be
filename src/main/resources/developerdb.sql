    -- ----------------------------
    -- plugin and app-test table start -----------------
    -- ----------------------------
    -- Table structure for tbl_appfunction
    -- ----------------------------
    CREATE TABLE IF NOT EXISTS  "tbl_appfunction"(
      "functionid" varchar(255) NOT NULL DEFAULT NULL,
      "funcname" varchar(255) DEFAULT NULL,
      "funcdesc" varchar(255) DEFAULT NULL,
      "addtime" varchar(244) DEFAULT NULL,
      CONSTRAINT "tbl_appfunction_pkey" PRIMARY KEY ("functionid")
    )
    ;

    -- ----------------------------
    -- Records of tbl_appfunction
    -- ----------------------------
    INSERT INTO  "tbl_appfunction" VALUES ('53fc40e9a1f048e4b4310e8ac30856b3', 'CPU', '处理速度', '2019-10-23 03:27:36')
    ON CONFLICT(functionid) do nothing;
    INSERT INTO  "tbl_appfunction" VALUES ('343d42a3b59c46f9afda063b8be4cc8f', 'GPU', '处理图片', '2019-10-23 03:27:54')
    ON CONFLICT(functionid) do nothing;
    INSERT INTO  "tbl_appfunction" VALUES ('526f86afd6b841ae9df56e30d37f0574', 'Memory Disk', '存储优先', '2019-11-02 10:48:33')
    ON CONFLICT(functionid) do nothing;
    INSERT INTO  "tbl_appfunction" VALUES ('8167fc046c2d4e42997c612fdfbd7c8f', 'AI', '存储', '2019-10-23 05:37:46')
    ON CONFLICT(functionid) do nothing;


    -- ----------------------------
    -- Table structure for tbl_downloadrecord
    -- ----------------------------
    CREATE TABLE IF NOT EXISTS "tbl_downloadrecord"(
      "recordid" varchar(255) NOT NULL,
      "pluginid" varchar(255) DEFAULT NULL,
      "downloaduserid" varchar(255) DEFAULT NULL,
      "downloadusername" varchar(255) DEFAULT NULL,
      "score" float4 DEFAULT NULL,
      "scoretype" int4 DEFAULT NULL,
      "downloadtime" timestamptz(0) DEFAULT NULL,
       CONSTRAINT "tbl_downloadrecord_pkey" PRIMARY KEY ("recordid")
    )
    ;

    -- ----------------------------
    -- Table structure for tbl_plugin
    -- ----------------------------
    CREATE TABLE IF NOT EXISTS "tbl_plugin" (
      "pluginid" varchar(255) NOT NULL DEFAULT NULL,
      "pluginname" varchar(255) DEFAULT NULL,
      "introduction" varchar(500) DEFAULT NULL,
      "satisfaction" float4 DEFAULT NULL,
      "codelanguage" varchar(255) DEFAULT NULL,
      "plugintype" int4  DEFAULT NULL,
      "version" varchar(255)  DEFAULT NULL,
      "downloadcount" int4 DEFAULT NULL,
      "logofile" varchar(500) DEFAULT NULL,
      "pluginfile" varchar(500) DEFAULT NULL,
      "userid" varchar(255) DEFAULT NULL,
      "uploadtime" timestamptz(6) DEFAULT NULL,
      "username" varchar(255) DEFAULT NULL,
      "pluginsize" int4 DEFAULT NULL,
      "apifile" varchar(500) DEFAULT NULL,
      "scorecount" int4 DEFAULT NULL,
      "pluginfilehashcode" varchar(50) DEFAULT NULL,
      CONSTRAINT "tbl_plugin_pkey" PRIMARY KEY ("pluginid")
    )
    ;

    -- plugin and app-test table end -----------------
    -- workspace table start -----------------
    CREATE TABLE IF NOT EXISTS "tbl_app_project" (
      "id" varchar(50) DEFAULT NULL,
      "name" varchar(100) DEFAULT NULL,
      "provider" varchar(100) DEFAULT NULL,
      "platform" varchar(100) DEFAULT NULL,
      "industries" varchar(100) DEFAULT NULL,
      "type" varchar(50) DEFAULT NULL,
      "description" text DEFAULT NULL,
      "status" varchar(20) DEFAULT NULL,
      "user_id" varchar(50) DEFAULT NULL,
      "create_date" varchar(50) DEFAULT NULL,
      "last_test_id" varchar(50) DEFAULT NULL,
      "version" varchar(50) DEFAULT NULL,
      "capabilities" text DEFAULT NULL,
      "project_type" varchar(10) DEFAULT NULL,
      "icon_file_id" varchar(50) DEFAULT NULL,
      "open_capability_id" varchar(50) DEFAULT NULL,
      "deploy_platform" varchar(100) DEFAULT NULL,
      CONSTRAINT "tbl_app_project_pkey" PRIMARY KEY ("id")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_openmep_capability" (
      "group_id" varchar(50) NOT NULL,
      "one_level_name" varchar(255)  DEFAULT NULL,
      "one_level_name_en" varchar(255) DEFAULT NULL,
      "two_level_name" varchar(255) DEFAULT NULL,
      "two_level_name_en" varchar(255) DEFAULT NULL,
      "type" varchar(20) DEFAULT NULL,
      "description" text DEFAULT NULL,
      "description_en" text DEFAULT NULL,
      "icon_file_id" varchar(50) DEFAULT NULL,
      "author" varchar(50) DEFAULT NULL,
      "select_count" int4 NOT NULL DEFAULT 0,
      "upload_time" timestamptz(6) DEFAULT NULL,
      CONSTRAINT "tbl_openmep_capability_pkey" PRIMARY KEY ("group_id")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_openmep_capability_detail" (
      "detail_id" varchar(50) NOT NULL,
      "service" varchar(100) DEFAULT NULL,
      "service_en" varchar(100) DEFAULT NULL,
      "version" varchar(100) DEFAULT NULL,
      "description" text DEFAULT NULL,
      "description_en" text DEFAULT NULL,
      "provider" varchar(100) DEFAULT NULL,
      "group_id" varchar(50) DEFAULT NULL,
      "api_file_id" varchar(255) DEFAULT NULL,
      "guide_file_id" varchar(255) DEFAULT NULL,
      "guide_file_id_en" varchar(255) DEFAULT NULL,
      "upload_time" varchar(50) NOT NULL,
      "host" varchar(50) DEFAULT NULL,
      "port" int4 DEFAULT NULL,
      "protocol" varchar(20) DEFAULT NULL,
      "app_id" varchar(255) DEFAULT NULL,
      "package_id" varchar(255) DEFAULT NULL,
      "user_id" varchar(255) DEFAULT NULL,
      CONSTRAINT "tbl_openmep_capability_detail_pkey" PRIMARY KEY ("detail_id")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_project_image" (
      "id"  varchar(255) NOT NULL,
      "pod_name" varchar(255) DEFAULT NULL,
      "pod_containers" text  DEFAULT NULL,
      "project_id" varchar(255) DEFAULT NULL,
      "svc_type" varchar(255)  DEFAULT NULL,
      "svc_port" varchar(255)  DEFAULT NULL,
      "svc_node_port" varchar(255)  DEFAULT NULL,
      CONSTRAINT "tbl_project_image_pkey" PRIMARY KEY ("id")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_project_test_config" (
      "test_id" varchar(50) NOT NULL,
      "project_id" varchar(50) DEFAULT NULL,
      "agent_config" text DEFAULT NULL,
      "image_file_id" varchar(255) DEFAULT NULL,
      "app_api_file_id" varchar(50) DEFAULT NULL,
      "deploy_file_id" varchar(50) DEFAULT NULL,
      "private_host" bool DEFAULT FALSE,
      "platform" varchar(100) DEFAULT NULL,
      "access_url" text DEFAULT NULL,
      "error_log" text DEFAULT NULL,
      "deploy_date" timestamptz(6) DEFAULT NULL,
      "hosts" text DEFAULT NULL,
      "app_instance_id" varchar(50) DEFAULT NULL,
      "work_load_id" varchar(255) DEFAULT NULL,
      "pods" text DEFAULT NULL,
      "deploy_status" varchar(255) DEFAULT NULL,
      "stage_status"  varchar(255) DEFAULT NULL,
      "lcm_token"  varchar(1000) DEFAULT NULL,
      "package_id" varchar(255) DEFAULT NULL,
      CONSTRAINT "tbl_project_test_config_pkey" PRIMARY KEY ("test_id")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_uploaded_file" (
      "file_id" varchar(50) NOT NULL,
      "file_name" varchar(255) DEFAULT NULL,
      "is_temp" bool DEFAULT NULL,
      "user_id" varchar(50) DEFAULT NULL,
      "upload_date" timestamptz(6) DEFAULT NULL,
      "file_path" varchar(255) DEFAULT NULL,
      CONSTRAINT "tbl_uploaded_file_pkey" PRIMARY KEY ("file_id")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_helm_template_yaml" (
      "file_id" varchar(50) NOT NULL,
      "file_name" varchar(255) DEFAULT NULL,
      "user_id" varchar(50) DEFAULT NULL,
      "project_id" varchar(50) DEFAULT NULL,
      "content" text DEFAULT NULL,
      "upload_time_stamp" bigint DEFAULT NULL,
      "config_type" varchar(50) DEFAULT NULL,
      CONSTRAINT "tbl_helm_template_yaml_pkey" PRIMARY KEY ("file_id")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_service_host" (
      "host_id" varchar(50) NOT NULL,
      "user_id" varchar(50) DEFAULT NULL,
      "name" varchar(100) DEFAULT NULL,
      "address" varchar(255) DEFAULT NULL,
      "architecture" varchar(100) DEFAULT NULL,
      "status" varchar(20) DEFAULT NULL,
      "protocol" varchar(20) DEFAULT NULL,
      "lcm_ip" varchar(20) DEFAULT NULL,
      "mec_host" varchar(20) DEFAULT NULL,
      "os" varchar(255) DEFAULT NULL,
      "port_range_min" int DEFAULT '-1'::integer,
      "port_range_max" int DEFAULT '-1'::integer,
      "port" int4 DEFAULT '-1'::integer,
      "user_name" varchar(50) DEFAULT NULL,
      "password" varchar(50) DEFAULT NULL,
      "vnc_port" int4 DEFAULT 22,
      "parameter" text DEFAULT NULL,
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

    CREATE TABLE IF NOT EXISTS "tbl_api_emulator" (
      "id" varchar(50) NOT NULL,
      "user_id" varchar(50) NOT NULL,
      "host_id" varchar(50) NOT NULL,
      "port" int4 NOT NULL,
      "workload_id" varchar(50) NOT NULL,
      "create_time" varchar(50) NOT NULL
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_release_config" (
       "release_id" varchar(255) NOT NULL,
       "project_id" varchar(255) NOT NULL,
       "guide_file_id" varchar(255) DEFAULT NULL,
       "appinstance_id" varchar(255) DEFAULT NULL,
       "capabilities_detail" text  DEFAULT NULL,
       "atp_test" text DEFAULT NULL,
       "test_status" varchar(255)  DEFAULT NULL,
       "create_time" timestamptz(0) NOT NULL DEFAULT NULL,
       CONSTRAINT "tbl_release_config_pkey" PRIMARY KEY ("release_id")
     )
     ;

     CREATE TABLE IF NOT EXISTS "tbl_vm_regulation" (
      "regulation_id" int4 NOT NULL,
      "architecture" varchar(50) DEFAULT NULL,
      "name_zh" varchar(50) DEFAULT NULL,
      "name_en" varchar(50) DEFAULT NULL,
      "scene_zh" varchar(255) DEFAULT NULL,
      "scene_en" varchar(255) DEFAULT NULL,
      "memory" int4  DEFAULT NULL,
      "cpu" int4  DEFAULT NULL,
      "system_disk" int4  DEFAULT NULL,
      "data_disk" int4  DEFAULT NULL,
      "gpu" varchar(50)  DEFAULT NULL,
      "other_ability" varchar(255)  DEFAULT NULL,
      CONSTRAINT "tbl_vm_regulation_pkey" PRIMARY KEY ("regulation_id")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_vm_network" (
      "network_type" varchar(50) DEFAULT NULL,
      "description_zh" varchar(255) DEFAULT NULL,
      "description_en" varchar(255) DEFAULT NULL,
      "network_name" varchar(50) DEFAULT NULL,
      CONSTRAINT "tbl_vm_network_pkey" PRIMARY KEY ("network_type")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_vm_system" (
        "system_id" SERIAL,
        "system_name" varchar(128) DEFAULT NULL,
        "type" varchar(50) DEFAULT NULL,
        "operate_system" varchar(50) DEFAULT NULL,
        "version" varchar(50) DEFAULT NULL,
        "system_bit" varchar(50) DEFAULT NULL,
        "system_disk" int4  DEFAULT NULL,
        "user_id" varchar(50) DEFAULT NULL,
        "user_name" varchar(50) DEFAULT NULL,
        "create_time" timestamptz(6)  DEFAULT NULL,
        "modify_time" timestamptz(6)  DEFAULT NULL,
        "system_format" varchar(50) DEFAULT NULL,
        "upload_time" timestamptz(6)  DEFAULT NULL,
        "system_path" varchar(128) DEFAULT NULL,
        "file_name" varchar(128) DEFAULT NULL,
        "file_md5" varchar(128) DEFAULT NULL,
        "status" varchar(50) DEFAULT NULL,
        CONSTRAINT "tbl_vm_system_uniqueName" UNIQUE ("system_name","user_id"),
        CONSTRAINT "tbl_vm_system_pkey" PRIMARY KEY ("system_id")
        )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_vm_user_data" (
      "operate_system" varchar(50) DEFAULT NULL,
      "flavor_extra_specs" text DEFAULT NULL,
      "is_temp" bool DEFAULT NULL,
      "contents" text DEFAULT NULL,
      "params" text DEFAULT NULL,
      CONSTRAINT "tbl_vm_user_data_pkey" PRIMARY KEY ("operate_system")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_project_vm_package_config" (
      "id" varchar(50) DEFAULT NULL,
      "project_id" varchar(50) DEFAULT NULL,
      "vm_regulation_desc" text DEFAULT NULL,
      "vm_system_desc" text DEFAULT NULL,
      "vm_network_desc" text DEFAULT NULL,
      "vm_user_data" text DEFAULT NULL,
      "vm_name" varchar(500) DEFAULT NULL,
      "ak" text DEFAULT NULL,
      "sk" text DEFAULT NULL,
      "app_instance_id" varchar(50) DEFAULT NULL,
      "create_time" timestamptz(6) DEFAULT NULL,
      CONSTRAINT "tbl_project_vm_package_config_pkey" PRIMARY KEY ("id")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_project_vm_create_config" (
      "vm_id"  varchar(255) NOT NULL DEFAULT NULL,
      "project_id" varchar(50) DEFAULT NULL,
      "vm_name" varchar(500) DEFAULT NULL,
      "host" text  DEFAULT NULL,
      "status" varchar(50)  DEFAULT NULL,
      "stage_status" varchar(500)  DEFAULT NULL,
      "lcm_token" varchar(1024)  DEFAULT NULL,
      "vm_info" text  DEFAULT NULL,
      "app_instance_id" varchar(50)  DEFAULT NULL,
      "package_id" varchar(100)  DEFAULT NULL,
      "create_time"  timestamptz(6)  DEFAULT NULL,
      "log" text  DEFAULT NULL,
      CONSTRAINT "tbl_vm_create_config_pkey" PRIMARY KEY ("vm_id")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_project_vm_image_config" (
      "vm_id"  varchar(255) NOT NULL DEFAULT NULL,
      "image_id" varchar(500) DEFAULT NULL,
      "project_id" varchar(50) DEFAULT NULL,
      "vm_name" varchar(500) DEFAULT NULL,
      "image_name" varchar(500) DEFAULT NULL,
      "app_instance_id" varchar(50)  DEFAULT NULL,
      "host_ip" varchar(50)  DEFAULT NULL,
      "sum_chunk_num" varchar(50)  DEFAULT NULL,
      "chunk_size" varchar(50)  DEFAULT NULL,
      "checksum" varchar(500)  DEFAULT NULL,
      "stage_status" varchar(500)  DEFAULT NULL,
      "status" varchar(512)  DEFAULT NULL,
      "lcm_token" varchar(1024)  DEFAULT NULL,
      "create_time"  timestamptz(6)  DEFAULT NULL,
      "log" text  DEFAULT NULL,
      CONSTRAINT "tbl_vm_image_config_pkey" PRIMARY KEY ("vm_id")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_container_image" (
    "image_id" varchar(255) NOT NULL,
    "image_name" varchar(255) NOT NULL,
    "image_version" varchar(255) NOT NULL,
    "user_id" varchar(255) NOT NULL,
    "user_name" varchar(255) NOT NULL,
    "upload_time" timestamptz(0) DEFAULT NULL,
    "create_time" timestamptz(0) DEFAULT NULL,
    "image_status" varchar(255) DEFAULT NULL,
    "image_type" varchar(255) DEFAULT NULL,
    "image_path" text DEFAULT NULL,
    "file_md5" varchar(255) DEFAULT NULL,
    "file_name" varchar(255) DEFAULT NULL,
    CONSTRAINT  "tbl_container_image_uniqueName" UNIQUE ("image_name","image_version"),
    CONSTRAINT "tbl_container_image_pkey" PRIMARY KEY ("image_id")
    );