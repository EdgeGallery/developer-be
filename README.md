# Developer-be 开发者平台

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Jenkins](https://img.shields.io/jenkins/build?jobUrl=http%3A%2F%2Fjenkins.edgegallery.org%2Fview%2FMEC-PLATFORM-BUILD%2Fjob%2Fdeveloper-backend-docker-image-build-update-daily-master%2F)

开发者平台是为 App 开发者提供开发工具/测试环境/上线部署的平台，分为前后台两个部分，developer-be 是后台部分，提供接口调用，[developer-fe](https://github.com/EdgeGallery/developer-fe)是前台部分，提供界面展示。有关开发者平台的架构的详细介绍请访问我们的[wiki 社区](https://edgegallery.atlassian.net/wiki/spaces/EG/overview)

## 特性介绍

为了方便开发者进行 APP 的开发和测试，我们提供开发者平台，主要包含以下能力：

- **应用孵化**

- 开发者孵化自我应用的流程，包括创建应用、选择平台能力（包括可以配置需要发布的能力）【可选步骤】、选择部署沙箱、应用打包、制作镜像、认证测试、发布应用到 AppStore。

- 应用孵化流程均支持容器应用和虚机应用。

- **能力中心**

  开发者平台提供的所有可用的能力（包括平台自身提供的能力、开发者发布的能力）。

- **应用编辑**

  管理员通过工具箱菜单进入应用编辑，选择从 AppStore 同步需要编辑的应用，编辑之后可以重新发布。

- **系统管理**

  - **_沙箱管理_**

  - 管理员通过系统管理菜单进入沙箱管理，包括沙箱的创建、查询、修改、删除操作。
  - 不同类型的沙箱配置不同，OpenStack、FusionSphere 类型的沙箱可以配置网络参数、资源配置等。

  - **_能力中心管理_**

  - 管理员通过系统管理菜单进入能力中心管理，包括平台能力的创建、查询、修改、删除操作。

  - 创建能力时，基于平台自身提供的能力创建。

  - **_系统镜像管理_**

  - 管理员通过系统管理菜单进入系统镜像管理，包括容器镜像的上传、查询、修改（是否公开）、下载、删除操作。

  - 管理员通过系统管理菜单进入系统镜像管理，包括虚拟机镜像的上传、查询、发布（方便其他开发者下载使用）、下载、删除、瘦身（精简虚机镜像大小）等操作。

## 编译运行

developer-be 对外提供 restful 接口，基于开源的 ServiceComb 微服务框架进行开发，并且集成了 Spring Boot 框架。本地运行需要依赖 ServiceCenter 进行服务注册发现，通过 postman 进行接口测试。

- ### 环境准备（本地运行）

  | Name                       | Version         | Link                                                                             |
  | -------------------------- | --------------- | -------------------------------------------------------------------------------- |
  | JDK1.8                     | 1.8xxx or above | [download](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)  |
  | MavApache Maven            | 3.6.3           | [download](https://maven.apache.org/download.cgi)                                |
  | IntelliJ IDEA              | Community       | [download](https://www.jetbrains.com/idea/download/)                             |
  | Servicecomb Service-Center | 1.3.2           | [download](https://servicecomb.apache.org/cn/release/service-center-downloads/)  |
  | Postgres                   | 9.6.17 or above | [download](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads) |

- ### 修改配置文件/src/main/resources/application.yaml

  - 1 修改 postgres 配置，本地安装默认 IP 是 127.0.0.1，默认端口是 5432，默认用户名和密码，如下：

  ```
  spring:
    datasource:
      url: jdbc:postgresql://127.0.0.1:5432/developerdb
      username: ***
      password: ***
      driver-class-name: org.postgresql.Driver
      initialization-mode: always
      schema: ***
      data: ***
  ```

  - 2 配置 Service Center，本地安装 IP 是 127.0.0.1，默认端口 30100，servicecomb.name 是注册到 servicecenter 上的服务名，可修改，默认是 mec-developer，配置如下：

  ```
  #### Service Center config ####
  # ip or service name in k8s
  servicecomb:
    service:
      registry:
      address: *** #连接SC(Service Center,注册中心)的地址
    rest:
      address: ***  #Rest通信地址(ip:port)
      servlet:
        urlPattern: /cse/*
  ```

- ### 编译打包

  从代码仓库拉取代码，默认 master 分支

  ```
  git clone https://github.com/EdgeGallery/developer-be.git
  ```

  编译构建，需要依赖 JDK1.8，首次编译会比较耗时，因为 maven 需要下载所有的依赖库。

  ```
  mvn clean install
  ```

- ### 运行
  cd 到打包路径，通过 java 启动：
  ```
  java -jar mec-developer-platform.jar
  ```
  启动后通过浏览器访问 http://127.0.0.1/30103 可以查看服务是否注册成功。
