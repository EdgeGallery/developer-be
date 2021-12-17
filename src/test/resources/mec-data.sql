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
MERGE INTO tbl_capability_group (id, name, name_en, type,description, description_en,icon_file_id,author,create_time,update_time)KEY(id)
values ('e111f3e7-90d8-4a39-9874-ea6ea6752et4','group-1', 'group-1', 'OPENMEP','group1', 'group1','e111f3e7-90d8-4a39-9874-ea6ea6752e90','admin',1633689974911,1633689974911);

MERGE INTO tbl_capability (id,group_id, name, name_en,version, description, description_en, api_file_id, provider, guide_file_id, guide_file_id_en,
port, host, upload_time, user_id, experience_url,select_count)  KEY(id) values
( 'e111f3e7-90d8-4a39-9874-ea6ea6752ef0', 'e111f3e7-90d8-4a39-9874-ea6ea6752et4', 'Face Reg', 'Face Reg', '1.0.0', 'face-recognition-plus', 'Face Reg','e111f3e7-90d8-4a39-9874-ea6ea6752ef5', 'huawei',
'b8b5d055-1024-4ea8-8439-64de19875834', 'b8b5d055-1024-4ea8-8439-64de19875834', 9999, 'face-recognition-plus', 0, 'admin','',0);


MERGE INTO tbl_capability_group (id, name, name_en, type,description, description_en,icon_file_id,author,create_time,update_time)KEY(id)
values ('e111f3e7-90d8-4a39-9874-ea6ea6752et5','group-2', 'group-2', 'OPENMEP','group2', 'group2','e111f3e7-90d8-4a39-9874-ea6ea6752e90','admin',1633690318869,1633690318869);

MERGE INTO tbl_capability (id,group_id, name, name_en, version, description, description_en, api_file_id, provider, guide_file_id, guide_file_id_en, port,
host, upload_time, user_id, experience_url,select_count)  KEY(id) values
( 'e111f3e7-90d8-4a39-9874-ea6ea6752ef4', 'e111f3e7-90d8-4a39-9874-ea6ea6752et5', 'Face Reg', 'Face Reg', '1.0.0', 'face-recognition-plus', 'Face Reg','e111f3e7-90d8-4a39-9874-ea6ea6752ef5', 'huawei',
'b8b5d055-1024-4ea8-8439-64de19875834', 'b8b5d055-1024-4ea8-8439-64de19875834', 9999, 'face-recognition-plus', 0, 'admin','',0);

MERGE INTO tbl_capability_group (id, name, name_en, type,description, description_en,icon_file_id,author,create_time,update_time)KEY(id)
values ('e111f3e7-90d8-4a39-9874-ea6ea6752et6','group-3', 'group-3', 'OPENMEP','group3', 'group3','e111f3e7-90d8-4a39-9874-ea6ea6752e90','admin',1633690987061,1633690987061);

MERGE INTO tbl_capability (id,group_id, name, name_en, version, description, description_en, api_file_id, provider, guide_file_id,
guide_file_id_en, port, host, upload_time, user_id, experience_url,select_count)  KEY(id) values
( 'e111f3e7-90d8-4a39-9874-ea6ea6752efgd', 'e111f3e7-90d8-4a39-9874-ea6ea6752et6', 'Face Reg', 'Face Reg', '1.0.0', 'face-recognition-plus', 'Face Reg','e111f3e7-90d8-4a39-9874-ea6ea6752ef5', 'huawei',
'b8b5d055-1024-4ea8-8439-64de19875834', 'b8b5d055-1024-4ea8-8439-64de19875834', 9999, 'face-recognition-plus', 0, 'admin','',0);
-- Records of tbl_openmep_capability   tbl_openmep_capability_detail
-- ----------------------------
MERGE INTO tbl_capability_group (id, name, name_en, type,description, description_en)KEY(id) 
values ('e111f3e7-90d8-4a39-9874-ea6ea6752ef3','group-1', 'group-1', 'OPENMEP','group1', 'group1');
MERGE INTO tbl_capability (id,group_id, name, name_en, version, description, description_en, api_file_id, provider, guide_file_id, guide_file_id_en, port, host, upload_time, user_id, experience_url)  KEY(id) values
( 'e111f3e7-90d8-4a39-9874-ea6ea6752ef4', 'e111f3e7-90d8-4a39-9874-ea6ea6752ef3', 'Face Reg', 'Face Reg', '1.0.0', 'face-recognition-plus', 'Face Reg','e111f3e7-90d8-4a39-9874-ea6ea6752ef5', 'huawei',
'b8b5d055-1024-4ea8-8439-64de19875834', 'b8b5d055-1024-4ea8-8439-64de19875834', 9999, 'face-recognition-plus', 0, 'admin','');

