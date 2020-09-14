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
-- ----------------------------
-- Records of tbl_service_host
-- ----------------------------
INSERT INTO tbl_service_host(host_id,name,address,architecture,status,ip,port,os,port_range_min,port_range_max) VALUES ('c8aac2b2-4162-40fe-9d99-0630e3245cf7', 'host-1', 'xian', 'ARM','NORMAL','10.1.12.1',8999,'liunx',30000,300001);
INSERT INTO tbl_service_host(host_id,name,address,architecture,status,ip,port,os,port_range_min,port_range_max) VALUES ('c8aac2b2-4162-40fe-9d99-0630e3245cf8', 'host-2', 'xian', 'ARM','NORMAL','10.1.12.1',8999,'liunx',30000,300001);

-- ----------------------------
-- Records of tbl_openmep_capability   tbl_openmep_capability_detail
-- ----------------------------
INSERT INTO tbl_openmep_capability (group_id,name,type,description)values('e111f3e7-90d8-4a39-9874-ea6ea6752ef3','group-1','OPENMEP','group1');
INSERT INTO tbl_openmep_capability_detail (detail_id,group_id, service, version, description, api_file_id, provider)values
( 'e111f3e7-90d8-4a39-9874-ea6ea6752ef4', 'e111f3e7-90d8-4a39-9874-ea6ea6752ef3', 'Face Reg', '1.0.0', 'Face Reg','e111f3e7-90d8-4a39-9874-ea6ea6752ef5', 'huawei');

-- ----------------------------
-- Records of tbl_uploaded_file
-- ----------------------------
INSERT INTO tbl_uploaded_file (file_id, file_name,is_temp, user_id, upload_date, file_path)values
( 'e111f3e7-90d8-4a39-9874-ea6ea6752ef5','pet.json', false, 'e111f3e7-90d8-4a39-9874-ea6ea6752ef6', '2019-10-23 03:27:36', '/workspace/200dfab1-3c30-4fc7-a6ca-ed6f0620a85e\ad66d1b6-5d29-487b-9769-be48b62aec2e');

-- ----------------------------
-- Records of tbl_app_project
-- ----------------------------
INSERT INTO tbl_app_project VALUES ('200dfab1-3c30-4fc7-a6ca-ed6f0620a85e', 'p-001', 'hw', '["test-plat"]', 'new', 'test', 'ONLINE', 'f24ea0a2-d8e6-467c-8039-94f0d29bac43', '2020-03-20 09:37:15.979+08', NULL, '1.0.0', '[{"groupId":"7503ed60-ace4-41c7-b8d8-36dc1fc1e238","name":"group-001","type":"OPENMEP","description":"test","capabilityDetailList":[{"detailId":"998c3255-e654-46a7-87e9-f7d871a10a86","groupId":"7503ed60-ace4-41c7-b8d8-36dc1fc1e238","service":"face reg","version":"1.0.0","description":"string","provider":"hw","apiFileId":"7690bfbb-6865-4c08-8807-92a95d899334"}]}]', '["test industry1","test industry2"]', 'CREATE_NEW', 'ad66d1b6-5d29-487b-9769-be48b62aec2e', NULL);

-- ----------------------------
-- Records of tbl_plugin
-- ----------------------------
INSERT INTO tbl_plugin VALUES ('586224da-e1a2-4893-a5b5-bf766fdfb8c7', 'test001', 'test 0318', 4, 'JAVA', 1, '1.0.0', 1, 'testdata/idea.png', 'testdata/IDEAPluginDev.zip', 'f24ea0a2-d8e6-467c-8039-94f0d29bac43', '2020-03-18 09:18:05.188+08', 'helongfei999', 3440123, 'testdata/plugin.json', 1);

-- ----------------------------
-- Records of tbl_testapp
-- ----------------------------
INSERT INTO tbl_testapp VALUES ('4c22f069-e489-47cd-9c3c-e21741c857db', 'face_recognition1.2', 'D://home//app//temp_a826d364-dbf4-462d-8c65-8d16caa35106.csar', 'GPU,CPU', 'test industry', 'test 0318', '2020-03-18 15:50:36+08', 'f24ea0a2-d8e6-467c-8039-94f0d29bac43', 'D://home//app//1584517821651.png', '1.2', 'Video');
INSERT INTO tbl_testapp VALUES ('fd497d95-7c98-40cb-bc90-308bdefc0e39', 'face_recognition1.2', 'D://home//app//temp_a826d364-dbf4-462d-8c65-8d16caa35106.csar', 'GPU', 'test industry1,test industry2', '0318', '2020-03-18 16:19:36+08', 'f24ea0a2-d8e6-467c-8039-94f0d29bac43', 'D://home//app//1584519514112.png', '1.2', 'Video Surveillance');

