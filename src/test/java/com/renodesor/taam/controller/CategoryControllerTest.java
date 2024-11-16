package com.renodesor.taam.controller;

import com.renodesor.taam.entity.Category;
import com.renodesor.taam.entity.TaamUser;
import com.renodesor.taam.security.JwtConverter;
import com.renodesor.taam.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
//@ContextConfiguration(classes = {JwtConverter.class})
@ActiveProfiles("test")
class CategoryControllerTest {
    @Autowired
    private MockMvc mvc;

    @InjectMocks
    private TaamUser taamUser;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup()
    {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        taamUser = Mockito.mock(TaamUser.class);
        Mockito.when(taamUser.getFirstName()).thenReturn("Reno");
        Mockito.when(taamUser.getLastName()).thenReturn("Desor");
    }

    @Test
    @DisplayName("Retrieve all categories")
    @Sql({"classpath:db-scripts/test-data.sql"})
    void testGetAllCategories() throws Exception {
        String expected = "[{\"id\":\"ee330a97-96ac-401c-97f2-e5d8d922f815\",\"createdBy\":\"renodesor\",\"createdOn\":\"2024-09-26T01:39:15.893\",\"updatedBy\":\"wisline.pl@gmail.com\",\"updatedOn\":\"2024-10-12T21:31:19.677\",\"name\":\"Lecture20\",\"description\":\"Lecture 20\"}," +
                "{\"id\":\"ee330a97-96ac-401c-97f2-e5d8d922f805\",\"createdBy\":\"renodesor\",\"createdOn\":\"2024-09-26T01:39:15.893\",\"updatedBy\":\"wisline.pl@gmail.com\",\"updatedOn\":\"2024-10-12T21:31:19.677\",\"name\":\"Lecture10\",\"description\":\"Lecture 10\"}]";
        MockHttpServletResponse response = mvc
                .perform(get("/taam-api/categories"))
                .andReturn().getResponse();
        Assertions.assertEquals(200, response.getStatus(), "The api must return a succes code - Http status 200");
        Assertions.assertEquals(expected, response.getContentAsString(), "The api must return two categories");
    }

    @Test
    @DisplayName("Retrieve a category by id")
    @Sql({"classpath:db-scripts/test-data.sql"})
    void testGetCategoryById() throws Exception {
        String expected = "{\"id\":\"ee330a97-96ac-401c-97f2-e5d8d922f815\",\"createdBy\":\"renodesor\",\"createdOn\":\"2024-09-26T01:39:15.893\",\"updatedBy\":\"wisline.pl@gmail.com\",\"updatedOn\":\"2024-10-12T21:31:19.677\",\"name\":\"Lecture20\",\"description\":\"Lecture 20\"}";

        MockHttpServletResponse response = mvc
                .perform(get("/taam-api/categories/ee330a97-96ac-401c-97f2-e5d8d922f815"))
                .andReturn().getResponse();
        Assertions.assertEquals(200, response.getStatus(), "The api must return a succes code - Http status 200");
        Assertions.assertEquals(expected, response.getContentAsString(), "The api return a category");
    }

    @Test
    @DisplayName("Add a category to the database")
    void testAddACategory() throws Exception {
        String categoryInJson = "{\"id\":\"ee330a97-96ac-401c-97f2-e5d8d922f817\",\"name\":\"Lecture30\",\"description\":\"Lecture 30\",\"createdBy\":\"renodesor\",\"createdOn\":\"2024-09-26T01:39:15.893\"}";
        MockHttpServletResponse response = mvc
                .perform(post("/taam-api/categories")
                        .contentType("application/json")
                        .content(categoryInJson))
                .andReturn().getResponse();
        Category createdCategory = Utils.convertJsonToObject(response.getContentAsString(), Category.class);
        Assertions.assertEquals(201, response.getStatus());
        Assertions.assertEquals("Lecture30", createdCategory.getName());
        Assertions.assertEquals("Lecture 30", createdCategory.getDescription());
    }

    @Test
    @DisplayName("Put an existing category")
    @Sql({"classpath:db-scripts/test-data.sql"})
    void testPutCategory_categoryAlreadyExists() throws Exception {
        String categoryInJson = "{\"id\":\"ee330a97-96ac-401c-97f2-e5d8d922f815\",\"name\":\"Loisir\",\"description\":\"Loisir des jeunes\",\"createdBy\":\"renodesor\",\"createdOn\":\"2024-09-26T01:39:15.893\"}";
        MockHttpServletResponse response = mvc
                .perform(put("/taam-api/categories")
                        .contentType("application/json")
                        .content(categoryInJson))
                .andReturn().getResponse();
        Category createdCategory = Utils.convertJsonToObject(response.getContentAsString(), Category.class);
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("Loisir", createdCategory.getName());
        Assertions.assertEquals("Loisir des jeunes", createdCategory.getDescription());
        Assertions.assertNotNull(createdCategory.getUpdatedOn());
    }

    @Test
    @DisplayName("Put a new category")
    @Sql({"classpath:db-scripts/test-data.sql"})
    void testPutCategory_categoryNotExistsYet() throws Exception {
        String categoryInJson = "{\"id\":\"ee330a97-96ac-401c-97f2-e5d8d922f825\",\"name\":\"Loisir\",\"description\":\"Loisir des jeunes\",\"createdBy\":\"renodesor\",\"createdOn\":\"2024-09-26T01:39:15.893\"}";
        MockHttpServletResponse response = mvc
                .perform(put("/taam-api/categories")
                        .contentType("application/json")
                        .content(categoryInJson))
                .andReturn().getResponse();
        Category createdCategory = Utils.convertJsonToObject(response.getContentAsString(), Category.class);
        Assertions.assertEquals(201, response.getStatus());
        Assertions.assertEquals("Loisir", createdCategory.getName());
        Assertions.assertEquals("Loisir des jeunes", createdCategory.getDescription());
        Assertions.assertNull(createdCategory.getUpdatedOn());
    }
}
