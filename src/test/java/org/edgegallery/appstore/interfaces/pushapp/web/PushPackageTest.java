package org.edgegallery.appstore.interfaces.pushapp.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import org.edgegallery.appstore.interfaces.AppstoreApplicationTest;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushTargetAppStoreDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class PushPackageTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Gson gson = new Gson();

    @Before
    public void setUp() throws Exception {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).apply(springSecurity()).build();
    }

    @WithMockUser(roles = "APPSTORE_TENANT")
    @Test
    public void should_success_when_get_all_pushablepackages() throws Exception {
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/mec/appstore/poke/pushable/packages")
            .contentType(MediaType.APPLICATION_JSON_VALUE).with(csrf()).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        int result = mvcResult.getResponse().getStatus();
        assertEquals(200, result);
        String content = mvcResult.getResponse().getContentAsString();
        List<PushablePackageDto> packages = gson
            .fromJson(content, new TypeToken<List<PushablePackageDto>>() { }.getType());
        assertFalse(packages.isEmpty());
    }

    @WithMockUser(roles = "APPSTORE_TENANT")
    @Test
    public void should_success_when_get_pushablepackages() throws Exception {
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/mec/appstore/poke/pushable/packages/packageid-0002")
                .contentType(MediaType.APPLICATION_JSON_VALUE).with(csrf()).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        int result = mvcResult.getResponse().getStatus();
        assertEquals(200, result);
        String content = mvcResult.getResponse().getContentAsString();
        PushablePackageDto packageDto = gson.fromJson(content, PushablePackageDto.class);
        assertEquals("packageid-0002", packageDto.getPackageId());
    }

    @WithMockUser(roles = "APPSTORE_TENANT")
    @Test
    public void should_success_when_push_package_notice() throws Exception {
        PushTargetAppStoreDto dto = new PushTargetAppStoreDto();
        List<String> targetPlatform = new ArrayList<>();
        targetPlatform.add("appstore-test-0001");
        targetPlatform.add("appstore-test-0002");
        dto.setTargetPlatform(targetPlatform);

        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/poke/pushable/packages/packageid-0002/action/push")
                .contentType(MediaType.APPLICATION_JSON_VALUE).with(csrf()).content(gson.toJson(dto))
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        int result = mvcResult.getResponse().getStatus();
        assertEquals(200, result);
        String content = mvcResult.getResponse().getContentAsString();
        assertEquals("ok", content);
    }

}
