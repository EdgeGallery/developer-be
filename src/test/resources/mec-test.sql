/*
 Navicat Premium Data Transfer

 Source Server         : 1
 Source Server Type    : PostgreSQL
 Source Server Version : 100011
 Source Host           : localhost:5432
 Source Catalog        : exampledb
 Source Schema         : mec

 Target Server Type    : PostgreSQL
 Target Server Version : 100011
 File Encoding         : 65001

 Date: 30/12/2019 14:40:23
*/
DROP TABLE IF  EXISTS tbl_appfunction;
DROP TABLE IF  EXISTS tbl_downloadrecord;
DROP TABLE IF  EXISTS  tbl_plugin;
DROP TABLE IF  EXISTS tbl_subtaskstatus;
DROP TABLE IF  EXISTS tbl_testCase;
DROP TABLE  IF  EXISTS tbl_testapp;
DROP TABLE  IF  EXISTS  tbl_testtask;
DROP TABLE  IF  EXISTS tbl_app_project;
DROP TABLE  IF  EXISTS tbl_openmep_capability;
DROP TABLE  IF  EXISTS tbl_openmep_capability_detail;
DROP TABLE  IF  EXISTS tbl_project_image;
DROP TABLE  IF  EXISTS tbl_project_test_config;
DROP TABLE  IF  EXISTS tbl_uploaded_file;
DROP TABLE  IF  EXISTS tbl_helm_template_yaml;
DROP TABLE  IF  EXISTS tbl_service_host;
DROP TABLE  IF  EXISTS tbl_api_emulator;
DROP TABLE  IF  EXISTS tbl_release_config;
DROP TABLE  IF  EXISTS tbl_host_log;
DROP TABLE  IF  EXISTS tbl_vm_regulation;
DROP TABLE  IF  EXISTS tbl_vm_network;
DROP TABLE  IF  EXISTS tbl_vm_system;
DROP TABLE  IF  EXISTS tbl_vm_flavor;
DROP TABLE  IF  EXISTS tbl_project_vm_create_config;
DROP TABLE  IF  EXISTS tbl_project_vm_image_config;

-- ----------------------------
-- Table structure for tbl_appfunction
-- ----------------------------
CREATE TABLE IF NOT EXISTS tbl_appfunction (
  functionid varchar(255)  NOT NULL DEFAULT NULL,
  funcname varchar(255)  NOT NULL DEFAULT NULL,
  funcdesc varchar(255)  NOT NULL DEFAULT NULL,
  addtime varchar(244)  NOT NULL DEFAULT NULL,
  CONSTRAINT tbl_appfunction_pkey PRIMARY KEY (functionid)
)
;

-- ----------------------------
-- Table structure for tbl_downloadrecord
-- ----------------------------
CREATE TABLE IF NOT EXISTS tbl_downloadrecord (
  recordid varchar(255)  NOT NULL DEFAULT NULL,
  pluginid varchar(255)  NOT NULL DEFAULT NULL,
  downloaduserid varchar(255)  NOT NULL DEFAULT NULL,
  downloadusername varchar(255)  NOT NULL DEFAULT NULL,
  score float NOT NULL DEFAULT NULL,
  scoretype int NOT NULL DEFAULT NULL,
  downloadtime timestamp NOT NULL DEFAULT NULL,
  CONSTRAINT tbl_downloadrecord_pkey PRIMARY KEY (recordid)
)
;

-- ----------------------------
-- Table structure for tbl_plugin
-- ----------------------------
CREATE TABLE IF NOT EXISTS  tbl_plugin (
  pluginid varchar(255)  NOT NULL DEFAULT NULL,
  pluginname varchar(255)  NOT NULL DEFAULT NULL,
  introduction varchar(500)  DEFAULT NULL,
  satisfaction FLOAT NOT NULL DEFAULT NULL,
  codelanguage varchar(255)  NOT NULL DEFAULT NULL,
  plugintype int NOT NULL DEFAULT NULL,
  version varchar(255)  NOT NULL DEFAULT NULL,
  downloadcount int NOT NULL DEFAULT NULL,
  logofile varchar(500)  NOT NULL DEFAULT NULL,
  pluginfile varchar(500)  NOT NULL DEFAULT NULL,
  userid varchar(255)  NOT NULL DEFAULT NULL,
  uploadtime timestamp NOT NULL DEFAULT NULL,
  username varchar(255)  NOT NULL DEFAULT NULL,
  pluginsize int NOT NULL DEFAULT NULL,
  apifile varchar(500)  NOT NULL DEFAULT NULL,
  scorecount int NOT NULL DEFAULT NULL,
  pluginFileHashCode varchar(50) DEFAULT NULL,
  CONSTRAINT tbl_plugin_pkey PRIMARY KEY (pluginid)
)
;

