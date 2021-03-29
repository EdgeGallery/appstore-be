# AppStore-be

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Jenkins](https://img.shields.io/jenkins/build?jobUrl=http://jenkins.edgegallery.org/view/GITEE-MEC-PLATFORM-BUILD/job/appstore-backend-docker-master-daily/)

AppStore是开发者发布和上线App应用的市场，上传App包后首先要通过测试，只有检测通过的应用才能够正式上线。AppStore分为前后台两个部分，AppStore-be是后台部分，提供接口调用，
[AppStore-fe](https://gitee.com/edgegallery/appstore-fe)是前台部分，提供界面展示。有关AppStore架构的详细介绍请参考[AppStore_Overview](https://gitee.com/edgegallery/docs/blob/master/Projects/APPSTORE/AppStore_Overview.md)。


## 本地运行

为了更好地帮助开发者理解和使用AppStore-be，我们提供了[AppStore_Setting Up Local Development Environment](https://gitee.com/edgegallery/docs/blob/master/Projects/APPSTORE/AppStore_Setting%20Up%20Local%20Development%20Environment.md)
指导开发者如何在本地快速启动、运行AppStore项目。

## 编译构建

AppStore-be项目基于docker进行了容器化改造，在编译构建时共分为两步.

- ### 编译

AppStore-be是基于jdk1.8和maven编写的Java程序，编译只需要执行`mvn install`即可实现编译，生成jar包

- ### 构建镜像

AppStore-be项目提供了镜像制作的dockerfile文件，在制作镜像时可以使用如下命令
```
docker build -t appstore-be:latest -f docker/Dockerfile .
```