MERGE INTO tbl_capability (id,group_id, name, name_en,version, description, description_en, api_file_id, provider, guide_file_id, guide_file_id_en, port, host, upload_time, user_id, experience_url)  KEY(id) values
( 'e111f3e7-90d8-4a39-9874-ea6ea6752ef0', 'e111f3e7-90d8-4a39-9874-ea6ea6752ef3', 'Face Reg', 'Face Reg', '1.0.0', 'face-recognition-plus', 'Face Reg','e111f3e7-90d8-4a39-9874-ea6ea6752ef5', 'huawei',
'b8b5d055-1024-4ea8-8439-64de19875834', 'b8b5d055-1024-4ea8-8439-64de19875834', 9999, 'face-recognition-plus', 0, 'admin','');

MERGE INTO tbl_capability_group (id, name, name_en, type,description, description_en)  KEY(id) values 
('e111f3e7-90d8-4a39-9874-ea6ea6752eb2','group-3', 'group-3', 'OPENMEP','group3', 'group3');

MERGE INTO tbl_capability (id,group_id, name, name_en, version, description, description_en, api_file_id, provider, guide_file_id, guide_file_id_en, port, host, upload_time, user_id, experience_url)  KEY(id) values
( 'e111f3e7-90d8-4a39-9874-ea6ea6752efgd', 'e111f3e7-90d8-4a39-9874-ea6ea6752eb2', 'Face Reg', 'Face Reg', '1.0.0', 'face-recognition-plus', 'Face Reg','e111f3e7-90d8-4a39-9874-ea6ea6752ef5', 'huawei',
'b8b5d055-1024-4ea8-8439-64de19875834', 'b8b5d055-1024-4ea8-8439-64de19875834', 9999, 'face-recognition-plus', 0, 'admin','');
-- ----------------------------
-- Records of tbl_uploaded_file
-- ----------------------------
MERGE INTO tbl_uploaded_file (file_id, file_name,is_temp, user_id, upload_date, file_path)  KEY(file_id) values
( 'e111f3e7-90d8-4a39-9874-ea6ea6752ef5','pet.json', false, 'e111f3e7-90d8-4a39-9874-ea6ea6752ef6', '2019-10-23 03:27:36', '/workspace/200dfab1-3c30-4fc7-a6ca-ed6f0620a85e\ad66d1b6-5d29-487b-9769-be48b62aec2e');

MERGE INTO tbl_container_image (image_id, image_name, image_version, user_id, user_name)  KEY(image_id) values
( '6ababcec-2934-43d9-afc8-d3d403ebc782','test1','1.0','c5c7c35a-f85b-441c-9307-5516b951efd2','author');
--
--MERGE INTO tbl_container_image (image_id, image_name, image_version, user_id, user_name, file_name, image_path)  KEY(image_id) values
--( '6ababcec-2934-43d9-afc8-d3d403ebc785','test2','2.0','c5c7c35a-f85b-441c-9307-5516b951efd3','author1','test2.tar','xxxx')
---- ----------------------------