-- ----------------------------
-- Records of tbl_testtask 
-- ----------------------------
INSERT INTO tbl_testtask VALUES ('11e12b66-508f-48d4-bbc8-3ed99631cf92', 'MEC20200318001', 'COMPLETED', '2020-03-18 16:19:36.127+08', '2020-03-18 16:20:30.013+08', 'fd497d95-7c98-40cb-bc90-308bdefc0e39');

-- ----------------------------
-- Records of tbl_subtaskstatus 
-- ----------------------------
INSERT INTO tbl_subtaskstatus VALUES ('567e9955-65a7-4c9a-bd7c-b739b8969617', '11e12b66-508f-48d4-bbc8-3ed99631cf92', 4, 'COMPLETED', NULL);
INSERT INTO tbl_subtaskstatus VALUES ('3e292ee7-2c62-4512-b515-f6b5d73724db', '11e12b66-508f-48d4-bbc8-3ed99631cf92', 2, 'COMPLETED', NULL);
INSERT INTO tbl_subtaskstatus VALUES ('16f75093-a279-4458-b160-553f26fca698', '11e12b66-508f-48d4-bbc8-3ed99631cf92', 5, 'COMPLETED', NULL);
INSERT INTO tbl_subtaskstatus VALUES ('dc8d7e6f-1bf1-4598-880b-ba8ce72181e0', '11e12b66-508f-48d4-bbc8-3ed99631cf92', 3, 'COMPLETED', NULL);
INSERT INTO tbl_subtaskstatus VALUES ('9e9b8588-f192-4012-bbec-848144d8d787', '11e12b66-508f-48d4-bbc8-3ed99631cf92', 1, 'COMPLETED', NULL);

-- ----------------------------
-- Records of tbl_uploaded_file 
-- ----------------------------
INSERT INTO tbl_uploaded_file VALUES ('ad66d1b6-5d29-487b-9769-be48b62aec2e', '1584500561029.png', 0, 'f24ea0a2-d8e6-467c-8039-94f0d29bac43', '2020-03-20 09:57:36.074+08', '/workspace/200dfab1-3c30-4fc7-a6ca-ed6f0620a85e\ad66d1b6-5d29-487b-9769-be48b62aec2e');

-- ----------------------------
-- Records of tbl_project_image 
-- ----------------------------
INSERT INTO tbl_project_image VALUES ('78055873-58cf-4712-8f12-cfdd4e19f268', 'p-001-image001', '1.0.0', '200dfab1-3c30-4fc7-a6ca-ed6f0620a85e', 'PLATFORM', 3000, 3020);

-- ----------------------------
-- Records of tbl_project_test_config
-- ----------------------------
-- INSERT INTO tbl_project_test_config VALUES ("TEST-CONFIG-ID-fwegert", "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e", "agent_config", "image_file_id", "e111f3e7-90d8-4a39-9874-ea6ea6752ef5", "status", "access_url", "error_log", "deploy_date", "hosts", "app_instance_id", "work_load_id");
INSERT INTO tbl_project_test_config VALUES ('00001', '200dfab1-3c30-4fc7-a6ca-ed6f0620a85e', '{"serviceName":"servicename-001","href":"http://127.0.0.1","port":8254}', '["image-file-id-001","image-file-id-001"]', 'e111f3e7-90d8-4a39-9874-ea6ea6752ef5', 'RUNNING', 'access_url', 'error_log', '2020-03-20 09:57:36.074+08', '[{"host_id":"c8aac2b2-4162-40fe-9d99-0630e3245cf7","name":"host-1","address":"xian","architecture":"ARM","status":"NORMAL","ip":"10.1.12.1","port":8999,"os":"liunx","port_range_min":30000,"port_range_max":30200}]', 'app_instance_id', 'work_load_id');
