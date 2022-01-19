# Developer-be Developer Platform

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Jenkins](https://img.shields.io/jenkins/build?jobUrl=http%3A%2F%2Fjenkins.edgegallery.org%2Fview%2FMEC-PLATFORM-BUILD%2Fjob%2Fdeveloper-backend-docker-image-build-update-daily-master%2F)

The developer platform is a platform that provides development tools/testing environment/online deployment for App developers. It is divided into two parts: front and back. Developer-be is the back-end part, which provides interface calls. [developer-fe](https://github .com/EdgeGallery/developer-fe) is the front section, providing interface display. For a detailed introduction to the architecture of the developer platform please visit our [wiki community](https://edgegallery.atlassian.net/wiki/spaces/EG/overview)

## Feature introduction

In order to facilitate developers to develop and test APP, we provide a developer platform, which mainly includes the following capabilities:

- **App Incubation**

- The process for developers to incubate their own applications, including creating applications, selecting platform capabilities (including the ability to configure the capabilities to be released) [optional steps], selecting and deploying sandboxes, packaging applications, making images, ATP testing, and publishing applications to the AppStore.

- Both container applications and virtual machine applications are supported in the application incubation process.

- **Capability Center**

  All available capabilities provided by the developer platform (including capabilities provided by the platform itself, capabilities released by developers).

- **App editing**

  The administrator enters the application editing through the toolbox menu, selects the application to be edited from the AppStore synchronization, and can be republished after editing.

- **System Management**

  - **_Sandbox management_**

  - Administrators enter sandbox management through the system management menu, including sandbox creation, query, modification, and deletion operations.
  - Different types of sandboxes have different configurations. OpenStack and FusionSphere sandboxes can configure network parameters and resource configuration.

  - **_Capability Center Management_**

  - The administrator enters the capability center management through the system management menu, including the creation, query, modification and deletion of platform capabilities.

  - When creating a capability, it is created based on the capability provided by the platform itself.

  - **_System image management_**

  - The administrator enters the system image management through the system management menu, including uploading, querying, modifying (whether public or not), downloading, and deleting container images.

  - The administrator enters the system image management through the system management menu, including uploading, querying, publishing (convenient for other developers to download and use), downloading, deleting, and slimming (reducing the size of the virtual machine image) of the virtual machine image.

## compile and run

developer-be provides restful interfaces, develops based on the open source ServiceComb microservice framework, and integrates the Spring Boot framework. Local operation needs to rely on ServiceCenter for service registration discovery and postman for interface testing.

- ### Environment preparation (run locally)

  | Name                       | Version         | Link                                                                             |
  | -------------------------- | --------------- | -------------------------------------------------------------------------------- |
  | JDK1.8                     | 1.8xxx or above | [download](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)  |
  | MavApache Maven            | 3.6.3           | [download](https://maven.apache.org/download.cgi)                                |
  | IntelliJ IDEA              | Community       | [download](https://www.jetbrains.com/idea/download/)                             |
  | Servicecomb Service-Center | 1.3.2           | [download](https://servicecomb.apache.org/cn/release/service-center-downloads/)  |
  | Postgres                   | 9.6.17 or above | [download](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads) |

- ### Modify the configuration file /src/main/resources/application.yaml

  - 1 Modify the postgres configuration. The default IP for local installation is 127.0.0.1, the default port is 5432, and the default username and password are as follows:

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

  - 2 Configure Service Center, the local installation IP is 127.0.0.1, the default port is 30100, servicecomb.name is the service name registered on the servicecenter, which can be modified, the default is mec-developer, and the configuration is as follows:

  ```
  #### Service Center config ####
  # ip or service name in k8s
  servicecomb:
    service:
      registry:
      address: *** #Connect the address of SC (Service Center, Registration Center)
    rest:
      address: *** #Rest communication address (ip:port)
      servlet:
        urlPattern: /cse/*
  ```

- ### Compile and package

  Pull the code from the code repository, the default master branch

  ```
  git clone https://github.com/EdgeGallery/developer-be.git
  ```

  Compile and build, you need to rely on JDK1.8, the first compilation will be time-consuming, because maven needs to download all the dependent libraries.

  ```
  mvn clean install
  ```

- ### run
  cd to the package path and start via java:
  ```
  java -jar mec-developer-platform.jar
  ```
  After startup, visit http://127.0.0.1/30103 through a browser to check whether the service is successfully registered.