-- ----------------------------
-- Table structure for tbl_subtaskstatus
-- ----------------------------
CREATE TABLE IF NOT EXISTS tbl_subtaskstatus (
  executionid varchar(255)  DEFAULT NULL,
  taskid varchar(255)  DEFAULT NULL,
  testcaseid int NOT NULL DEFAULT NULL,
  status varchar(255)  DEFAULT NULL,
  parameters text  DEFAULT NULL,
  CONSTRAINT tbl_subtaskstatus_pkey PRIMARY KEY (executionid)
)
;

-- ----------------------------
-- Table structure for tbl_testCase
-- ----------------------------
CREATE TABLE IF NOT EXISTS tbl_testCase (
  id int NOT NULL DEFAULT NULL,
  scenarios varchar(255)  DEFAULT NULL,
  testsuite varchar(255)  DEFAULT NULL,
  description text  DEFAULT NULL,
  testCaseName varchar(255)  DEFAULT NULL,
  author varchar(255)  DEFAULT NULL,
  inputs text  DEFAULT NULL,
  outputs text  DEFAULT NULL,
  mandatory varchar(255)  DEFAULT NULL,
  subtestcase text  DEFAULT NULL,
  CONSTRAINT tbl_testCase_pkey PRIMARY KEY (id)
)
;

-- ----------------------------
-- Table structure for tbl_testapp
-- ----------------------------
CREATE TABLE IF NOT EXISTS tbl_testapp (
  appid varchar(255)  NOT NULL DEFAULT NULL,
  appname varchar(255)  DEFAULT NULL,
  appfile varchar(255)  NOT NULL DEFAULT NULL,
  affinity varchar(255)  NOT NULL DEFAULT NULL,
  industry varchar(255)  NOT NULL DEFAULT NULL,
  appdesc varchar(500)  DEFAULT NULL,
  uploadtime timestamp NOT NULL DEFAULT NULL,
  userid varchar(255)  NOT NULL DEFAULT NULL,
  logofile varchar(255)  NOT NULL DEFAULT NULL,
  appversion varchar(255)  DEFAULT NULL,
  type varchar(255)  NOT NULL DEFAULT NULL,
  CONSTRAINT tbl_testapp_pkey PRIMARY KEY (appid)
)
;

-- ----------------------------
-- Table structure for tbl_testtask
-- ----------------------------
CREATE TABLE IF NOT EXISTS  tbl_testtask (
  taskid varchar(255)  NOT NULL DEFAULT NULL,
  taskno varchar(255)  NOT NULL DEFAULT NULL,
  status varchar(255)  NOT NULL DEFAULT NULL,
  begintime timestamp NOT NULL DEFAULT NULL,
  endtime timestamp DEFAULT NULL,
  appid varchar(255)  NOT NULL DEFAULT NULL,
  CONSTRAINT tbl_testtask_pkey PRIMARY KEY (taskid)
)
;
-- workspace table start -----------------
CREATE TABLE IF NOT EXISTS tbl_app_project (
  id varchar(50)  NOT NULL DEFAULT NULL,
  name varchar(100)  NOT NULL DEFAULT NULL,
  provider varchar(100)  NOT NULL DEFAULT NULL,
  platform text DEFAULT NULL,
  type varchar(50)  NOT NULL DEFAULT NULL,
  description text  DEFAULT NULL,
  status varchar(20)  NOT NULL DEFAULT NULL,
  user_id varchar(50)  NOT NULL DEFAULT NULL,
  create_date timestamp DEFAULT NULL,
  last_test_id varchar(50)  DEFAULT NULL,
  version varchar(50)  DEFAULT NULL,
  capabilities text DEFAULT NULL,
  industries text DEFAULT NULL,
  project_type varchar(10)  DEFAULT NULL,
  icon_file_id varchar(50)  DEFAULT NULL,
  open_capability_id varchar(50)  DEFAULT NULL,
  deploy_platform varchar(100) NOT NULL  DEFAULT NULL,
  CONSTRAINT tbl_app_project_pkey PRIMARY KEY (id)
)
;