-- ----------------------------
-- Records of tbl_network
-- ----------------------------
MERGE INTO tbl_network KEY(app_id,name) VALUES('776be50f-f6eb-4aed-b562-e9e5b914dc59','init-application','MEC_APP_Private','N6网络，端侧设备在访问边缘应用时，需要通过该网络进行访问');
MERGE INTO tbl_network KEY(app_id,name) VALUES('776be50f-f6eb-4aed-b562-e9e5b914dc60','init-application','MEC_APP_MP1','与边缘计算平台之间的网络，当应用存在服务依赖或需要发布服务时，需要该网络');
MERGE INTO tbl_network KEY(app_id,name) VALUES('776be50f-f6eb-4aed-b562-e9e5b914dc61','init-application','MEC_APP_Public','Internet网络');
MERGE INTO tbl_network KEY(app_id,name) VALUES('560e554c-7ef4-4f21-b2b5-e33fa64aa069','4cbbab9d-c48f-4adb-ae82-d1816d8edd7c','MEC_APP_Public','Internet网络');
MERGE INTO tbl_network KEY(app_id,name) VALUES('560e554c-7ef4-4f21-b2b5-e33fa64aa068','4cbbab9d-c48f-4adb-ae82-d1816d8edd7c','MEC_APP_MP1','N6网络');
MERGE INTO tbl_network KEY(app_id,name) VALUES('560e554c-7ef4-4f21-b2b5-e33fa64aa067','4cbbab9d-c48f-4adb-ae82-d1816d8edd7c','MEC_APP_Private','MEP网络');
MERGE INTO tbl_network KEY(app_id,name) VALUES('560e554c-7ef4-4f21-b2b5-e33fa64aa070','3f11715f-b59e-4c23-965b-b7f9c34c20d1','MEC_APP_Public','Internet网络');
MERGE INTO tbl_network KEY(app_id,name) VALUES('560e554c-7ef4-4f21-b2b5-e33fa64aa071','3f11715f-b59e-4c23-965b-b7f9c34c20d1','MEC_APP_Private','MEP网络');
-- ----------------------------
-- Records of tbl_vm_flavor
-- ----------------------------
MERGE INTO tbl_vm_flavor KEY(id) VALUES('3ef2bea0-5e23-4fab-952d-cc9e6741dbe7', 'General Computing-1', 'Ordinary APP', 'X86', 1, 1, 50, 40, '', '');
MERGE INTO tbl_vm_flavor KEY(id) VALUES('96f9c44c-4d01-4da6-84dd-9f3564fe2bdb', 'General Computing-2', 'Ordinary APP', 'X86', 4, 8, 50, 100, '', '');
-- ----------------------------
-- Records of tbl_vm_image
-- ----------------------------
MERGE INTO tbl_vm_image KEY(id) VALUES(1, 'Ubuntu18.04', 'public', 'ubuntu', '18.04', '64', 40, 'Ubuntu18.04.qcow2', 'qcow2', null, null, 1024000, null, 'UPLOAD_WAIT', '2020-03-18 16:19:36.127+08', '2020-03-18 16:19:36.127+08', null, '39937079-99fe-4cd8-881f-04ca8c4fe09d', 'admin', null, null);
MERGE INTO tbl_vm_image KEY(id) VALUES(2, 'Ubuntu16.04', 'public', 'ubuntu', '16.04', '64', 40, 'Ubuntu16.04.qcow2', 'qcow2', 'test', null, 1024000, null, 'UPLOAD_WAIT', '2020-03-18 16:19:36.127+08', '2020-03-18 16:19:36.127+08', null, '39937079-99fe-4cd8-881f-04ca8c4fe09d', 'admin', null, null);
MERGE INTO tbl_vm_image KEY(id) VALUES(3, 'Ubuntu14.04', 'public', 'ubuntu', '14.04', '64', 40, 'Ubuntu14.04.qcow2', 'qcow2', 'url', null, 1024000, null, 'UPLOAD_WAIT', '2020-03-18 16:19:36.127+08', '2020-03-18 16:19:36.127+08', null, '39937079-99fe-4cd8-881f-04ca8c4fe09d', 'admin', null, null);
MERGE INTO tbl_vm_image KEY(id) VALUES(4, 'Ubuntu12.04', 'public', 'ubuntu', '12.04', '64', 40, 'Ubuntu12.04.qcow2', 'qcow2', null, null, 1024000, null, 'UPLOAD_WAIT', '2020-03-18 16:19:36.127+08', '2020-03-18 16:19:36.127+08', null, 'd1bb89fe-1a9d-42e9-911e-7b038c3480b9', 'admin', null, null);
MERGE INTO tbl_vm_image KEY(id) VALUES(5, 'Ubuntu10.04', 'public', 'ubuntu', '10.04', '64', 40, 'Ubuntu10.04.qcow2', 'qcow2', null, null, 1024000, null, 'UPLOADING_MERGING', '2020-03-18 16:19:36.127+08', '2020-03-18 16:19:36.127+08', null, 'd1bb89fe-1a9d-42e9-911e-7b038c3480b9', 'admin', null, null);

