    -- ----------------------------
    -- plugin and app-test table start -----------------
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
    
    CREATE TABLE IF NOT EXISTS "tbl_app_project_capability" (
      "project_id" varchar(50) NOT NULL,
      "capability_id" varchar(50) NOT NULL,
      CONSTRAINT "tbl_app_project_capability_pkey" PRIMARY KEY ("project_id","capability_id")
    )
    ;
    
    CREATE TABLE IF NOT EXISTS "tbl_capability_group" (
      "id" varchar(50) NOT NULL,
      "name" varchar(255)  DEFAULT NULL,
      "name_en" varchar(255) DEFAULT NULL,
      "type" varchar(20) DEFAULT NULL,
      "description" text DEFAULT NULL,
      "description_en" text DEFAULT NULL,
      "icon_file_id" varchar(50) DEFAULT NULL,
      "author" varchar(50) DEFAULT NULL,
      "create_time" bigint NOT NULL DEFAULT 0,
      "update_time" bigint NOT NULL DEFAULT 0,
      CONSTRAINT "tbl_capability_group_pkey" PRIMARY KEY ("id")
    )
    ;    

    CREATE TABLE IF NOT EXISTS "tbl_capability" (
      "id" varchar(50) NOT NULL,
      "name" varchar(100) DEFAULT NULL,
      "name_en" varchar(100) DEFAULT NULL,
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
      "select_count" integer NOT NULL DEFAULT 0,
      "icon_file_id" varchar(50) DEFAULT NULL,
      "author" varchar(50) DEFAULT NULL,
      "experience_url" text DEFAULT NULL,
      CONSTRAINT "tbl_capability_pkey" PRIMARY KEY ("id")
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_project_image" (
      "id"  varchar(255) NOT NULL,
      "image_info" text  DEFAULT NULL,
      "project_id" varchar(255) DEFAULT NULL,
      "helm_chart_file_id" varchar(255) DEFAULT NULL,
      CONSTRAINT "tbl_project_image_pkey" PRIMARY KEY ("id")
    )
    ;

     CREATE TABLE IF NOT EXISTS "tbl_container_app_image_info" (
       "id"  varchar(255) NOT NULL,
       "image_info" text  DEFAULT NULL,
       "application_id" varchar(255) DEFAULT NULL,
       "helm_chart_file_id" varchar(255) DEFAULT NULL,
       CONSTRAINT "tbl_container_app_image_info_pkey" PRIMARY KEY ("id")
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
      "delete" bool DEFAULT NULL,
      "resource" text DEFAULT NULL
    )
    ;

    CREATE TABLE IF NOT EXISTS "tbl_mep_host" (
      "host_id" varchar(50) NOT NULL,
      "name" varchar(100) DEFAULT NULL,
      "lcm_ip" varchar(20) DEFAULT NULL,
      "lcm_protocol" varchar(20) DEFAULT NULL,
      "lcm_port" int4 DEFAULT '-1'::integer,
      "architecture" varchar(100) DEFAULT NULL,
      "status" varchar(20) DEFAULT NULL,
      "mec_host_ip" varchar(20) DEFAULT NULL,
      "vim_type" varchar(255) DEFAULT NULL,
      "mec_host_user_name" varchar(50) DEFAULT NULL,
      "mec_host_password" varchar(50) DEFAULT NULL,
      "mec_host_port" int4 DEFAULT 22,
      "user_id" varchar(50) DEFAULT NULL,
      "config_file_id" varchar(50) DEFAULT NULL,
      "net_work_parameter" text DEFAULT NULL,
      "resource" text DEFAULT NULL,
      "address" varchar(255) DEFAULT NULL
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
      "system_size" bigint DEFAULT NULL,
      "system_slim" varchar(50) DEFAULT NULL,
      "upload_time" timestamptz(6)  DEFAULT NULL,
      "system_path" varchar(128) DEFAULT NULL,
      "file_name" varchar(128) DEFAULT NULL,
      "file_md5" varchar(128) DEFAULT NULL,
      "status" varchar(50) DEFAULT NULL,
      "file_identifier" varchar(128) DEFAULT NULL,
      "error_type" varchar(32) DEFAULT NULL,
      CONSTRAINT "tbl_vm_system_uniqueName" UNIQUE ("system_name","user_id"),
      CONSTRAINT "tbl_vm_system_pkey" PRIMARY KEY ("system_id")
      );

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
      CONSTRAINT  "tbl_project_vm_package__uniqueProjectId" UNIQUE ("project_id"),
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
    "file_name" varchar(255) DEFAULT NULL,
    CONSTRAINT  "tbl_container_image_uniqueName" UNIQUE ("image_name","image_version","user_name"),
    CONSTRAINT "tbl_container_image_pkey" PRIMARY KEY ("image_id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_app_traffic_rule" (
    "app_id" varchar(255) NOT NULL,
    "traffic_rule_id" varchar(255) NOT NULL,
    "action" varchar(255) DEFAULT NULL,
    "priority" int4 DEFAULT NULL,
    "filter_type" varchar(255) DEFAULT NULL,
    "traffic_filter" text DEFAULT NULL,
    "dst_interface" text DEFAULT NULL,
    CONSTRAINT  "tbl_app_traffic_rule_unique_id_traffic_rule" UNIQUE ("app_id","traffic_rule_id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_app_dns_rule" (
    "app_id" varchar(255) NOT NULL,
    "dns_rule_id" varchar(255) NOT NULL,
    "domain_name" varchar(255) DEFAULT NULL,
    "ip_address_type" varchar(255) DEFAULT NULL,
    "ip_address" varchar(255) DEFAULT NULL,
    "ttl" varchar(255) DEFAULT NULL,
    CONSTRAINT  "tbl_app_dns_rule_unique_id_dns_rule" UNIQUE ("app_id","dns_rule_id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_app_service_produced" (
    "app_id" varchar(50) NOT NULL,
    "app_service_produced_id" varchar(50) NOT NULL,
    "one_level_name" varchar(100) NOT NULL,
    "one_level_name_en" varchar(100) NOT NULL,
    "two_level_name" varchar(100) NOT NULL,
    "description" varchar(500) NOT NULL,
    "api_file_id" varchar(50) NOT NULL,
    "guide_file_id" varchar(50) NOT NULL,
    "icon_file_id" varchar(50) NOT NULL,
    "service_name" varchar(50) NOT NULL,
    "internal_port" int4 NOT NULL,
    "version" varchar(30) NOT NULL,
    "protocol" varchar(30) NOT NULL,
    "author" varchar(50) NOT NULL,
    "experience_url" varchar(500) DEFAULT NULL,
    "dns_rule_id_list" text DEFAULT NULL,
    "traffic_rule_id_list" text DEFAULT NULL,
    CONSTRAINT  "tbl_app_service_produced_unique_id_name" UNIQUE ("app_id","service_name")
    );

    CREATE TABLE IF NOT EXISTS "tbl_app_service_required" (
    "app_id" varchar(255) NOT NULL,
    "id" varchar(255) NOT NULL,
    "one_level_name" varchar(255) NOT NULL,
    "one_level_name_en" varchar(255) NOT NULL,
    "two_level_name" varchar(255) NOT NULL,
    "two_level_name_en" varchar(255) NOT NULL,
    "ser_name" varchar(255) NOT NULL,
    "version" varchar(255) DEFAULT NULL,
    "requested_permissions" bool DEFAULT NULL,
    "ser_app_id" varchar(255) DEFAULT NULL,
    "package_id" varchar(255) DEFAULT NULL,
    CONSTRAINT  "tbl_app_service_required_unique_id_name" UNIQUE ("app_id","ser_name")
    );

    CREATE TABLE IF NOT EXISTS "tbl_app_certificate" (
    "app_id" varchar(255) NOT NULL,
    "ak" text DEFAULT NULL,
    "sk" text DEFAULT NULL,
    CONSTRAINT "tbl_app_certificate_pkey" PRIMARY KEY ("app_id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_application" (
    "id" varchar(255) NOT NULL,
    "name" varchar(255) NOT NULL,
    "description" varchar(255) DEFAULT NULL,
    "version" varchar(255) NOT NULL,
    "provider" varchar(255) NOT NULL,
    "architecture" varchar(255) DEFAULT NULL,
    "app_class" varchar(255) DEFAULT NULL,
    "type" varchar(255) DEFAULT NULL,
    "industry" varchar(255) DEFAULT NULL,
    "icon_file_id" varchar(255) DEFAULT NULL,
    "guide_file_id" varchar(255) DEFAULT NULL,
    "app_create_type" varchar(255) DEFAULT NULL,
    "create_time" timestamptz(6)  DEFAULT NULL,
    "status" varchar(255) DEFAULT NULL,
    "user_id" varchar(255) DEFAULT NULL,
    "user_name" varchar(255) DEFAULT NULL,
    "mep_host_id" varchar(255) DEFAULT NULL,
    CONSTRAINT  "tbl_application_unique_name_version" UNIQUE ("name","version"),
    CONSTRAINT "tbl_application_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_container_helm_chart" (
    "id" varchar(255) NOT NULL,
    "app_id" varchar(255) NOT NULL,
    "name" varchar(255) DEFAULT NULL,
    "helm_chart_file_id" text DEFAULT NULL,
    CONSTRAINT "tbl_container_helm_chart_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_vm" (
    "id" varchar(255) NOT NULL,
    "app_id" varchar(255) DEFAULT NULL,
    "name" varchar(255) NOT NULL,
    "flavor_id" varchar(255) DEFAULT NULL,
    "image_id" int4 DEFAULT NULL,
    "user_data" text DEFAULT NULL,
    "status" varchar(255) DEFAULT NULL,
    "area_zone" varchar(255) DEFAULT NULL,
    "flavor_extra_specs"  text DEFAULT NULL,
    CONSTRAINT "tbl_vm_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_network" (
    "id" varchar(255) NOT NULL,
    "app_id" varchar(255) DEFAULT NULL,
    "name" varchar(255) NOT NULL,
    "description" varchar(255) DEFAULT NULL,
    CONSTRAINT "tbl_network_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_vm_port" (
    "id" varchar(255) NOT NULL,
    "vm_id" varchar(255) DEFAULT NULL,
    "name" varchar(255) DEFAULT NULL,
    "description" varchar(255) DEFAULT NULL,
    "network_name" varchar(255) DEFAULT NULL,
    CONSTRAINT "tbl_vm_port_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_vm_certificate" (
    "vm_id" varchar(255) DEFAULT NULL,
    "certificate_type" varchar(255) NOT NULL,
    "pwd_certificate" text DEFAULT NULL,
    "key_pair_certificate" text DEFAULT NULL,
    CONSTRAINT "tbl_vm_certificate_pkey" PRIMARY KEY ("vm_id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_vm_flavor" (
    "id" varchar(255) NOT NULL,
    "name" varchar(255) NOT NULL,
    "description" varchar(255) DEFAULT NULL,
    "architecture" varchar(255) DEFAULT NULL,
    "cpu" text DEFAULT NULL,
    "memory" varchar(255) DEFAULT NULL,
    "system_disk_size" int4 DEFAULT NULL,
    "data_disk_size"  int4 DEFAULT NULL,
    "gpu_extra_info" text DEFAULT NULL,
    "other_extra_info" text DEFAULT NULL,
    CONSTRAINT "tbl_vm_flavor_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_vm_image" (
    "id" SERIAL,
    "name" varchar(255) NOT NULL,
    "visible_type" varchar(255) DEFAULT NULL,
    "os_type" varchar(255) DEFAULT NULL,
    "os_version" varchar(255) DEFAULT NULL,
    "os_bit_type" varchar(255) DEFAULT NULL,
    "system_disk_size" int4 DEFAULT NULL,
    "image_file_name" varchar(255) DEFAULT NULL,
    "image_format" varchar(255) DEFAULT NULL,
    "down_load_url" varchar(255) DEFAULT NULL,
    "file_md5" varchar(255) DEFAULT NULL,
    "image_size" bigint DEFAULT NULL,
    "image_slim_status" varchar(50) DEFAULT NULL,
    "status" varchar(255) DEFAULT NULL,
    "create_time" timestamptz(6)  DEFAULT NULL,
    "modify_time" timestamptz(6)  DEFAULT NULL,
    "upload_time" timestamptz(6)  DEFAULT NULL,
    "user_id" varchar(255) DEFAULT NULL,
    "user_name" varchar(255) DEFAULT NULL,
    "file_identifier" varchar(128) DEFAULT NULL,
    "error_type" varchar(32) DEFAULT NULL,
    CONSTRAINT "tbl_vm_image_uniqueName" UNIQUE ("name","user_id"),
    CONSTRAINT "tbl_vm_image_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_vm_instantiate_info" (
    "vm_id" varchar(255) NOT NULL,
    "operation_id" varchar(255) DEFAULT NULL,
    "app_package_id" varchar(255) DEFAULT NULL,
    "distributed_mec_host" varchar(255) DEFAULT NULL,
    "mepm_package_id" varchar(255) DEFAULT NULL,
    "app_instance_id" varchar(255) DEFAULT NULL,
    "vm_instance_id" varchar(255) DEFAULT NULL,
    "status" varchar(255) DEFAULT NULL,
    "vnc_url" varchar(255) DEFAULT NULL,
    "log" text DEFAULT NULL,
    "instantiate_time" timestamptz(6)  DEFAULT NULL,
    CONSTRAINT "tbl_vm_instantiate_info_pkey" PRIMARY KEY ("vm_id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_vm_port_instantiate_info" (
    "vm_id" varchar(255) NOT NULL,
    "network_name" varchar(255) NOT NULL,
    "ip_address" varchar(255) DEFAULT NULL,
    CONSTRAINT  "tbl_vm_port_instantiate_info_unique_id_name" UNIQUE ("vm_id","network_name")
    );

    CREATE TABLE IF NOT EXISTS "tbl_vm_image_export_info" (
    "vm_id" varchar(255) NOT NULL,
    "operation_id" varchar(255) DEFAULT NULL,
    "image_instance_id" varchar(255) DEFAULT NULL,
    "image_name" varchar(255) DEFAULT NULL,
    "format" varchar(255) DEFAULT NULL,
    "download_url" varchar(255) DEFAULT NULL,
    "check_sum" varchar(255) DEFAULT NULL,
    "image_size" varchar(255) DEFAULT NULL,
    "status" varchar(255) DEFAULT NULL,
    "log" text DEFAULT NULL,
    "create_time" timestamptz(6)  DEFAULT NULL,
    CONSTRAINT "tbl_vm_image_export_info_pkey" PRIMARY KEY ("vm_id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_container_app_instantiate_info" (
    "app_id" varchar(255) NOT NULL,
    "app_package_id" varchar(255) DEFAULT NULL,
    "distributed_mec_host" varchar(255) DEFAULT NULL,
    "app_instance_id" varchar(255) DEFAULT NULL,
    "status" varchar(255) DEFAULT NULL,
    "log" text DEFAULT NULL,
    "instantiate_time" timestamptz(6)  DEFAULT NULL,
    CONSTRAINT "tbl_container_app_instantiate_info_pkey" PRIMARY KEY ("app_id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_k8s_pod_instantiate_info" (
    "name" varchar(255) NOT NULL,
    "app_id" varchar(255) NOT NULL,
    "pod_status" varchar(255) DEFAULT NULL,
    "events_info" text DEFAULT NULL,
    CONSTRAINT  "tbl_k8s_pod_instantiate_info_unique_id_name" UNIQUE ("app_id","name")
    );

    CREATE TABLE IF NOT EXISTS "tbl_container_instantiate_info" (
    "name" varchar(255) NOT NULL,
    "pod_name" varchar(255) NOT NULL,
    "cpu_usage" varchar(255) DEFAULT NULL,
    "mem_usage" varchar(255) DEFAULT NULL,
    "disk_usage" varchar(255) DEFAULT NULL,
    CONSTRAINT  "tbl_container_instantiate_info_unique_id_name" UNIQUE ("pod_name","name")
    );

    CREATE TABLE IF NOT EXISTS "tbl_k8s_service_instantiate_info" (
    "name" varchar(255) NOT NULL,
    "app_id" varchar(255) NOT NULL,
    "type" varchar(255) DEFAULT NULL,
    CONSTRAINT "tbl_k8s_service_instantiate_info_pkey" PRIMARY KEY ("name")
    );

    CREATE TABLE IF NOT EXISTS "tbl_k8s_service_port_instantiate_info" (
    "port" varchar(255) NOT NULL,
    "service_name" varchar(255) NOT NULL,
    "target_port" varchar(255) DEFAULT NULL,
    "node_port" varchar(255) DEFAULT NULL,
    CONSTRAINT "tbl_k8s_service_port_instantiate_info_pkey" PRIMARY KEY ("service_name")
    );

    CREATE TABLE IF NOT EXISTS "tbl_operation_status" (
    "id" varchar(255) NOT NULL,
    "user_name" varchar(255) NOT NULL,
    "object_type" varchar(255) DEFAULT NULL,
    "object_id" varchar(255) DEFAULT NULL,
    "object_name" varchar(255) DEFAULT NULL,
    "operation_name" varchar(255) DEFAULT NULL,
    "progress" int4 DEFAULT NULL,
    "status" varchar(255) DEFAULT NULL,
    "error_msg" text DEFAULT NULL,
    "create_time" timestamptz(6) DEFAULT NULL,
    "update_time" timestamptz(6)  DEFAULT NULL,
    CONSTRAINT "tbl_operation_status_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_action_status" (
    "id" varchar(255) NOT NULL,
    "operation_id" varchar(255) NOT NULL,
    "object_type" varchar(255) DEFAULT NULL,
    "object_id" varchar(255) DEFAULT NULL,
    "action_name" varchar(255) DEFAULT NULL,
    "progress" int4 DEFAULT NULL,
    "status" varchar(255) DEFAULT NULL,
    "error_msg" text DEFAULT NULL,
    "status_log" text DEFAULT NULL,
    "update_time" timestamptz(6)  DEFAULT NULL,
    CONSTRAINT "tbl_action_status_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_app_package" (
    "id" varchar(255) NOT NULL,
    "app_id" varchar(255) NOT NULL,
    "package_file_name" varchar(255) DEFAULT NULL,
    CONSTRAINT "tbl_app_package_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_atp_test_task" (
    "id" varchar(255) NOT NULL,
    "app_id" varchar(255) NOT NULL,
    "app_name" varchar(255) DEFAULT NULL,
    "status" varchar(255) DEFAULT NULL,
    "create_time" varchar(255)  DEFAULT NULL,
    CONSTRAINT "tbl_atp_test_task_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_reverse_proxy" (
    "id" varchar(255) NOT NULL,
    "dest_host_id" varchar(255) NOT NULL,
    "dest_host_port" int4 NOT NULL,
    "proxy_port" int4 NOT NULL,
    "type" int4 NOT NULL,
    CONSTRAINT "tbl_reverse_proxy_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_profile" (
    "id" varchar(255) NOT NULL,
    "name" varchar(255) NOT NULL,
    "description" varchar(255) DEFAULT NULL,
    "description_en" varchar(255) DEFAULT NULL,
    "file_path" varchar(255) NOT NULL,
    "deploy_file_path" TEXT NOT NULL,
    "config_file_path" varchar(255) DEFAULT NULL,
    "seq" varchar(255) NOT NULL,
    "create_time" timestamptz(6)  NOT NULL,
    "type" varchar(255) NOT NULL,
    "industry" varchar(255) NOT NULL,
    "topo_file_path" varchar(255) DEFAULT NULL,
    CONSTRAINT "tbl_profile_pkey" PRIMARY KEY ("id")
    );

    CREATE TABLE IF NOT EXISTS "tbl_app_script" (
    "id" varchar(255) NOT NULL,
    "app_id" varchar(255) NOT NULL,
    "name" varchar(255) DEFAULT NULL,
    "script_file_id" text DEFAULT NULL,
    "create_time" timestamptz(6)  NOT NULL,
    CONSTRAINT "tbl_app_script_pkey" PRIMARY KEY ("id")
    );