CREATE TABLE IF NOT EXISTS tbl_openmep_capability (
  group_id varchar(50)  NOT NULL DEFAULT NULL,
  one_level_name varchar(255)  DEFAULT NULL,
  one_level_name_en varchar(255)  DEFAULT NULL,
  two_level_name varchar(255)  DEFAULT NULL,
  two_level_name_en varchar(255)  DEFAULT NULL,
  type varchar(20)  DEFAULT NULL,
  description text  DEFAULT NULL,
  description_en text  DEFAULT NULL,
  icon_file_id varchar(50)  DEFAULT NULL,
  author varchar(50)  DEFAULT NULL,
  select_count int DEFAULT NULL,
  upload_time timestamp DEFAULT NULL,
  CONSTRAINT tbl_openmep_capability_pkey PRIMARY KEY (group_id)
)
;


CREATE TABLE IF NOT EXISTS tbl_openmep_capability_detail (
  detail_id varchar(50)  NOT NULL DEFAULT NULL,
  service varchar(100)  DEFAULT NULL,
  service_en varchar(100)  DEFAULT NULL,
  version varchar(100)  DEFAULT NULL,
  description text  DEFAULT NULL,
  description_en text  DEFAULT NULL,
  provider varchar(100)  DEFAULT NULL,
  group_id varchar(50)  DEFAULT NULL,
  api_file_id varchar(255)  DEFAULT NULL,
  guide_file_id varchar(255) DEFAULT NULL,
  guide_file_id_en varchar(255) DEFAULT NULL,
  upload_time  varchar(50) DEFAULT NULL,
  host varchar(50) DEFAULT NULL,
  port int DEFAULT '-1',
  protocol varchar(255) DEFAULT NULL,
  app_id varchar(255) DEFAULT NULL,
  package_id varchar(255) DEFAULT NULL,
  user_id varchar(255) DEFAULT NULL,
  CONSTRAINT tbl_openmep_capability_detail_pkey PRIMARY KEY (detail_id)
)
;


CREATE TABLE IF NOT EXISTS tbl_project_image (
  id varchar(50)  NOT NULL DEFAULT NULL,
  pod_name varchar(100)  NOT NULL DEFAULT NULL,
  pod_containers text  NOT NULL DEFAULT NULL,
  project_id varchar(50)  NOT NULL DEFAULT NULL,
  svc_type varchar(255)  NOT NULL DEFAULT NULL,
  svc_port varchar(255)  NOT NULL DEFAULT NULL,
  svc_node_port varchar(255)  NOT NULL DEFAULT NULL,
  CONSTRAINT tbl_project_image_pkey PRIMARY KEY (id)
)
;


CREATE TABLE IF NOT EXISTS tbl_project_test_config (
  test_id varchar(50)  NOT NULL DEFAULT NULL,
  project_id varchar(50)  NOT NULL DEFAULT NULL,
  agent_config text  DEFAULT NULL,
  image_file_id varchar(255)  DEFAULT NULL,
  deploy_file_id varchar(50)  DEFAULT NULL,
  app_api_file_id varchar(50)  DEFAULT NULL,
  private_host int DEFAULT NULL,
  platform varchar(100)  DEFAULT NULL,
  status varchar(100)  DEFAULT NULL,
  access_url varchar(200)  DEFAULT NULL,
  error_log text  DEFAULT NULL,
  deploy_date timestamp DEFAULT NULL,
  hosts text  DEFAULT NULL,
  app_instance_id varchar(50)  DEFAULT NULL,
  work_load_id varchar(255)  DEFAULT NULL,
  pods varchar(255)  DEFAULT NULL,
  deploy_status varchar(255)  DEFAULT NULL,
  stage_status  varchar(255)  DEFAULT NULL,
  lcm_token  varchar(1000) DEFAULT NULL,
  package_id varchar(255)  DEFAULT NULL,
  CONSTRAINT tbl_project_test_config_pkey PRIMARY KEY (test_id)
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

CREATE TABLE IF NOT EXISTS tbl_helm_template_yaml (
  file_id varchar(50) NOT NULL DEFAULT NULL,
  file_name varchar(255) DEFAULT NULL,
  user_id varchar(50) DEFAULT NULL,
  project_id varchar(50) DEFAULT NULL,
  content text DEFAULT NULL,
  upload_time_stamp bigint DEFAULT NULL,
  config_type varchar(50)  DEFAULT NULL,
  CONSTRAINT tbl_helm_template_yaml_pkey PRIMARY KEY (file_id)
)
;

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
  password varchar(255)  DEFAULT NULL
)
;

