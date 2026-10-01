package vn.edu.jwtjjwt.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.edu.jwtjjwt.user.UserAccount;
import vn.edu.jwtjjwt.user.UserRepository;
import vn.edu.jwtjjwt.security.ApiAccessDeniedHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:jwt_api_test;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "security.jwt.secret-key=test-secret-key-at-least-32-bytes-for-hs256",
    "security.jwt.expiration-time=3600000"
})
class BackendApiTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired ApiAccessDeniedHandler accessDeniedHandler;

    @BeforeEach void seedDemoAccount() {
        users.deleteAll();
        users.save(new UserAccount("demo", passwordEncoder.encode("JwtDemo123!"), "USER"));
    }

    @Test void registrationNormalizesUsernameAndUsesSuccessEnvelope() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"  Student_1 \",\"password\":\"StrongPass123\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").isNotEmpty())
            .andExpect(jsonPath("$.data.username").value("student_1"))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors.length()").value(0));
    }

    @Test void registrationRejectsMalformedUsernameWeakPasswordAndBcryptOversize() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"bad name!\",\"password\":\"weak\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
            .andExpect(jsonPath("$.errors").isArray());

        String tooManyUtf8Bytes = "é".repeat(37) + "a1";
        String body = objectMapper.writeValueAsString(new Credentials("student2", tooManyUtf8Bytes));
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test void registrationAcceptsUsernameAndPasswordAtTheirBoundaries() throws Exception {
        String username = "a".repeat(30);
        String password = "A1" + "x".repeat(70);
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Credentials(username, password))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.username").value(username));

        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Credentials("a".repeat(31), "StrongPass123"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].field").value("username"));

        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Credentials("shortpass", "A1" + "x".repeat(71)))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].field").value("password"));
    }

    @Test void loginDoesNotReapplyRegistrationPasswordStrengthPolicyOrTrimPassword() throws Exception {
        users.save(new UserAccount("legacy_user", passwordEncoder.encode("short1"), "USER"));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Credentials("legacy_user", "short1"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty());

        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Credentials("spaced_pass", "Abc12345 "))))
            .andExpect(status().isCreated());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Credentials("spaced_pass", "Abc12345 "))))
            .andExpect(status().isOk());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Credentials("spaced_pass", "Abc12345"))))
            .andExpect(status().isUnauthorized());
    }

    @Test void duplicateUsernameUsesConflictEnvelope() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\" DEMO \",\"password\":\"StrongPass123\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors[0].field").value("username"));
    }

    @Test void invalidCredentialsUseUnauthorizedEnvelope() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"demo\",\"password\":\"incorrect\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors[0].field").value("credentials"));
    }

    @Test void forbiddenResponsesUseTheCommonEnvelope() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        accessDeniedHandler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("forbidden"));
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        org.junit.jupiter.api.Assertions.assertEquals(403, response.getStatus());
        org.junit.jupiter.api.Assertions.assertFalse(body.path("success").asBoolean());
        org.junit.jupiter.api.Assertions.assertEquals("authorization", body.path("errors").get(0).path("field").asText());
    }

    @Test void protectedEndpointsRejectMissingAndInvalidTokensWithSameEnvelope() throws Exception {
        mvc.perform(get("/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        mvc.perform(get("/users/me").header("Authorization", "Bearer not-a-jwt"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors[0].field").value("authorization"));
    }

    @Test void loginTokenReadsProfileAndListInsideDataEnvelope() throws Exception {
        String login = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\" DEMO \",\"password\":\"JwtDemo123!\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
            .andReturn().getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(login);
        String token = body.path("data").path("token").asText();

        mvc.perform(get("/users/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("demo"));
        mvc.perform(get("/users").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].username").value("demo"));
    }

    private record Credentials(String username, String password) {}
}
