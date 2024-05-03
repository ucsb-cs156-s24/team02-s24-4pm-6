package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Help;
import edu.ucsb.cs156.example.repositories.HelpRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = HelpController.class)
@Import(TestConfig.class)
public class HelpControllerTests extends ControllerTestCase {

        @MockBean
        HelpRepository helpRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/ucsbdates/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/help/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/help/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_help() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Help help1 = Help.builder()
                                .requesterEmail("ibareket@ucsb.edu")
                                .teamId("20222")
                                .tableOrBreakoutRoom("table")
                                .requestTime(ldt1)
                                .explanation("please")
                                .solved(false)
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-01-03T00:00:00");

                Help help2 = Help.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .teamId("20222")
                                .tableOrBreakoutRoom("6")
                                .requestTime(ldt2)
                                .explanation("N/A")
                                .solved(true)
                                .build();

                ArrayList<Help> expectedHelp = new ArrayList<>();
                expectedHelp.addAll(Arrays.asList(help1, help2));

                when(helpRepository.findAll()).thenReturn(expectedHelp);

                // act
                MvcResult response = mockMvc.perform(get("/api/help/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(helpRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedHelp);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/ucsbdates/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/help/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/help/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_help() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Help help1 = Help.builder()
                                .requesterEmail("ibareket@ucsb.edu")
                                .teamId("20222")
                                .tableOrBreakoutRoom("table")
                                .requestTime(ldt1)
                                .explanation("please")
                                .solved(true)
                                .build();

                when(helpRepository.save(eq(help1))).thenReturn(help1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/help/post?requesterEmail=ibareket@ucsb.edu&teamId=20222&tableOrBreakoutRoom=table&requestTime=2022-01-03T00:00:00&explanation=please&solved=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRepository, times(1)).save(help1);
                String expectedJson = mapper.writeValueAsString(help1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/help?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/help?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Help help = Help.builder()
                                .requesterEmail("ibareket@ucsb.edu")
                                .teamId("20222")
                                .tableOrBreakoutRoom("table")
                                .requestTime(ldt1)
                                .explanation("please")
                                .solved(false)
                                .build();

                when(helpRepository.findById(eq(1L))).thenReturn(Optional.of(help));

                // act
                MvcResult response = mockMvc.perform(get("/api/help?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(helpRepository, times(1)).findById(eq(1L));
                String expectedJson = mapper.writeValueAsString(help);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_logged_in_user_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(helpRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/help?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(helpRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Help with id 7 not found", json.get("message"));
        }


        // Tests for DELETE /api/ucsbdates?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_help() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Help help1 = Help.builder()
                                .requesterEmail("ibareket@ucsb.edu")
                                .teamId("20222")
                                .tableOrBreakoutRoom("table")
                                .requestTime(ldt1)
                                .explanation("please")
                                .solved(false)
                                .build();

                when(helpRepository.findById(eq(1L))).thenReturn(Optional.of(help1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/help?id=1")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRepository, times(1)).findById(1L);
                verify(helpRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Help request with id 1 deleted", json.get("message"));
        }
        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_help_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(helpRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/help?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(helpRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Help with id 15 not found", json.get("message"));
        }

        // Tests for PUT /api/ucsbdates?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_help() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

                Help helpOrig = Help.builder()
                                .requesterEmail("ibareket@ucsb.edu")
                                .teamId("20222")
                                .tableOrBreakoutRoom("table")
                                .requestTime(ldt1)
                                .explanation("please")
                                .solved(false)
                                .build();

                Help helpEdited = Help.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .teamId("20223")
                                .tableOrBreakoutRoom("7")
                                .requestTime(ldt2)
                                .explanation("N/A")
                                .solved(true)
                                .build();

                String requestBody = mapper.writeValueAsString(helpEdited);

                when(helpRepository.findById(eq(1L))).thenReturn(Optional.of(helpOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/help?id=1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRepository, times(1)).findById(1L);
                verify(helpRepository, times(1)).save(helpEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_help_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Help help1 = Help.builder()
                                .requesterEmail("ibareket@ucsb.edu")
                                .teamId("20222")
                                .tableOrBreakoutRoom("table")
                                .requestTime(ldt1)
                                .explanation("please")
                                .solved(true)
                                .build();

                String requestBody = mapper.writeValueAsString(help1);

                when(helpRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/help?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(helpRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Help with id 67 not found", json.get("message"));

        }
}