-- ----------------------------
-- Records of tbl_application
-- ----------------------------
MERGE INTO tbl_application KEY(id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d364','container-app','container app desc','v1.0','edgegallery','X86','CONTAINER','Video Application','Smart Park','db22dc00-8f44-408c-a106-402e60c643de','db22dc00-8f44-408c-a106-402e60c643df','DEVELOP','1635738228272','CREATED','b27d72b5-93a6-4db4-8268-7ec502331ade','admin',null, null);
MERGE INTO tbl_application KEY(id) VALUES('4cbbab9d-c48f-4adb-ae82-d1816d8edd7b','vm-app','vm app desc','v1.0','edgegallery','X86','VM','Video Application','Smart Park','db22dc00-8f44-408c-a106-402e60c643de','db22dc00-8f44-408c-a106-402e60c643df','DEVELOP','1635738228272','CREATED','b27d72b5-93a6-4db4-8268-7ec502331ade','admin','fe934a92-1cfc-42fe-919d-422e2e3bd1f8', null);
MERGE INTO tbl_application KEY(id) VALUES('4cbbab9d-c48f-4adb-ae82-d1816d8edd7c','vm-app-for-test','vm app desc','v1.0','edgegallery','X86','VM','Video Application','Smart Park','db22dc00-8f44-408c-a106-402e60c643de','db22dc00-8f44-408c-a106-402e60c643df','DEVELOP','1635738228272','CREATED','b27d72b5-93a6-4db4-8268-7ec502331ade','admin','fe934a92-1cfc-42fe-919d-422e2e3bd1f8', null);
MERGE INTO tbl_application KEY(id) VALUES('3f11715f-b59e-4c23-965b-b7f9c34c20d1','vm-app-01','vm app desc','v2.0','edgegallery','X86','VM','Video Application','Smart Park','db22dc00-8f44-408c-a106-402e60c643de','db22dc00-8f44-408c-a106-402e60c643df','DEVELOP','1635738228272','CREATED','b27d72b5-93a6-4db4-8268-7ec502331ade','admin',null, null);
MERGE INTO tbl_application KEY(id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d365','container-app-01','container app desc','v1.1','edgegallery','X86','CONTAINER','Video Application','Smart Park','db22dc00-8f44-408c-a106-402e60c643de','db22dc00-8f44-408c-a106-402e60c643df','DEVELOP','1635738228272','CREATED','b27d72b5-93a6-4db4-8268-7ec502331ade','admin','fe934a92-1cfc-42fe-919d-422e2e3bd1f7', null);
MERGE INTO tbl_application KEY(id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d366','container-app-02','container app desc','v1.2','edgegallery','X86','CONTAINER','Video Application','Smart Park','db22dc00-8f44-408c-a106-402e60c643de','db22dc00-8f44-408c-a106-402e60c643df','DEVELOP','1635738228272','CREATED','b27d72b5-93a6-4db4-8268-7ec502331ade','admin',null, null);
MERGE INTO tbl_application KEY(id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d367','container-app-03','container app desc','v1.3','edgegallery','X86','CONTAINER','Video Application','Smart Park','db22dc00-8f44-408c-a106-402e60c643de','db22dc00-8f44-408c-a106-402e60c643df','DEVELOP','1635738228272','CREATED','b27d72b5-93a6-4db4-8268-7ec502331ade','admin',null, null);

-- ----------------------------
-- Records of tbl_vm
-- ----------------------------
MERGE INTO tbl_vm KEY(id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d757','4cbbab9d-c48f-4adb-ae82-d1816d8edd7c','appvm1','3ef2bea0-5e23-4fab-952d-cc9e6741dbe7',1,null,'',null,'','');
MERGE INTO tbl_vm KEY(id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d758','4cbbab9d-c48f-4adb-ae82-d1816d8edd7c','appvm2','3ef2bea0-5e23-4fab-952d-cc9e6741dbe7',1,null,'',null,'','');
-- ----------------------------
-- Records of tbl_vm_port
-- ----------------------------
MERGE INTO tbl_vm_port KEY(id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d661','6a75a2bd-9811-432f-bbe8-2813aa97d757','port1','vm port 1', 'MEC_APP_Public');
MERGE INTO tbl_vm_port KEY(id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d662','6a75a2bd-9811-432f-bbe8-2813aa97d757','port2','vm port 2', 'MEC_APP_Private');

MERGE INTO tbl_vm_certificate KEY(vm_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d757','PASSWORD','{"username":"root","password":"test1234"}', null);
-- ----------------------------
-- Records of tbl_vm_instantiate_info
-- ----------------------------
MERGE INTO tbl_vm_instantiate_info KEY(vm_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d758','4cbbab9d-c48f-4adb-ae82-d1816d8edd7c', '3ef2bea0-5e23-4fab-952d-cc9e6741dbe7',null,null,'',null,'SUCCESS','',null, null);

-- ----------------------------
-- Records of tbl_vm_port_instantiate_info
-- ----------------------------
MERGE INTO tbl_vm_port_instantiate_info KEY(vm_id,network_name) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d758','MEC_APP_N6', '192.168.1.3');

-- ----------------------------
-- Records of tbl_vm_certificate
-- ----------------------------
MERGE INTO tbl_vm_certificate KEY(vm_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d758','PASSWORD', '{"username":"root","password":"root"}',null);
-- ----------------------------
-- Records of tbl_app_dns_rule
-- ----------------------------
MERGE INTO tbl_app_traffic_rule KEY(traffic_rule_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d365','e7bb85d1-a461-465a-b335-7189d1e527d4','action','1','filter_type',null,null);
MERGE INTO tbl_app_traffic_rule KEY(traffic_rule_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d366','e7bb85d1-a461-465a-b335-7189d1e527d5','action','1','filter_type',null,null);

-- ----------------------------
-- Records of tbl_app_dns_rule
-- ----------------------------
MERGE INTO tbl_app_dns_rule KEY(dns_rule_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d365','aeeb627d-a377-42bb-acb9-1f076682b205','domain','ip-type','1.1.1.1','ttl');
MERGE INTO tbl_app_dns_rule KEY(dns_rule_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d366','aeeb627d-a377-42bb-acb9-1f076682b206','domain','ip-type','1.1.1.1','ttl');

-- ----------------------------
-- Records of tbl_app_service_produced
-- ----------------------------
MERGE INTO tbl_app_service_produced KEY(app_service_produced_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d365','2e334f90-53ca-4d4c-a644-e90a44fa73c8',
'one-level','one-level-en','two-level','desc','api_file_id','guide_file_id','icon-file-id','serviceName-000',22222,'v1.0','https','admin',null,null,null);

MERGE INTO tbl_app_service_produced KEY(app_service_produced_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d366','2e334f90-53ca-4d4c-a644-e90a44fa73c9',
'one-level','one-level-en','two-level','desc','api_file_id','guide_file_id','icon-file-id','serviceName-001',22222,'v1.0','https','admin',null,null,null);

-- ----------------------------
-- Records of tbl_app_service_required
-- ----------------------------
MERGE INTO tbl_app_service_required KEY(app_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d365', '143e8608-7304-4932-9d99-4bd6b115dac8', '平台基础服务','Platform services','服务发现','service discovery','serName-test','v1.0',true,null,null);
MERGE INTO tbl_app_service_required KEY(app_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d366','143e8608-7304-4932-9d99-4bd6b115dac8', '平台基础服务','Platform services','服务发现','service discovery','serName-test-001','v1.0',true,null,null);
-- ----------------------------
-- Records of tbl_app_certificate
-- ----------------------------
MERGE INTO tbl_app_certificate KEY(app_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d365','ak','sk');
MERGE INTO tbl_app_certificate KEY(app_id) VALUES('6a75a2bd-9811-432f-bbe8-2813aa97d366','ak','sk');

-- ----------------------------
-- Records of tbl_app_package
-- ----------------------------
MERGE INTO tbl_app_package KEY(id) VALUES('f2759fcb-bb4b-42f5-bc6c-8e1635348fda','6a75a2bd-9811-432f-bbe8-2813aa97d365','a.csar','/test/a.csar');
MERGE INTO tbl_app_package KEY(id) VALUES('f2759fcb-bb4b-42f5-bc6c-8e1635348fdb','6a75a2bd-9811-432f-bbe8-2813aa97d366','b.csar','/test/b.csar');
MERGE INTO tbl_app_package KEY(id) VALUES('f2759fcb-bb4b-42f5-bc6c-8e1635348fdc','test-path','',null);
MERGE INTO tbl_app_package KEY(id) VALUES('f2759fcb-bb4b-42f5-bc6c-8e1635348fdd','4cbbab9d-c48f-4adb-ae82-d1816d8edd7b','d.csar','/test/d.csar');
-- ----------------------------

-- ----------------------------
-- Records of tbl_released_package
-- ----------------------------
MERGE INTO tbl_released_package KEY(id) VALUES('f69fc98c-51d9-4c20-b2ad-20b50359f1cb','appId','f2759fcb-bb4b-42f5-bc6c-8e1635348fda','name','v1.0','eg','smart','video','X86','desc','2021-12-14 11:50:10.456+08','userId','userName','testTaskId');
MERGE INTO tbl_released_package KEY(id) VALUES('f69fc98c-51d9-4c20-b2ad-20b50359f1ca','appId1','f2759fcb-bb4b-42f5-bc6c-8e1635348fdb','name','v1.0','eg','smart','video','X86','desc','2021-12-14 11:51:10.456+08','userId','userName','testTaskId');
MERGE INTO tbl_released_package KEY(id) VALUES('f69fc98c-51d9-4c20-b2ad-20b50359f1cc','appId2','f2759fcb-bb4b-42f5-bc6c-8e1635348fdc','name','v1.0','eg','smart','video','X86','desc','2021-12-14 11:52:10.456+08','userId','userName','testTaskId');

-- Records of tbl_mep_host
-- ----------------------------
MERGE INTO tbl_mep_host KEY(host_id) VALUES('fe934a92-1cfc-42fe-919d-422e2e3bd1f9','k8s-sandbox','localhost','http',31252,'X86','NORMAL','localhost','K8S','test','test',20000,
'5ce78873-d73d-4e7d-84a4-ab75ac95400f',null,null,null,'xian');
MERGE INTO tbl_mep_host KEY(host_id) VALUES('fe934a92-1cfc-42fe-919d-422e2e3bd1f8','open-stack-sandbox','1.1.1.3','https',30100,'X86','NORMAL','1.1.1.3','OpenStack','test','test',20000,
'5ce78873-d73d-4e7d-84a4-ab75ac95400f',null,'VDU1_APP_Plane01_IP=192.168.220.0/24;VDU1_APP_Plane03_IP=192.168.222.0/24;VDU1_APP_Plane02_IP=192.168.221.0/24',null,'xian');
MERGE INTO tbl_mep_host KEY(host_id) VALUES('fe934a92-1cfc-42fe-919d-422e2e3bd1f7','k8s-sandbox','1.1.1.1','https',30100,'X86','NORMAL','1.1.1.1','K8S','test','test',20000,
'5ce78873-d73d-4e7d-84a4-ab75ac95400f',null,null,null,'xian');
-- ----------------------------
-- Records of tbl_plugin
-- ----------------------------
MERGE INTO tbl_plugin KEY(pluginid) VALUES ('586224da-e1a2-4893-a5b5-bf766fdfb8c7', 'test001', 'test 0318', 4, 'JAVA', 1, '1.0.0', 1, 'testdata/idea.png', 'testdata/IDEAPluginDev.zip', 'f24ea0a2-d8e6-467c-8039-94f0d29bac43', '2020-03-18 09:18:05.188+08', 'helongfei999', 3440123, 'testdata/plugin.json', 1, null);
MERGE INTO tbl_plugin KEY(pluginid) VALUES ('586224da-e1a2-4893-a5b5-bf766fdfb8c8', 'test001', 'test 0318', 4, 'JAVA', 1, '1.0.0', 1, 'testdata/idea.png', 'testdata/IDEAPluginDev.zip', 'f24ea0a2-d8e6-467c-8039-94f0d29bac43', '2020-03-18 09:18:05.188+08', 'helongfei999', 3440123, 'testdata/plugin.json', 1, null);
-- ----------------------------
-- Records of tbl_uploaded_file 
-- ----------------------------
MERGE INTO tbl_uploaded_file KEY(file_id) VALUES ('ad66d1b6-5d29-487b-9769-be48b62aec2e', '1584500561029.png', 0, 'f24ea0a2-d8e6-467c-8039-94f0d29bac43', '2020-03-20 09:57:36.074+08', '/workspace/200dfab1-3c30-4fc7-a6ca-ed6f0620a85e/ad66d1b6-5d29-487b-9769-be48b62aec2e');
MERGE INTO tbl_uploaded_file KEY(file_id) VALUES ('ad66d1b6-5d29-487b-9769-be48b62aec2f', '1584500561029.png', true, 'f24ea0a2-d8e6-467c-8039-94f0d29bac43', '2020-03-20 09:57:36.074+08', '/workspace/200dfab1-3c30-4fc7-a6ca-ed6f0620a85e/ad66d1b6-5d29-487b-9769-be48b62aec2e');

-- ----------------------------
-- Records of tbl_host_log
-- ----------------------------
MERGE INTO tbl_host_log(log_id, host_ip, user_name, user_id, project_id, project_name, app_instances_id, deploy_time, status, operation, host_id) KEY(log_id) VALUES ('2223', '192.168.10.100', 'dong', '333211', '33333', 'sa', 'w222', '30204', 'NORMAL', '使用/释放', 'd6bcf665-ba9c-4474-b7fb-25ff859563d3');

-- ----------------------------
-- Records of tbl_atp_test_task
-- ----------------------------
MERGE INTO tbl_atp_test_task KEY(id) VALUES('6a75a2bd-1111-432f-bbe8-2813aa97d365','6a75a2bd-9811-432f-bbe8-2813aa97d365','atpName', 'success', '');