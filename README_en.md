# AppStore-be

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Jenkins](https://img.shields.io/jenkins/build?jobUrl=http://jenkins.edgegallery.org/view/GITEE-MEC-PLATFORM-BUILD/job/appstore-backend-docker-master-daily/)

AppStoreIs the developer release and go onlineAppApplied market，UploadAppAfter the package, it must first pass the test，Only apps that pass the test can be officially launched。AppStoreDivided into two parts: front and back，AppStore-beIs the background part，Provide interface call，
[AppStore-fe](https://gitee.com/edgegallery/appstore-fe)Is the front desk part，Provide interface display。relatedAppStoreFor a detailed introduction to the architecture, please refer to[AppStore_Overview](https://gitee.com/edgegallery/docs/blob/master/Projects/APPSTORE/AppStore_Overview.md)。


## Run locally

To better help developers understand and useAppStore-be，We provide[AppStore_Setting Up Local Development Environment](https://gitee.com/edgegallery/docs/blob/master/Projects/APPSTORE/AppStore_Setting%20Up%20Local%20Development%20Environment.md)
Instruct developers how to quickly start locally、runAppStoreproject。

## Compile and build

AppStore-beThe project is based ondockerContainerized transformation，There are two steps when compiling and building.

- ### Compile

AppStore-beis based onjdk1.8withmavenWrittenJavaprogram，Compilation only needs to be executed`mvn install`Can be compiled，generatejarpackage

- ### Build image

AppStore-beThe project provides mirrored productiondockerfilefile，You can use the following commands when making a mirror
```
docker build -t appstore-be:latest -f docker/Dockerfile .
```