CREATE TABLE IF NOT EXISTS tbl_api_emulator (
  id varchar(50) NOT NULL,
  user_id varchar(50) NOT NULL,
  host_id varchar(50) NOT NULL,
  port int NOT NULL,
  workload_id varchar(50) NOT NULL,
  create_time varchar(50) NOT NULL
)
;
CREATE TABLE IF NOT EXISTS tbl_release_config (
  release_id varchar(255) NOT NULL,
  project_id varchar(255) NOT NULL,
  guide_file_id varchar(255) DEFAULT NULL,
  appinstance_id varchar(255) DEFAULT NULL,
  capabilities_detail text  DEFAULT NULL,
  atp_test text DEFAULT NULL,
  test_status varchar(255) DEFAULT NULL,
  create_time TIMESTAMP(0) DEFAULT NULL,
  CONSTRAINT tbl_release_config_pkey PRIMARY KEY (release_id)
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
CREATE TABLE IF NOT EXISTS  tbl_host_log  (
       log_id  varchar(50) NOT NULL,
       host_ip  varchar(50) NOT NULL,
       user_name  varchar(50) DEFAULT NULL,
       user_id  varchar(50) DEFAULT NULL,
       project_id  varchar(50) DEFAULT NULL,
       project_name  varchar(50) DEFAULT NULL,
       app_instances_id  varchar(50) DEFAULT NULL,
       deploy_time  varchar(50) DEFAULT NULL,
       status  varchar(50) DEFAULT NULL,
       operation  varchar(50) DEFAULT NULL,
       host_id  varchar(50) DEFAULT NULL
    )
    ;

CREATE TABLE IF NOT EXISTS  tbl_vm_regulation  (
       regulation_id  SERIAL,
       architecture  varchar(50) DEFAULT NULL,
       name_zh  varchar(50) NOT NULL DEFAULT NULL,
       name_en  varchar(50) NOT NULL DEFAULT NULL,
       scene_zh  varchar(255) DEFAULT NULL,
       scene_en  varchar(255) DEFAULT NULL,
       memory  int4  DEFAULT NULL,
       cpu  int4  DEFAULT NULL,
       system_disk  int4  DEFAULT NULL,
       data_disk  int4  DEFAULT NULL,
       gpu  varchar(50)  DEFAULT NULL,
       other_ability  varchar(255)  DEFAULT NULL
    )
    ;
    CREATE TABLE IF NOT EXISTS  tbl_vm_network  (
       network_type  varchar(50) DEFAULT NULL,
       description_zh  varchar(255) DEFAULT NULL,
       description_en  varchar(255) DEFAULT NULL
    )
    ;
    CREATE TABLE IF NOT EXISTS  tbl_vm_system  (
       system_id  SERIAL,
       type  varchar(50) DEFAULT NULL,
       operate_system  varchar(50) DEFAULT NULL,
       version  varchar(50) NOT NULL DEFAULT NULL,
       system_bit  varchar(50) DEFAULT NULL,
       system_disk  int4  DEFAULT NULL
    )
    ;
    CREATE TABLE IF NOT EXISTS  tbl_vm_flavor  (
       architecture  varchar(50) DEFAULT NULL,
       flavor  varchar(50) DEFAULT NULL,
       constraints  varchar(50) DEFAULT NULL
    )
    ;
    CREATE TABLE IF NOT EXISTS  tbl_project_vm_create_config  (
       vm_id   varchar(255) NOT NULL DEFAULT NULL,
       project_id  varchar(50) DEFAULT NULL,
       vm_regulation_desc  varchar(512) DEFAULT NULL,
       vm_system_desc  varchar(512) NOT NULL DEFAULT NULL,
       vm_network_desc  varchar(512) DEFAULT NULL,
       vm_name  varchar(50)  DEFAULT NULL,
       host  varchar(512)  DEFAULT NULL,
       status  varchar(50)  DEFAULT NULL,
       stage_status  varchar(500)  DEFAULT NULL,
       lcm_token  varchar(1024)  DEFAULT NULL,
       vm_info  varchar(512)  DEFAULT NULL,
       app_instance_id  varchar(50)  DEFAULT NULL,
       create_time   timestamp(6)  DEFAULT NULL,
       log  text  DEFAULT NULL,
      CONSTRAINT  tbl_vm_create_config_pkey  PRIMARY KEY (vm_id)
    )
    ;
    CREATE TABLE IF NOT EXISTS  tbl_project_vm_image_config  (
       vm_id   varchar(255) NOT NULL DEFAULT NULL,
       image_id  varchar(50) DEFAULT NULL,
       project_id  varchar(50) DEFAULT NULL,
       vm_name  varchar(50) NOT NULL DEFAULT NULL,
       image_name  varchar(50) DEFAULT NULL,
       app_instance_id  varchar(50)  DEFAULT NULL,
       host_ip  varchar(50)  DEFAULT NULL,
       sum_chunk_num  varchar(50)  DEFAULT NULL,
       chunk_size  varchar(50)  DEFAULT NULL,
       stage_status  varchar(500)  DEFAULT NULL,
       status  varchar(512)  DEFAULT NULL,
       lcm_token  varchar(1024)  DEFAULT NULL,
       create_time   timestamp(6)  DEFAULT NULL,
       log  text  DEFAULT NULL,
      CONSTRAINT  tbl_vm_image_config_pkey  PRIMARY KEY (vm_id)
    )
    ;
-- workspace table end -----------------


-- ----------------------------
-- Records of tbl_appfunction
-- ----------------------------
MERGE INTO tbl_appfunction  KEY(functionid) VALUES ('53fc40e9a1f048e4b4310e8ac30856b3', 'CPU', '处理速度', '2019-10-23 03:27:36'),
('343d42a3b59c46f9afda063b8be4cc8f', 'GPU', '处理图片', '2019-10-23 03:27:54'),
('526f86afd6b841ae9df56e30d37f0574', 'Memory Disk', '存储优先', '2019-11-02 10:48:33'),
('8167fc046c2d4e42997c612fdfbd7c8f', 'AI', '存储', '2019-10-23 05:37:46');

-- workspace mep capability init --
MERGE INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) KEY(group_id) VALUES ('c0db376b-ae50-48fc-b9f7-58a609e3ee12', 'Platform basic services', 'Platform basic services', 'Traffic', 'Traffic', 'OPENMEP', 'L3/L4规则API,L7规则API', 'L3/L4规则API,L7规则API'),
('a6efaa2c-ad99-432f-9405-e28e90f44f15', 'Platform basic services', 'Platform basic services', 'Service Discovery', 'Service Discovery', 'OPENMEP', 'Service Discovery', 'Service Discovery'),
('406593b4-c782-409c-8f46-a6fd5e1f6221', 'Platform basic services', 'Platform basic services', 'Location', 'Location', 'OPENMEP', '自定义不规则区域分析API,标准栅格区域分析API,特定人群流动分析API,API区域原子报表分析,匿名历史位置轨', 'TEST'),
('72a1434d-fbb0-459b-9b92-ce1e02a121c2', 'Platform basic services', 'Platform basic services', 'Bandwidth', 'Bandwidth', 'OPENMEP', 'MBB应用,FMC应用,UIC应用', 'TEST'),
('d8f06d28-390c-4a06-905e-120f56279bbc', 'Platform basic services', 'Platform basic services', 'Face Recognition', 'Face Recognition', 'OPENMEP', 'Face Recognition', 'Face Recognition');


