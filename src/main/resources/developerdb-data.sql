-- workspace vm resources init --

    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (1, 'X86', '通用计算型-2', 'General Computing-1', '普通APP', 'Ordinary APP', 1, 1, 50, 40, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (2, 'X86', '通用计算型-2', 'General Computing-2', '普通APP', 'Ordinary APP', 8, 4, 50, 100, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (3, 'X86', '通用计算型-4', 'General Computing-4', '普通APP', 'Ordinary APP', 16, 4, 50, 100, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (4, 'X86', '通用计算增强型-2', 'General Computing Enhancement-2', '普通APP', 'Ordinary APP', 16, 8, 50, 200, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (5, 'X86', '通用计算增强型-4', 'General Computing Enhancement-4', '普通APP', 'Ordinary APP', 32, 8, 50, 200, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (6, 'X86', '高I/O型-2', 'High I/O-2', '高I/O型APP', 'High I/O-APP', 32, 4, 50, 100, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (7, 'X86', '高I/O型-4', 'High I/O-4', '高I/O型APP', 'High I/O-APP', 64, 8, 50, 100, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (8, 'X86', '大存储型', 'Large Storage', 'IoT数据采集', 'IoT Data Collection', 8, 4, 50, 1000, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (9, 'X86', 'AI推理型-2', 'AI', '工业视觉、园区监控', 'Industrial Vision, Park Monitoring', 8, 4, 50, 200, '', '1*Atlas300C(16G)')
    ON CONFLICT(regulation_id) do nothing;

    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (10, 'ARM', '通用计算型-2', 'General Computing-2', '普通APP', 'Ordinary APP', 8, 4, 50, 100, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (11, 'ARM', '通用计算型-4', 'General Computing-4', '普通APP', 'Ordinary APP', 16, 4, 50, 100, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (12, 'ARM', '通用计算增强型-2', 'General Computing Enhancement-2', '普通APP', 'Ordinary APP', 16, 8, 50, 200, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (13, 'ARM', '通用计算增强型-4', 'General Computing Enhancement-4', '普通APP', 'Ordinary APP', 32, 8, 50, 200, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (14, 'ARM', '高I/O型2', 'High I/O-2', '高I/O型APP', 'High I/O-APP', 32, 4, 50, 100, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (15, 'ARM', '高I/O型4', 'High I/O-4', '高I/O型APP', 'High I/O-APP', 64, 8, 50, 100, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (16, 'ARM', '大存储型', 'Large Storage', 'IoT数据采集', 'IoT Data Collection', 8, 4, 50, 1000, '', '')
    ON CONFLICT(regulation_id) do nothing;
    INSERT INTO tbl_vm_regulation (regulation_id, architecture, name_zh, name_en, scene_zh, scene_en, memory, cpu, system_disk, data_disk, gpu, other_ability) VALUES
    (17, 'ARM', 'AI推理型2', 'AI', '工业视觉、园区监控', 'Industrial Vision, Park Monitoring', 8, 4, 50, 200, '', '1*Atlas300C(16G)')
    ON CONFLICT(regulation_id) do nothing;

    INSERT INTO tbl_vm_network (network_type, description_zh, description_en, network_name) VALUES
    ('Network_N6', 'N6网络，端侧设备在访问边缘应用时，需要通过该网络进行访问', 'N6 network, when end-side devices access edge applications, they need to access through this network', 'mec_network_n6')
    ON CONFLICT(network_type) do nothing;
    INSERT INTO tbl_vm_network (network_type, description_zh, description_en, network_name)
    VALUES ('Network_MEP', '与边缘计算平台之间的网络，当应用存在服务依赖或需要发布服务时，需要该网络', 'The network with the edge computing platform, when the application has service dependency or needs to publish the service, the network is needed', 'mec_network_mep')
    ON CONFLICT(network_type) do nothing;
    INSERT INTO tbl_vm_network (network_type, description_zh, description_en, network_name) VALUES
    ('Network_Internet', 'Internet网络', 'Internet Network', 'mec_network_internet')
    ON CONFLICT(network_type) do nothing;

    INSERT INTO tbl_vm_flavor (architecture, flavor, constraints) VALUES
    ('X86', 'mgmt_egx86', 'nova')
    ON CONFLICT(architecture) do nothing;
    INSERT INTO tbl_vm_flavor (architecture, flavor, constraints) VALUES
    ('ARM', 'mgmt_egarm', 'nova')
    ON CONFLICT(architecture) do nothing;

    INSERT INTO tbl_vm_user_data (operate_system, is_temp, contents, params) VALUES
    ('ubuntu', 'true', '','')
    ON CONFLICT(operate_system) do nothing;
    INSERT INTO tbl_vm_user_data (operate_system, is_temp, contents, params) VALUES
    ('centos', 'true', '','')
    ON CONFLICT(operate_system) do nothing;
    INSERT INTO tbl_vm_user_data (operate_system, is_temp, contents, params) VALUES
    ('debian', 'true', '','')
    ON CONFLICT(operate_system) do nothing;

    -- workspace mep capability init --


    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('c0db376b-ae50-48fc-b9f7-58a609e3ee12', '平台基础服务', 'Platform services', '服务治理', 'Service governance', 'OPENMEP', 'EdgeGallery平台为APP提供服务注册、发现、订阅等相关功能。', 'The EdgeGallery platform provides APP with related functions such as service registration, discovery, and subscription.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('143e8608-7304-4932-9d99-4bd6b115dac8', '服务发现', 'service discovery', 'v1', '为APP提供服务注册、发现、订阅等相关功能', 'The EdgeGallery platform provides APP with related functions such as service registration, discovery, and subscription.', 'Huawei', 'c0db376b-ae50-48fc-b9f7-58a609e3ee12', '540e0817-f6ea-42e5-8c5b-cb2daf9925a3', '9bb4a85f-e985-47e1-99a4-20c03a486864', '9ace2dfc-6548-4511-96f3-2f622736e18a', 8684, 'service-discovery', 'http', '2020-11-20 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('540e0817-f6ea-42e5-8c5b-cb2daf9925a3', 'Service Discovery.json', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/540e0817-f6ea-42e5-8c5b-cb2daf9925a3')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9bb4a85f-e985-47e1-99a4-20c03a486864', 'Service Discovery.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/9bb4a85f-e985-47e1-99a4-20c03a486864')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-2f622736e18a', 'Service Discovery_en.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/9ace2dfc-6548-4511-96f3-2f622736e18a')
    ON CONFLICT(file_id) do nothing;

    -- telecom network capability init

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('a6efaa2c-ad99-432f-9405-e28e90f44f15', '电信网络能力', 'Telecom network', '带宽管理', 'Bandwidth management', 'OPENMEP', 'EdgeGallery平台为APP提供网络宽带业务相关的功能。', 'The EdgeGallery platform provides APP with functions related to network broadband services.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('406593b4-c782-409c-8f46-a6fd5e1f6221', '电信网络能力', 'Telecom network', '位置服务', 'Location service', 'OPENMEP', '为APP提供定位服务，包括接入点信息，指定用户的位置信息等。', 'Provide location services for APP, including access point information, location information of designated users, etc.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('d8f06d28-390c-4a06-905e-120f56279bbc', '电信网络能力', 'Telecom network', '流量规则', 'Traffic Rule', 'OPENMEP', '为MEC应用APP配置和修改流量规则信息。', 'Configure and modify traffic rule information for MEC application APP.')
    ON CONFLICT(group_id) do nothing;

    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('ee7fbc17-f370-4c02-a9ab-680a41cd0255', '带宽管理', 'Bandwidth management', 'v1', '为APP提供网络宽带业务相关的功能', 'The EdgeGallery platform provides APP with functions related to network broadband services.', 'Huawei', 'a6efaa2c-ad99-432f-9405-e28e90f44f15', '7c544903-aa4f-40e0-bd8c-cf6e17c37c12', '6736ec41-eb7e-4dca-bda2-3b4e10d0a294', '16dd231c-70dd-4187-a89b-2eb4db79264f', 8489, 'bandwidth-service', 'http', '2020-11-20 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('7c544903-aa4f-40e0-bd8c-cf6e17c37c12', 'Bandwidth service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/7c544903-aa4f-40e0-bd8c-cf6e17c37c12')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('6736ec41-eb7e-4dca-bda2-3b4e10d0a294', 'Bandwidth service.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/6736ec41-eb7e-4dca-bda2-3b4e10d0a294')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('16dd231c-70dd-4187-a89b-2eb4db79264f', 'Bandwidth service_en.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/16dd231c-70dd-4187-a89b-2eb4db79264f')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('146f4f87-4027-4ad8-af99-ec4a6f6bcc3c', '位置服务', 'Location service', 'v1', '为APP提供定位服务，包括接入点信息，指定用户的位置信息等', 'Provide location services for APP, including access point information, location information of designated users, etc.', 'Huawei', '406593b4-c782-409c-8f46-a6fd5e1f6221', '688f259e-48eb-407d-8604-7feb19cf1f44', 'b0819798-e932-415c-95f5-dead04ef2fba', '3fe31309-9e28-40f7-a593-a87e9a73ba5e', 8487, 'location-service', 'http', '2020-11-20 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('688f259e-48eb-407d-8604-7feb19cf1f44', 'Location service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/688f259e-48eb-407d-8604-7feb19cf1f44')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('b0819798-e932-415c-95f5-dead04ef2fba', 'Location service.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/b0819798-e932-415c-95f5-dead04ef2fba')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('3fe31309-9e28-40f7-a593-a87e9a73ba5e', 'Location service_en.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/3fe31309-9e28-40f7-a593-a87e9a73ba5e')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('3fda958c-ef56-44c9-bf3b-469cf5d54e33', '流量规则', 'Traffic Rule', 'v1', '为MEC应用APP配置和修改流量规则信息。', 'Configure and modify traffic rule information for MEC application APP.', 'Huawei', 'd8f06d28-390c-4a06-905e-120f56279bbc', '9f1f13a0-8554-4dfa-90a7-d2765238fca7', '5110740f-305c-4553-920e-2b11cd9f64c1', '25689270-5d31-4f5f-9edd-f81a83cb4844', 8456, 'traffice-service', 'http', '2020-11-20 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9f1f13a0-8554-4dfa-90a7-d2765238fca7', 'Traffic service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/9f1f13a0-8554-4dfa-90a7-d2765238fca7')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('5110740f-305c-4553-920e-2b11cd9f64c1', 'Traffic service.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/5110740f-305c-4553-920e-2b11cd9f64c1')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('25689270-5d31-4f5f-9edd-f81a83cb4844', 'Traffic service_en.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/25689270-5d31-4f5f-9edd-f81a83cb4844')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('fc4d2874-9876-44eb-be35-f6e7056a88fd', '电信网络能力', 'Telecom network', '上行压缩', 'Uplink Compression', 'OPENMEP', '为图像分析APP提供高质量图像数据。', 'Provide high-quality image data for image analysis APP.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('048bea62-30af-4443-a9c4-ff69319c741f', '上行压缩', 'Uplink Compression', 'v1', '为图像分析APP提供高质量图像数据。', 'Provide high-quality image data for image analysis APP.', 'Huawei', 'fc4d2874-9876-44eb-be35-f6e7056a88fd', '71f887cf-b316-4ffe-800f-b680b4006107', 'ffb4c1dd-cb29-4bc7-aed1-b3cff8aa7891', 'fd0b4541-422d-4ece-b4e9-5c71da6a4363', 0, 'uplink-compression', 'http', '2020-11-20 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('71f887cf-b316-4ffe-800f-b680b4006107', 'UpstreamCompressionDoc.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/UpstreamCompressionDoc.md')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('ffb4c1dd-cb29-4bc7-aed1-b3cff8aa7891', 'UpstreamCompressionDoc.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/UpstreamCompressionDoc.md')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('fd0b4541-422d-4ece-b4e9-5c71da6a4363', 'UpstreamCompressionDoc.json', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/UpstreamCompressionDoc.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('eab41d0f-63ee-4200-89c2-d4de463793ff', '电信网络能力', 'Telecom network', '拥塞检测', 'Congestion Detection', 'OPENMEP', 'EdgeGallery平台为APP提供了实时拥塞调度，达到最佳业务体验。', 'The EdgeGallery platform provides real-time congestion scheduling for APP to achieve the best business experience.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('e0eacedb-e94f-4f13-a616-e8a58335f017', '拥塞检测', 'Congestion Detection', 'v1', 'EdgeGallery平台为APP提供了实时拥塞调度，达到最佳业务体验。', 'The EdgeGallery platform provides real-time congestion scheduling for APP to achieve the best business experience.', 'Huawei', 'eab41d0f-63ee-4200-89c2-d4de463793ff', '67f709a0-e269-465a-a540-c7a8f4c685d8', 'e2d758e5-4454-4645-ba3a-7aeecfe3f827', '335c21ce-7768-44ee-8627-bf0af40e1a25', 0, 'congestion-detection', 'http', '2020-11-20 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('67f709a0-e269-465a-a540-c7a8f4c685d8', 'CongestionDetectionDoc.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/CongestionDetectionDoc.md')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('e2d758e5-4454-4645-ba3a-7aeecfe3f827', 'CongestionDetectionDoc.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/CongestionDetectionDoc.md')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('335c21ce-7768-44ee-8627-bf0af40e1a25', 'CongestionDetectionDoc.json', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/CongestionDetectionDoc.json')
    ON CONFLICT(file_id) do nothing;

    -- workspace shengteng capability init --

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('c0db376b-ae50-48fc-b9f7-58a609e3ee13', '昇腾AI能力', 'Ascend AI', 'AI图像修复', 'AI Image Repair', 'OPENMEP', 'AI图像修复技术，可以快速帮助你去除照片中的瑕疵，你的照片你做主，一切问题AI帮你搞定。', 'AI image repair technology can quickly help you remove the blemishes in your photos. Your photos are up to you, and AI will help you solve all problems.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('143e8608-7304-4932-9d99-4bd6b115dac9', 'AI图像修复', 'AI Image Repair', 'v1', '', '', 'Huawei', 'c0db376b-ae50-48fc-b9f7-58a609e3ee13', '9ace2dfc-6548-4511-96f3-1f622736e182', '9ace2dfc-6548-4511-96f3-2f622736e181', '9ace2dfc-6548-4511-96f3-2f622736e181', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-2f622736e181', 'AIImageRepair.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/AIImageRepair.md')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-1f622736e182', 'AIImage Repair.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/AIImage Repair.yaml')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('c0db376b-ae50-48fc-b9f7-58a609e3ee14', '昇腾AI能力', 'Ascend AI', '边缘检测', 'Edge Detection', 'OPENMEP', 'Edgegallery集成昇腾AI边缘检测能力，使用RCF模型对输入图像执行边缘检测。', 'Edgegallery integrates AI edge detection capabilities，Use the RCF model to perform edge detection on the input image.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('143e8608-7304-4932-9d99-4bd6b115daa0', '边缘检测', 'Edge Detection', 'v1', '', '', 'Huawei', 'c0db376b-ae50-48fc-b9f7-58a609e3ee14', '9ace2dfc-6548-4511-96f3-3f622736e182', '9ace2dfc-6548-4511-96f3-2f622736e182', '9ace2dfc-6548-4511-96f3-2f622736e182', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-2f622736e182', 'EdgeDetection.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/EdgeDetection.md')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-3f622736e182', 'EdgeDetection.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/EdgeDetection.yaml')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('c0db376b-ae50-48fc-b9f7-58a609e3ee15', '昇腾AI能力', 'Ascend AI', '卡通图像生成', 'Image Cartoonization', 'OPENMEP', '人工智能带来的便捷的可能，现实世界的景色人物都可以一键定格为卡通风格.', 'With the convenient possibilities brought by artificial intelligence, the scenery and characters in the real world can be frozen into cartoon style with one click.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('143e8608-7304-4932-9d99-4bd6b115daa1', '卡通图像生成', 'Image Cartoonization', 'v1', '', '', 'Huawei', 'c0db376b-ae50-48fc-b9f7-58a609e3ee15', '9ace2dfc-6548-4511-96f3-4f622736e183', '9ace2dfc-6548-4511-96f3-2f622736e183', '9ace2dfc-6548-4511-96f3-2f622736e183', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-2f622736e183', 'ImageCartoonization.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/ImageCartoonization.md')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-4f622736e183', 'ImageCartoonization.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/ImageCartoonization.yaml')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('c0db376b-ae50-48fc-b9f7-58a609e3ee16', '昇腾AI能力', 'Ascend AI', '图像上色', 'Image Coloring', 'OPENMEP', '让我们走进AI，使用黑白图像上色模型对黑白照片进行着色，看一看曾经世界的颜色吧！', 'Let walk into AI, use the black and white image coloring model to color black and white photos, and take a look at the colors of the past world!')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('143e8608-7304-4932-9d99-4bd6b115daa2', '图像上色', 'Image Coloring', 'v1', '', '', 'Huawei', 'c0db376b-ae50-48fc-b9f7-58a609e3ee16', '9ace2dfc-6548-4511-96f3-5f622736e184', '9ace2dfc-6548-4511-96f3-2f622736e184', '9ace2dfc-6548-4511-96f3-2f622736e184', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-2f622736e184', 'ImageColoring.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/ImageColoring.md')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-5f622736e184', 'ImageColoring.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/ImageColoring.yaml')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('c0db376b-ae50-48fc-b9f7-58a609e3ee17', '昇腾AI能力', 'Ascend AI', '对象分类', 'Object Classification', 'OPENMEP', '通过读取本地图像数据作为输入，对图像中的物体进行识别分类，并将分类的结果展示出来。', 'By reading local image data as input, the objects in the image are identified and classified, and the results of the classification are displayed.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('143e8608-7304-4932-9d99-4bd6b115daa3', '对象分类', 'Object Classification', 'v1', '', '', 'Huawei', 'c0db376b-ae50-48fc-b9f7-58a609e3ee17', '9ace2dfc-6548-4511-96f3-6f622736e185', '9ace2dfc-6548-4511-96f3-2f622736e185', '9ace2dfc-6548-4511-96f3-2f622736e185', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-2f622736e185', 'ObjectClassification.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/ObjectClassification.md')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-6f622736e185', 'ObjectClassification.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/ObjectClassification.yaml')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('c0db376b-ae50-48fc-b9f7-58a609e3ee18', '昇腾AI能力', 'Ascend AI', '目标检测', 'Object Detection', 'OPENMEP', '目标检测（Object Detection）是计算机视觉领域的基本任务之一，学术界已有将近二十年的研究历史。', 'Object Detection is one of the basic tasks in the field of computer vision, and the academic field has a research history of nearly two decades.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('143e8608-7304-4932-9d99-4bd6b115daa4', '目标检测', 'Object Detection', 'v1', '', '', 'Huawei', 'c0db376b-ae50-48fc-b9f7-58a609e3ee18', '9ace2dfc-6548-4511-96f3-7f622736e186', '9ace2dfc-6548-4511-96f3-2f622736e186', '9ace2dfc-6548-4511-96f3-2f622736e186', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-2f622736e186', 'ObjectDetection.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/ObjectDetection.md')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('9ace2dfc-6548-4511-96f3-7f622736e186', 'ObjectDetection.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/ObjectDetection.yaml')
    ON CONFLICT(file_id) do nothing;


    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('b8ddc4c4-07ca-4b49-a3dd-c018f120bff9', '昇腾AI能力', 'Ascend AI', '模糊图像变清晰', 'Blur2Sharp', 'OPENMEP', '模糊图像变清晰。', 'Blur2Sharp.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('0610e038-f956-458b-98fc-ef49bcefd79g', '模糊图像变清晰', 'Blur2Sharp', 'v1', '', '', 'Huawei', 'b8ddc4c4-07ca-4b49-a3dd-c018f120bff9', '84acf7bb-3e78-489a-90dc-95784b2dae0e', 'bd4346ba-e4c3-4c51-82df-beacf6481b59', 'bd4346ba-e4c3-4c51-82df-beacf6481b59', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('84acf7bb-3e78-489a-90dc-95784b2dae0e', 'Blur2Sharp.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/Blur2Sharp.yaml')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('bd4346ba-e4c3-4c51-82df-beacf6481b59', 'Blur2SharpImage.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/Blur2SharpImage.md')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('b8ddc4c4-07ca-4b49-a3dd-c018f120bff1', '昇腾AI能力', 'Ascend AI', '图像剪裁', 'Crop An Image', 'OPENMEP', '图像剪裁。', 'Crop An Image.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('0610e038-f956-458b-98fc-ef49bcefd791', '图像剪裁', 'Crop An Image', 'v1', '', '', 'Huawei', 'b8ddc4c4-07ca-4b49-a3dd-c018f120bff1', '84acf7bb-3e78-489a-90dc-95784b2dae01', 'bd4346ba-e4c3-4c51-82df-beacf6481b51', 'bd4346ba-e4c3-4c51-82df-beacf6481b51', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('84acf7bb-3e78-489a-90dc-95784b2dae01', 'CropAnImage.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/CropAnImage.yaml')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('bd4346ba-e4c3-4c51-82df-beacf6481b51', 'CropAnImage.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/CropAnImage.md')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('b8ddc4c4-07ca-4b49-a3dd-c018f120bff3', '昇腾AI能力', 'Ascend AI', '图像除雾', 'Dehaze an Image', 'OPENMEP', '图像除雾。', 'Dehaze an Image.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('0610e038-f956-458b-98fc-ef49bcefd793', '图像除雾', 'Dehaze an Image', 'v1', '', '', 'Huawei', 'b8ddc4c4-07ca-4b49-a3dd-c018f120bff3', '84acf7bb-3e78-489a-90dc-95784b2dae03', 'bd4346ba-e4c3-4c51-82df-beacf6481b53', 'bd4346ba-e4c3-4c51-82df-beacf6481b53', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('84acf7bb-3e78-489a-90dc-95784b2dae03', 'DehazePicture.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/DehazePicture.yaml')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('bd4346ba-e4c3-4c51-82df-beacf6481b53', 'DehazePicture.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/DehazePicture.md')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('b8ddc4c4-07ca-4b49-a3dd-c018f120bff4', '昇腾AI能力', 'Ascend AI', '垃圾识别', 'Garbage Detection', 'OPENMEP', '垃圾识别。', 'Garbage Detection.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('0610e038-f956-458b-98fc-ef49bcefd794', '垃圾识别', 'Garbage Detection', 'v1', '', '', 'Huawei', 'b8ddc4c4-07ca-4b49-a3dd-c018f120bff4', '84acf7bb-3e78-489a-90dc-95784b2dae04', 'bd4346ba-e4c3-4c51-82df-beacf6481b54', 'bd4346ba-e4c3-4c51-82df-beacf6481b54', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('84acf7bb-3e78-489a-90dc-95784b2dae04', 'GarbageDetection.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/GarbageDetection.yaml')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('bd4346ba-e4c3-4c51-82df-beacf6481b54', 'GarbageDetection.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/GarbageDetection.md')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('b8ddc4c4-07ca-4b49-a3dd-c018f120bff5', '昇腾AI能力', 'Ascend AI', '口罩检测', 'MaskDetection', 'OPENMEP', '口罩检测。', 'MaskDetection.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('0610e038-f956-458b-98fc-ef49bcefd795', '口罩检测', 'MaskDetection', 'v1', '', '', 'Huawei', 'b8ddc4c4-07ca-4b49-a3dd-c018f120bff5', '84acf7bb-3e78-489a-90dc-95784b2dae05', 'bd4346ba-e4c3-4c51-82df-beacf6481b55', 'bd4346ba-e4c3-4c51-82df-beacf6481b55', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('84acf7bb-3e78-489a-90dc-95784b2dae05', 'MaskDetectionPicture.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/MaskDetectionPicture.yaml')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('bd4346ba-e4c3-4c51-82df-beacf6481b55', 'MaskDetectionPicture.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/MaskDetectionPicture.md')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('b8ddc4c4-07ca-4b49-a3dd-c018f120bff6', '昇腾AI能力', 'Ascend AI', '背景替换', 'Portrait Picture', 'OPENMEP', '背景替换。', 'Portrait Picture.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('0610e038-f956-458b-98fc-ef49bcefd796', '背景替换', 'Portrait Picture', 'v1', '', '', 'Huawei', 'b8ddc4c4-07ca-4b49-a3dd-c018f120bff6', '84acf7bb-3e78-489a-90dc-95784b2dae06', 'bd4346ba-e4c3-4c51-82df-beacf6481b56', 'bd4346ba-e4c3-4c51-82df-beacf6481b56', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('84acf7bb-3e78-489a-90dc-95784b2dae06', 'PortraitPicture.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/PortraitPicture.yaml')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('bd4346ba-e4c3-4c51-82df-beacf6481b56', 'PortraitPicture.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/PortraitPicture.md')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('b8ddc4c4-07ca-4b49-a3dd-c018f120bff7', '昇腾AI能力', 'Ascend AI', '尺寸调整', 'ResizeAnImage', 'OPENMEP', '尺寸调整。', 'ResizeAnImage.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('0610e038-f956-458b-98fc-ef49bcefd797', '尺寸调整', 'ResizeAnImage', 'v1', '', '', 'Huawei', 'b8ddc4c4-07ca-4b49-a3dd-c018f120bff7', '84acf7bb-3e78-489a-90dc-95784b2dae07', 'bd4346ba-e4c3-4c51-82df-beacf6481b57', 'bd4346ba-e4c3-4c51-82df-beacf6481b57', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('84acf7bb-3e78-489a-90dc-95784b2dae07', 'ResizeAnImage.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/ResizeAnImage.yaml')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('bd4346ba-e4c3-4c51-82df-beacf6481b57', 'ResizeAnImage.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/ResizeAnImage.md')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('b8ddc4c4-07ca-4b49-a3dd-c018f120bff8', '昇腾AI能力', 'Ascend AI', '超分辨率图像处理', 'Super Resolution', 'OPENMEP', '超分辨率图像处理。', 'Super Resolution.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('0610e038-f956-458b-98fc-ef49bcefd798', '超分辨率图像处理', 'Super Resolution', 'v1', '', '', 'Huawei', 'b8ddc4c4-07ca-4b49-a3dd-c018f120bff8', '84acf7bb-3e78-489a-90dc-95784b2dae08', 'bd4346ba-e4c3-4c51-82df-beacf6481b58', 'bd4346ba-e4c3-4c51-82df-beacf6481b58', 0, '', 'http', '2021-3-13 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('84acf7bb-3e78-489a-90dc-95784b2dae08', 'SuperResolution.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/SuperResolution.yaml')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('bd4346ba-e4c3-4c51-82df-beacf6481b58', 'SuperResolution.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/SuperResolution.md')
    ON CONFLICT(file_id) do nothing;


    -- workspace AI capability init --

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('ab88bc3a-e1c0-4d0d-a4e5-242902f39b12', 'AI能力', 'AI capabilities', '人脸识别', 'face recognition', 'OPENMEP', '上传照片进行人脸识别，返回待识别人脸所在的位置。', 'Upload photos for face recognition, and return to the location of the face to be recognized.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('6f250fc0-0961-470f-bf17-e9bba8e56c12','人脸识别服务', 'face recognition service', 'v1', '上传照片进行人脸识别，返回待识别人脸所在的位置', 'Upload photos for face recognition, and return to the location of the face to be recognized.', 'Huawei', 'ab88bc3a-e1c0-4d0d-a4e5-242902f39b12', 'd0f8fa57-2f4c-4182-be33-0a508964d04a', '10d8a909-742a-433f-8f7a-5c7667adf825', '58f39f8a-332d-4e11-8421-bd4e11769d86', 9997, 'face-recognition', 'http', '2021-02-05 11:50:28', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('d0f8fa57-2f4c-4182-be33-0a508964d04a', 'Face Recognition service.json', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/d0f8fa57-2f4c-4182-be33-0a508964d04a')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('10d8a909-742a-433f-8f7a-5c7667adf825', 'Face Recognition service.md', false, 'admin', '2021-02-05 11:50:28', '/mep_capability/10d8a909-742a-433f-8f7a-5c7667adf825')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('58f39f8a-332d-4e11-8421-bd4e11769d86', 'Face Recognition service_en.md', false, 'admin', '2021-02-05 11:50:28', '/mep_capability/58f39f8a-332d-4e11-8421-bd4e11769d86')
    ON CONFLICT(file_id) do nothing;


    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('4dba5f43-d802-4ec2-89c5-a2b5d3ffa5fd', '视频处理',  'Video processing', '无损解压缩', 'Lossless compression', 'OPENMEP', '无损解压缩服务提供端侧与边缘之间的无损解压缩服务，降低对传输带宽的诉求。', ' Provides a lossless decompression service between the end and the edge, reducing the demand for transmission bandwidth.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('420bf209-b74a-4c37-99f6-6bcca97b0ee3', '无损解压缩', 'Lossless compression', 'v1', '无损解压缩服务提供端侧与边缘之间的无损解压缩服务，降低对传输带宽的诉求', ' Provides a lossless decompression service between the end and the edge, reducing the demand for transmission bandwidth.', 'Huawei', '4dba5f43-d802-4ec2-89c5-a2b5d3ffa5fd', '16532bf6-35cc-42e7-a77f-cbf6020c3667', 'b0b0f727-9964-48bc-9b04-2ed039c58d33', '5275d443-2200-4901-98da-7ec3e66db5e1', 8425, 'natural-language-processing', 'http', '2020-11-20 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('b0b0f727-9964-48bc-9b04-2ed039c58d33', 'lossless-decompression.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/b0b0f727-9964-48bc-9b04-2ed039c58d33')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('16532bf6-35cc-42e7-a77f-cbf6020c3667', 'lossless-decompression.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/16532bf6-35cc-42e7-a77f-cbf6020c3667')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('5275d443-2200-4901-98da-7ec3e66db5e1', 'lossless-decompression_en.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/5275d443-2200-4901-98da-7ec3e66db5e1')
    ON CONFLICT(file_id) do nothing;


    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('293bebd8-a226-4df6-adff-2d4bed7b08d3', '数据库', 'DateBase', '数据管理', 'Data management', 'OPENMEP', '分布式键值(key-value)数据库，采用键值对(key-value)方式存储数据。', 'A distributed key-value database uses key-value pairs to store data.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('7e00b0ed-9168-46be-a411-a786739be4d2', '分布式键值(key-value)数据库', 'Distributed key-value database', 'v1', '分布式键值(key-value)数据库，采用键值对(key-value)方式存储数据。', 'A distributed key-value database uses key-value pairs to store data.', 'Huawei', '293bebd8-a226-4df6-adff-2d4bed7b08d3', 'da823a31-f3c9-44f4-92d9-24bb2cded86c', '35249ffa-4998-4492-8440-fc9a52fd2fc7', 'e5f62364-fc24-4ad7-a8c7-ad838d400e08', 8425, 'key-value-database', 'http', '2020-11-20 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('35249ffa-4998-4492-8440-fc9a52fd2fc7', 'distributed-database.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/35249ffa-4998-4492-8440-fc9a52fd2fc7')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('da823a31-f3c9-44f4-92d9-24bb2cded86c', 'distributed-database.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/da823a31-f3c9-44f4-92d9-24bb2cded86c')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('e5f62364-fc24-4ad7-a8c7-ad838d400e08', 'distributed-database_en.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/e5f62364-fc24-4ad7-a8c7-ad838d400e08')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('ef6f8292-49e8-4b44-90c1-8418a5e63866', 'AI能力', 'AI capabilities',  '轻量级中文分词器', 'Lightweight chinese word segmenter', 'OPENMEP', '基于mmseg算法的轻量级中文分词器，集成了关键字提取，关键句子提取和文章自动摘要等功能。', 'Based on mmseg algorithm, which integrates functions such as keyword extraction, key sentence extraction and automatic article summarization.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('27246b7e-ac4d-4613-9548-2607a25cb794', '轻量级中文分词器', 'Lightweight chinese word segmenter', 'v1', '基于mmseg算法的轻量级中文分词器，集成了关键字提取，关键句子提取和文章自动摘要等功能。', 'Based on mmseg algorithm, which integrates functions such as keyword extraction, key sentence extraction and automatic article summarization.', 'Huawei', 'ef6f8292-49e8-4b44-90c1-8418a5e63866', 'b1f85bcc-74f4-4e78-8545-53986f0156e7', '2099cd1-e2c4-454b-9ee6-d2d54846928b', 'e51fd154-0f0a-4a30-8fa2-fd7f83677aa8', 8425, 'natural-language-processing', 'http', '2020-11-20 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('2099cd1-e2c4-454b-9ee6-d2d54846928b', 'jcseg.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/2099cd1-e2c4-454b-9ee6-d2d54846928b')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('b1f85bcc-74f4-4e78-8545-53986f0156e7', 'jcseg.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/b1f85bcc-74f4-4e78-8545-53986f0156e7')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('e51fd154-0f0a-4a30-8fa2-fd7f83677aa8', 'jcseg_en.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/e51fd154-0f0a-4a30-8fa2-fd7f83677aa8')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('4466a11e-c213-40ef-9d28-1907b63b8844', '公共框架', 'Public framework', '统一网关', 'Gateway', 'OPENMEP', '提供负载均衡、动态上游、灰度发布、服务熔断、身份认证、可观测性等丰富的流量管理功能', 'Provides rich traffic management functions such as load balancing, dynamic upstream, grayscale release, service fusing, identity authentication, observability, etc.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('b819227e-723f-48c8-b624-206b3531b9aa', '统一网关', 'Gateway', '2.1', '提供负载均衡、动态上游、灰度发布、服务熔断、身份认证、可观测性等丰富的流量管理功能', 'Provides rich traffic management functions such as load balancing, dynamic upstream, grayscale release, service fusing, identity authentication, observability, etc.', 'Huawei', '4466a11e-c213-40ef-9d28-1907b63b8844', 'a719208e-1d30-4e00-b397-148308c3f6b0', '92b875b6-d6e4-4ad8-9a7f-17ffe06e79d9', '5e951098-44af-4408-bdba-93db5fbc6928', 8421, 'natural-language-processing', 'http', '2020-11-20 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('92b875b6-d6e4-4ad8-9a7f-17ffe06e79d9', 'apisix.md', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/92b875b6-d6e4-4ad8-9a7f-17ffe06e79d9')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('a719208e-1d30-4e00-b397-148308c3f6b0', 'apisix.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/a719208e-1d30-4e00-b397-148308c3f6b0')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('5e951098-44af-4408-bdba-93db5fbc6928', 'apisix_en.yaml', false, 'admin', '2020-01-01 00:00:00.000000', '/mep_capability/5e951098-44af-4408-bdba-93db5fbc6928')
    ON CONFLICT(file_id) do nothing;

    -- workspace ETSI capability init --

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('daf15a57-ddac-4f08-bcc1-325689d44d56', 'ETSI', 'ETSI', '应用包管理 MEC 010-2', 'App Package management MEC 010-2', 'OPENMEP', ' ETSI GS MEC 010-2标准API，用于应用包管理，包括生成应用包、资源创建、订阅、通知等管理功能.', 'ETSI GS MEC 010-2 standard API, including management functions such as application package generation, resource creation, subscription, notification, etc.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('e3a48bb6-e27f-4da3-b594-be1b6a104eb1', '应用包管理 MEC 010-2', 'App Package management MEC 010-2', 'v2.1.1', 'ETSI GS MEC 010-2标准API，用于应用包管理，包括生成应用包、资源创建、订阅、通知等管理功能.', 'ETSI GS MEC 010-2 standard API, including management functions such as application package generation, resource creation, subscription, notification, etc.', 'ETSI', 'daf15a57-ddac-4f08-bcc1-325689d44d56', '6692b2fc-1ba4-46f8-8695-db87d20e0eb6', '', '', 8080, 'app-pkgm', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('6692b2fc-1ba4-46f8-8695-db87d20e0eb6', 'MEC010-2_AppPkgMgmt.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/MEC010-2_AppPkgMgmt.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('c0266e7f-85c8-43e4-a86d-6a5f7fa3037f', 'ETSI', 'ETSI', '应用生命周期管理 MEC 010-2', 'App lifecycle management MEC 010-2', 'OPENMEP', 'ETSI GS MEC 010-2标准API，用于应用管理的生命周期管理', 'ETSI GS MEC 010-2 standard API, used for App lifecycle management.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('b079f053-b648-46ce-851b-33c74216c0fd', '应用生命周期管理 MEC 010-2', 'App lifecycle management MEC 010-2', 'v2.1.1', 'ETSI GS MEC 010-2标准API，用于应用管理的生命周期管理', 'ETSI GS MEC 010-2 standard API, used for App lifecycle management.', 'ETSI', 'c0266e7f-85c8-43e4-a86d-6a5f7fa3037f', '1b1639ae-fa09-4edf-b834-93bcd141291d', '', '', 8080, 'app-lcm', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('1b1639ae-fa09-4edf-b834-93bcd141291d', 'MEC010-2_AppLcm.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/MEC010-2_AppLcm.json')
    ON CONFLICT(file_id) do nothing;
    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('24b9aa35-ec95-4827-a6ae-213d581bf9f2', 'ETSI', 'ETSI', '应用操作授权 MEC 010-2', 'Operation Granting MEC 010-2', 'OPENMEP', 'ETSI GS MEC 010-2标准API，授予特定应用程序生命周期操作', 'ETSI GS MEC 010-2 standard API,requests a grant for a particular application lifecycle operation')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('fb833a58-b5f6-4225-9184-206b0957f5a3', '应用操作授权 MEC 010-2', 'Operation Granting MEC 010-2', 'v2.1.1', 'ETSI GS MEC 010-2标准API，授予特定应用程序生命周期操作', 'ETSI GS MEC 010-2 standard API,requests a grant for a particular application lifecycle operation', 'ETSI', '24b9aa35-ec95-4827-a6ae-213d581bf9f2', '225493dc-31ce-4a1b-9e68-dee50423e0b2', '', '', 8080, 'granting', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('225493dc-31ce-4a1b-9e68-dee50423e0b2', 'MEC010-2_AppGrant.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/MEC010-2_AppGrant.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('5ad3b703-2b81-4779-98a7-b5b4e0b74983', 'ETSI', 'ETSI', '应用使能API MEC 011', 'MEC Application Support API MEC 011', 'OPENMEP', 'ETSI GSMEC 011标准API，用于应用的流量规则、DNS规则配置等', 'ETSI GS MEC 011 standard API, used for application traffic rules, DNS rule configuration, etc.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('70168c8b-ff06-49dd-a7d2-685cc8273c73', '应用使能API MEC 011', 'MEC Application Support API MEC 011', 'v2.1.1', 'ETSI GS MEC 011标准API，用于应用的流量规则、DNS规则配置等', 'ETSI GS MEC 011 standard API, used for application traffic rules, DNS rule configuration, etc.', 'ETSI', '5ad3b703-2b81-4779-98a7-b5b4e0b74983', '1d22f1af-53de-4806-b0e3-2d0fdba9631d', '', '', 8080, 'application-support', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('1d22f1af-53de-4806-b0e3-2d0fdba9631d', 'MecAppSupportApi.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/MecAppSupportApi.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('662d474c-151b-4791-83bb-17834c7c381e', 'ETSI', 'ETSI', '服务管理API MEC 011', 'Service Management API  API MEC 011', 'OPENMEP', 'ETSI GS MEC 011标准API，用于应用的服务注册、发现、订阅等功能', 'ETSI GS MEC 011 standard API, used for application service registration, discovery, subscription, stc.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('a435f8fc-6110-46b8-8eae-5d23caa5be9f', '服务管理API MEC 011', 'Service Management API  API MEC 011', 'v2.1.1', 'ETSI GS MEC 011标准API，用于应用的服务注册、发现、订阅等功能', 'ETSI GS MEC 011 standard API, used for application service registration, discovery, subscription, stc.', 'ETSI', '662d474c-151b-4791-83bb-17834c7c381e', 'a5983da8-3b06-4f8b-b3fb-217062a209b5', '', '', 8080, 'service-management ', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('a5983da8-3b06-4f8b-b3fb-217062a209b5', 'MecServiceMgmtApi.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/MecServiceMgmtApi.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('a96d265e-652b-423c-b694-1a7fd02ab4da', 'ETSI', 'ETSI', 'API原则和指南 MEC 009', 'API Principle Guide MEC 009', 'OPENMEP', 'ETSI GS MEC 009标准API定义原则和使用指南', 'ETSI GS MEC 009 standard API definition principles and use guidelines')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('07c8c8f8-5c3f-481f-9fa4-18339799d10f', 'API原则和指南 MEC 009', 'API Principle Guide MEC 009', 'v2.1.1', 'ETSI GS MEC 009标准API定义原则和使用指南', 'ETSI GS MEC 009 standard API definition principles and use guidelines', 'ETSI', 'a96d265e-652b-423c-b694-1a7fd02ab4da', '', '', '', 8080, 't ', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('4f16fa8b-1ebc-4711-bb34-98a8ee4bd00c', 'ETSI', 'ETSI', '位置服务API MEC 013', 'Location API MEC 013', 'OPENMEP', 'ETSI GS MEC 013 标准定义的位置服务API', 'The ETSI MEC ISG MEC013 Location API described using OpenAPI.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('a407b72d-e461-4bd3-8ab7-dea18bb9047f', '位置服务API MEC 013', 'Location API MEC 013', 'v2.1.1', 'ETSI GS MEC 013 标准定义的位置服务API', 'The ETSI MEC ISG MEC013 Location API described using OpenAPI.', 'ETSI', '4f16fa8b-1ebc-4711-bb34-98a8ee4bd00c', '0a0d5f75-5d64-410b-aaa2-f13c157477c0', '', '', 8080, 'location', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('0a0d5f75-5d64-410b-aaa2-f13c157477c0', 'LocationAPI.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/LocationAPI.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('6e8f8783-4b58-4dc9-8fc6-1095f0f4b338', 'ETSI', 'ETSI', 'V2X信息服务API MEC 030', 'V2X Information Service API MEC 030', 'OPENMEP', 'ETSI GS MEC 030标准定义的V2X信息服务API。', 'ETSI GS MEC 030 V2X Information Service API described using OpenAPI.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('925eb263-50b9-4d18-aa3b-92150bce2b84', 'V2X信息服务API MEC 030', 'V2X Information Service API MEC 030', 'v2.1.1', 'ETSI GS MEC 030标准定义的V2X信息服务API。', 'ETSI GS MEC 030 V2X Information Service API described using OpenAPI.', 'ETSI', '6e8f8783-4b58-4dc9-8fc6-1095f0f4b338', '1ec63237-569a-4ff6-8949-602df141219e', '', '', 8080, 'vis ', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('1ec63237-569a-4ff6-8949-602df141219e', 'MEC030_V2XInformationService.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/MEC030_V2XInformationService.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('daa8afa6-aa4d-4697-931e-da8b936bd54e', 'ETSI', 'ETSI', '固网信息API MEC 029', 'Fixed Access Information API MEC 029', 'OPENMEP', 'ETSI GS MEC 029标准定义的固定访问信息API。', 'ETSI GS MEC 029 Fixed Access Information API described using OpenAPI.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('925eb263-50b9-4d18-aa3b-92150bce2b86', '固网信息API MEC 029', 'Fixed Access Information API MEC 029', 'v2.1.1', 'ETSI GS MEC 029标准定义的固定访问信息API。', 'ETSI GS MEC 029 Fixed Access Information API described using OpenAPI.', 'ETSI', 'daa8afa6-aa4d-4697-931e-da8b936bd54e', '6f06d423-f5f9-49a6-a6a8-474ad5e31299', '', '', 8080, 'fai ', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('6f06d423-f5f9-49a6-a6a8-474ad5e31299', 'MEC029_FAI.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/MEC029_FAI.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('2af5288e-6ca2-4c26-bf5e-b10d7949354c', 'ETSI', 'ETSI', 'UE身份API MEC 014', 'UE Identity API MEC 014', 'OPENMEP', 'The ETSI MEC ISG MEC014标准定义的UE身份API。', 'The ETSI MEC ISG MEC014 UE Identity API described using OpenAPI')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('925eb263-50b9-4d18-aa3b-92150bce2b87', 'UE身份API MEC 014', 'UE Identity API MEC 014', 'v2.1.1', 'The ETSI MEC ISG MEC014标准定义的UE身份API。', 'The ETSI MEC ISG MEC014 UE Identity API described using OpenAPI', 'ETSI', '2af5288e-6ca2-4c26-bf5e-b10d7949354c', 'e6d5c865-1328-4fc0-ba09-13b1f2d91ce1', '', '', 8080, 'ui ', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('e6d5c865-1328-4fc0-ba09-13b1f2d91ce1', 'UEidentityAPI.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/UEidentityAPI.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('9a1bbe26-fb62-4cfe-a3fe-55e9c53228f4', 'ETSI', 'ETSI', 'WLAN信息API MEC 028', 'WLAN Information API MEC 028', 'OPENMEP', 'ETSI MEC ISG MEC028标准定义的WLAN信息API。', 'The ETSI MEC ISG MEC028 WLAN Access Information API described using OpenAPI')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('a407b72d-e461-4bd3-8ab7-dea18bb9047a', 'WLAN信息API MEC 028', 'WLAN Information API MEC 028', 'v2.1.1', 'ETSI MEC ISG MEC028标准定义的WLAN信息API。', 'The ETSI MEC ISG MEC028 WLAN Access Information API described using OpenAPI', 'ETSI', '9a1bbe26-fb62-4cfe-a3fe-55e9c53228f4', 'fe7156d1-b6a6-40c1-a6e7-3f2a2e442d1d', '', '', 8080, 'wai', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('fe7156d1-b6a6-40c1-a6e7-3f2a2e442d1d', 'WlanInformationApi.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/WlanInformationApi.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('6f30be1c-22a8-42c9-82b1-f5d692c8a46c', 'ETSI', 'ETSI', '移动性API MEC 021', 'Application Mobility Service API MEC 021', 'OPENMEP', 'ETSI GS MEC 021 标准定义的移动性API', 'ETSI GS MEC 021 Application Mobility Service API described using OpenAPI.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('a407b72d-e461-4bd3-8ab7-dea18bb9047c', '移动性API MEC 021', 'Application Mobility Service API MEC 021', 'v2.1.1', 'ETSI GS MEC 021 标准定义的移动性API', 'ETSI GS MEC 021 Application Mobility Service API described using OpenAPI.', 'ETSI', '6f30be1c-22a8-42c9-82b1-f5d692c8a46c', '16832aff-6d18-47d1-850b-c6b2162124be', '', '', 8080, 'amsi', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('16832aff-6d18-47d1-850b-c6b2162124be', 'MEC021_AppMobilityService.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/MEC021_AppMobilityService.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('53d3f69c-29bc-4895-a45e-300c9a7b6f68', 'ETSI', 'ETSI', 'UE应用程序接口API MEC 016', 'UE Application Interface API MEC 016', 'OPENMEP', 'ETSI GS MEC 016 标准定义的UI应用程序接口API', 'The ETSI MEC ISG Device application interface API described using OpenAPI.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('a407b72d-e461-4bd3-8ab7-dea18bb9047d', 'UE应用程序接口API MEC 016', 'UE Application Interface API MEC 016', 'v2.1.1', 'ETSI GS MEC 016 标准定义的UE应用程序接口API', 'The ETSI MEC ISG Device application interface API described using OpenAPI.', 'ETSI', '53d3f69c-29bc-4895-a45e-300c9a7b6f68', '7fdfd6e8-7dc5-4a3c-9862-45d04ce2c1f9', '', '', 8080, 'dev_app', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('7fdfd6e8-7dc5-4a3c-9862-45d04ce2c1f9', 'UEAppInterfaceApi.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/UEAppInterfaceApi.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('414f88ca-f0cd-4d1e-b3c5-4d8f9131a260', 'ETSI', 'ETSI', '带宽管理API MEC 015', 'Bandwidth Management API MEC 015', 'OPENMEP', 'ETSI GS MEC 015 标准定义的带宽管理API', 'The ETSI MEC ISG Bandwidth Management API described using OpenAPI.')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('a407b72d-e461-4bd3-8ab7-dea18bb9047e', '带宽管理API MEC 015', 'Bandwidth Management API MEC 015', 'v2.1.1', 'ETSI GS MEC 015 标准定义的带宽管理API', 'The ETSI MEC ISG Bandwidth Management API described using OpenAPI.', 'ETSI', '414f88ca-f0cd-4d1e-b3c5-4d8f9131a260', '7bcb9fa2-0707-4961-bbe4-02dbbddf2f3a', '', '', 8080, 'bwm', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('7bcb9fa2-0707-4961-bbe4-02dbbddf2f3a', 'BwManagementApi.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/BwManagementApi.json')
    ON CONFLICT(file_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('4bc92750-85be-46e1-ac22-9424241e6ca8', 'ETSI', 'ETSI', '无线网络信息API MEC 012', 'Radio Network Information API MEC 012', 'OPENMEP', 'ETSI GS MEC 012 标准定义的无线网络信息API', 'The ETSI MEC ISG MEC012 Radio Network Information API described using OpenAPI')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('a407b72d-e461-4bd3-8ab7-dea18bb9047g', '无线网络信息API MEC 012', 'Radio Network Information API MEC 012', 'v2.1.1', 'ETSI GS MEC 012 标准定义的无线网络信息API', 'The ETSI MEC ISG MEC012 Radio Network Information API described using OpenAPI', 'ETSI', '4bc92750-85be-46e1-ac22-9424241e6ca8', 'a56461cb-7dd3-46f1-8d71-3677d098ee19', '', '', 8080, 'rni', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    INSERT INTO tbl_uploaded_file (file_id, file_name, is_temp, user_id, upload_date, file_path) VALUES
    ('a56461cb-7dd3-46f1-8d71-3677d098ee19', 'RniAPI.json', false, 'admin', '2021-4-3 00:00:00.000000', '/mep_capability/RniAPI.json')
    ON CONFLICT(file_id) do nothing;

    -- workspace 3GPP capability init --

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('6aef3449-29d5-48a1-bde4-aa723ced62a9', '3GPP', '3GPP', '动态能力发布', 'Dynamic Capability Release', 'OPENMEP', '动态能力发布', 'Dynamic Capability Release')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('a407b72d-e461-4bd3-8ab7-dea18bb9041a', '动态能力发布', 'Dynamic Capability Release', 'v2.1.1', '动态能力发布', 'Dynamic Capability Release', '3GPP', '6aef3449-29d5-48a1-bde4-aa723ced62a9', '', '', '', 8080, '', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;


    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('0a96b3f8-72a8-4373-a129-cc30329e73bf', '3GPP', '3GPP', '用户管理', 'User Management', 'OPENMEP', '用户管理', 'User Management')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('a407b72d-e461-4bd3-8ab7-dea18bb9041c', '用户管理', 'User Management', 'v2.1.1', '用户管理', 'User Management', '3GPP', '0a96b3f8-72a8-4373-a129-cc30329e73bf', '', '', '', 8080, '', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;


    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('f1e297d4-437a-4d5a-bf0b-66170f9aadd8', '3GPP', '3GPP', '移动性管理', 'Mobility Management', 'OPENMEP', '移动性管理', 'Mobility Management')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('a407b72d-e461-4bd3-8ab7-dea18bb9041d', '移动性管理', 'Mobility Management', 'v2.1.1', '移动性管理', 'Mobility Management', '3GPP', 'f1e297d4-437a-4d5a-bf0b-66170f9aadd8', '', '', '', 8080, '', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;

    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('900ab8ae-782d-4c51-b6ca-d2a2b4b281b6', '3GPP', '3GPP', '服务计费', 'Service Billing', 'OPENMEP', '服务计费', 'Service Billing')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('a407b72d-e461-4bd3-8ab7-dea18bb9041e', '服务计费', 'Service Billing', 'v2.1.1', '服务计费', 'Service Billing', '3GPP', '900ab8ae-782d-4c51-b6ca-d2a2b4b281b6', '', '', '', 8080, '', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;


    INSERT INTO tbl_openmep_capability (group_id, one_level_name, one_level_name_en, two_level_name, two_level_name_en, type, description, description_en) VALUES
    ('9527db3c-d296-492c-8375-b1a78d815589', '3GPP', '3GPP', '服务审计', 'Service Audit', 'OPENMEP', '服务审计', 'Service Audit')
    ON CONFLICT(group_id) do nothing;
    INSERT INTO tbl_openmep_capability_detail (detail_id, service, service_en, version, description, description_en, provider, group_id, api_file_id, guide_file_id, guide_file_id_en, port, host, protocol, upload_time, user_id) VALUES
    ('a407b72d-e461-4bd3-8ab7-dea18bb9041f', '服务审计', 'Service Audit', 'v2.1.1', '服务审计', 'Service Audit', '3GPP', '9527db3c-d296-492c-8375-b1a78d815589', '', '', '', 8080, '', 'http', '2021-03-04 00:00:00.000000', 'admin')
    ON CONFLICT(detail_id) do nothing;
    -- workspace mep capability init end--