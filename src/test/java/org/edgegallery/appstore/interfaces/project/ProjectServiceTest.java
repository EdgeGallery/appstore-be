package org.edgegallery.appstore.interfaces.project;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.system.lcm.DistributeResponse;
import org.edgegallery.appstore.domain.model.system.lcm.UploadResponse;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.infrastructure.persistence.apackage.PackageMapper;
import org.edgegallery.appstore.interfaces.AppTest;
import org.edgegallery.appstore.interfaces.system.facade.ProjectService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

// @RunWith(SpringRunner.class)
// @SpringBootTest(classes = AppstoreApplicationTest.class)
// @AutoConfigureMockMvc
public class ProjectServiceTest extends AppTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    protected PackageMapper packageMapper;

    private HttpServer httpServer;

    private String token = "123456789";

    @Before
    public void before() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 30201), 0);
        httpServer.createContext("/lcmcontroller/v2/tenants/userId/packages", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("POST")) {
                    UploadPackageDto uploadPackageDto = new UploadPackageDto();
                    UploadResponse uploadResponse = new UploadResponse();
                    uploadResponse.setAppId("test-app-id");
                    uploadResponse.setPackageId("test-pkg-id");
                    uploadPackageDto.setData(uploadResponse);
                    String dtp = new Gson().toJson(uploadPackageDto);
                    byte[] response = dtp.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.createContext("/lcmcontroller/v2/tenants/userId/packages/test-pkg-id", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("GET")) {
                    DistributeResponseDto dto = new DistributeResponseDto();
                    List<DistributeResponse> data = new ArrayList<>();
                    DistributeResponse dis = new DistributeResponse();
                    dis.setAppId("test-app-id");
                    dis.setPackageId("test-pkg-id");
                    data.add(dis);
                    dto.setData(data);
                    String dtp = new Gson().toJson(dto);
                    byte[] response = dtp.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                } else if (method.equals("POST")) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                }
                exchange.close();
            }
        });
        httpServer.createContext(
            "/lcmcontroller/v2/tenants/userId/app_instances/3f50936d-f10f-41ff-9c05-bdf5da951b53/instantiate",
            new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                    if (!token.equals(accessToken)) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                        exchange.getResponseBody().write("FORBIDDEN".getBytes());
                    } else if (method.equals("GET")) {
                        DistributeResponseDto dto = new DistributeResponseDto();
                        List<DistributeResponse> data = new ArrayList<>();
                        DistributeResponse dis = new DistributeResponse();
                        dis.setAppId("test-app-id");
                        dis.setPackageId("test-pkg-id");
                        data.add(dis);
                        dto.setData(data);
                        String dtp = new Gson().toJson(dto);
                        byte[] response = dtp.getBytes();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                        exchange.getResponseBody().write(response);
                    } else if (method.equals("POST")) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                    }
                    exchange.close();
                }
            });
        httpServer.createContext("/lcmcontroller/v2/tenants/userId/app_instances/3f50936d-f10f-41ff-9c05-bdf5da951b53",
            new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                    if (!token.equals(accessToken)) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                        exchange.getResponseBody().write("FORBIDDEN".getBytes());
                    } else if (method.equals("GET")) {
                        WorkloadStatusDto workloadStatusDto = new WorkloadStatusDto();
                        WorkLoadStatus workLoadStatus = new WorkLoadStatus();
                        List<PortFromLcm> ports = new ArrayList<>();
                        ports.add(PortFromLcm.builder().nodePort("5588").build());
                        ports.add(PortFromLcm.builder().nodePort("9901").build());
                        workLoadStatus.getServices().add(ServiceFromLcm.builder().serviceName("serviceName1").ports(ports).build());
                        workLoadStatus.getServices().add(ServiceFromLcm.builder().serviceName("serviceName2").ports(ports).build());
                        workloadStatusDto.setData(new Gson().toJson(workLoadStatus));
                        String dtp = new Gson().toJson(workloadStatusDto);
                        byte[] response = dtp.getBytes();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                        exchange.getResponseBody().write(response);
                    }
                    exchange.close();
                }
            });
        httpServer.start();
    }

    @After
    public void after() {
        httpServer.stop(1);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_when_deploy_apk() throws IOException {
        projectService.setInstantiateAppSleepTime(1000);
        projectService.setUploadPkgSleepTime(1000);
        Optional.ofNullable(packageMapper.findReleaseById(unPublishedPackageId)).ifPresent(r -> {
            r.setStatus(EnumPackageStatus.Published.toString());
            r.setAppId("appid-test-0001");
            r.setAppInstanceId("3f50936d-f10f-41ff-9c05-bdf5da951b53");
            try {
                File csarFIle = Resources.getResourceAsFile("testfile/new_csar.csar");
                r.setPackageAddress(csarFIle.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            packageMapper.updateRelease(r);
            packageMapper.updateAppInstanceApp(r);
        });
        ResponseEntity<ResponseObject> response = projectService
            .deployAppById("appid-test-0001", unPublishedPackageId, "userId", "Node2", "localhost", "123456789");
        Assert.assertEquals(200, response.getStatusCode().value());
        Assert.assertEquals("get app url success.", Objects.requireNonNull(response.getBody()).getMessage());
    }
    //
    // @Test
    // @WithMockUser(roles = "APPSTORE_TENANT")
    // public void should_success_when_clean_env() {
    // }

}