MERGE INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, upload_time, user_id) KEY(detail_id)
VALUES ('8d93cb64-e9ff-468f-a5b1-160efa5c4f05', 'Face Recognition service plus', 'Face Recognition service plus', 'v1', 'provide the face recognition plus capabilities for apps', 'provide the face recognition plus capabilities for apps', 'Huawei', 'd8f06d28-390c-4a06-905e-120f56279bbc', '7dd477d8-bcc0-4e2a-a48d-2b587a30026a', 'b8b5d055-1024-4ea8-8439-64de19875834', 'b8b5d055-1024-4ea8-8439-64de19875834', 9999, 'face-recognition-plus', '2020-11-20 00:00:00.000000', 'admin');
MERGE INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, upload_time, user_id) KEY(detail_id)
VALUES ('6f250fc0-0961-470f-bf17-e9bba8e56c12', 'Face Recognition service', 'Face Recognition service plus', 'v1', 'provide the face recognition capabilities for apps', 'provide the face recognition plus capabilities for apps', 'Huawei', 'd8f06d28-390c-4a06-905e-120f56279bbc', 'd0f8fa57-2f4c-4182-be33-0a508964d04a', '10d8a909-742a-433f-8f7a-5c7667adf825', 'b8b5d055-1024-4ea8-8439-64de19875834', 9997, 'face-recognition', '2020-11-20 00:00:00.000000', 'admin');
MERGE INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, upload_time, user_id) KEY(detail_id)
VALUES ('143e8608-7304-4932-9d99-4bd6b115dac8', 'Service Discovery', 'Service Discovery', 'v1', 'provide the service discovery capabilities for apps', 'provide the service discovery capabilities for apps', 'Huawei', 'a6efaa2c-ad99-432f-9405-e28e90f44f15', '540e0817-f6ea-42e5-8c5b-cb2daf9925a3', '9bb4a85f-e985-47e1-99a4-20c03a486864', 'b8b5d055-1024-4ea8-8439-64de19875834', 8684, 'service-discovery', '2020-11-20 00:00:00.000000', 'admin');
MERGE INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, upload_time, user_id) KEY(detail_id)
VALUES ('ee7fbc17-f370-4c02-a9ab-680a41cd0255', 'Bandwidth service', 'Bandwidth service', 'v1', 'provide the bandwidth capabilities for apps', 'provide the bandwidth capabilities for apps', 'Huawei', '72a1434d-fbb0-459b-9b92-ce1e02a121c2', '7c544903-aa4f-40e0-bd8c-cf6e17c37c12', '6736ec41-eb7e-4dca-bda2-3b4e10d0a294', 'b8b5d055-1024-4ea8-8439-64de19875834', 8489, 'bandwidth-service', '2020-11-20 00:00:00.000000', 'admin');
MERGE INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, upload_time, user_id) KEY(detail_id)
VALUES ('146f4f87-4027-4ad8-af99-ec4a6f6bcc3c', 'Location service', 'Location service', 'v1', 'provide the location capabilities for apps', 'provide the location capabilities for apps', 'Huawei', '406593b4-c782-409c-8f46-a6fd5e1f6221', '688f259e-48eb-407d-8604-7feb19cf1f44', 'b0819798-e932-415c-95f5-dead04ef2fba', 'b8b5d055-1024-4ea8-8439-64de19875834', 8487, 'location-service', '2020-11-20 00:00:00.000000', 'admin');
MERGE INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, upload_time, user_id) KEY(detail_id)
VALUES ('3fda958c-ef56-44c9-bf3b-469cf5d54e33', 'Traffic service', 'Traffic service', 'v1', 'provide the traffic capabilities for apps', 'provide the traffic capabilities for apps', 'Huawei', 'c0db376b-ae50-48fc-b9f7-58a609e3ee12', '9f1f13a0-8554-4dfa-90a7-d2765238fca7', '5110740f-305c-4553-920e-2b11cd9f64c1', 'b8b5d055-1024-4ea8-8439-64de19875834', 8456, 'traffice-service', '2020-11-20 00:00:00.000000', 'admin');

