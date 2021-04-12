# Developer-be 开发者平台

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Jenkins](https://img.shields.io/jenkins/build?jobUrl=http%3A%2F%2Fjenkins.edgegallery.org%2Fview%2FMEC-PLATFORM-BUILD%2Fjob%2Fdeveloper-backend-docker-image-build-update-daily-master%2F)

开发者平台是为App开发者提供开发工具/测试环境/上线部署的平台，分为前后台两个部分，developer-be是后台部分，提供接口调用，[developer-fe](https://github.com/EdgeGallery/developer-fe)是前台部分，提供界面展示。有关开发者平台的架构的详细介绍请访问我们的[wiki社区](https://edgegallery.atlassian.net/wiki/spaces/EG/overview)


## 特性介绍

为了方便开发者进行APP的开发和测试，我们提供开发者平台，主要包含以下能力：

- **插件管理** 
    
    向开发者提供实用的插件，帮助开发者进行快速开发，并且插件会持续更新，同时开发者也可以参与到插件开发过程中，将自己的插件贡献到平台上。

- **API管理** 
    - MEP-API
    
        平台提供可用的MEP接口供开发者使用，提高开发者的开发效率，并能够开发出更加实用的App应用。MEP接口会持续更新，丰富能力。提供工具链，快速将App从x86平台迁移到RAM平台；
    - MEP-ECO API
    
        开发者也可以将自己开发的App通过接口的形式贡献出来，供其它开发者调用。

- **Projet管理** 

    - 针对开发者，提供从App立项/开发/测试/部署/上线发布等环节的管理；
    
    - 提供转换工具可以将x86平台的App快速转换成RAM平台运行；
    
    - 提供测试沙箱，可以将App自动部署到公有云环境，供用户测试；
    
    - 直接对接AppStore平台，开发测试通过的App可以直接发布到AppStore。

## 编译运行

  developer-be对外提供restful接口，基于开源的ServiceComb微服务框架进行开发，并且集成了Spring Boot框架。本地运行需要依赖ServiceCenter进行服务注册发现，通过postman进行接口测试。

- ### 环境准备（本地运行）
  
    |  Name     | Version   | Link |
    |  ----     | ----  |  ---- |
    | JDK1.8 |1.8xxx or above | [download](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)
    | MavApache Maven |3.6.3 | [download](https://maven.apache.org/download.cgi)
    | IntelliJ IDEA |Community |[download](https://www.jetbrains.com/idea/download/)
    | Servicecomb Service-Center    | 1.3.0 | [download](https://servicecomb.apache.org/cn/release/service-center-downloads/)
    | Postgres  | 9.6.17 or above |   [download](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads) |

- ### 修改配置文件/src/main/resources/application.properties

    - 1 修改postgres配置，本地安装默认IP是127.0.0.1，默认端口是5432，默认用户名和密码，如下：
    ```
    postgres.ip=127.0.0.1
    postgres.database=postgres
    postgres.port=5432
    postgres.username=***
    postgres.password=***
    ```
    - 2 配置Service Center，本地安装IP是127.0.0.1，默认端口30100，servicecomb.name是注册到servicecenter上的服务名，可修改，默认是mec-developer，配置如下：
    ```
    #### Service Center config ####
    # ip or service name in k8s
    servicecenter.ip=127.0.0.1
    servicecenter.port=30100
    servicecomb.name=mec-developer
    ```

- ### 编译打包
    从代码仓库拉取代码，默认master分支
    
    ```
    git clone https://github.com/EdgeGallery/developer-be.git
    ```

    编译构建，需要依赖JDK1.8，首次编译会比较耗时，因为maven需要下载所有的依赖库。

    ```
    mvn clean install
    ```

- ### 运行
    cd到打包路径，通过java启动：
    ```
    java -jar mec-developer-platform.jar
    ```
    启动后通过浏览器访问 http://127.0.0.1/30103 可以查看服务是否注册成功。