MERGE INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('7dd477d8-bcc0-4e2a-a48d-2b587a30026a', 'Face Recognition service plus.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/7dd477d8-bcc0-4e2a-a48d-2b587a30026a');
MERGE INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('d0f8fa57-2f4c-4182-be33-0a508964d04a', 'Face Recognition service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/d0f8fa57-2f4c-4182-be33-0a508964d04a');
MERGE INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('540e0817-f6ea-42e5-8c5b-cb2daf9925a3', 'Service Discovery.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/540e0817-f6ea-42e5-8c5b-cb2daf9925a3');
MERGE INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('7c544903-aa4f-40e0-bd8c-cf6e17c37c12', 'Bandwidth service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/7c544903-aa4f-40e0-bd8c-cf6e17c37c12');
MERGE INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('688f259e-48eb-407d-8604-7feb19cf1f44', 'Location service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/688f259e-48eb-407d-8604-7feb19cf1f44');
MERGE INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) KEY(file_id) VALUES ('9f1f13a0-8554-4dfa-90a7-d2765238fca7', 'Traffic service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/uploaded_files/mep_capability/9f1f13a0-8554-4dfa-90a7-d2765238fca7');

-- workspace mep capability init end--


-- workspace mep host init --

MERGE INTO tbl_service_host (host_id, name, address, architecture, status, ip, os, port_range_min, port_range_max, port, protocol, delete) KEY(host_id) VALUES ('3c55ac26-60e9-42c0-958b-1bf7ea4da60a', 'Node1', 'XIAN', 'X86', 'NORMAL', '127.0.0.1', 'Ubuntu', 30000, 32767, 30201, 'http', null);

-- workspace mep host init end--
-- workspace table end -----------------
