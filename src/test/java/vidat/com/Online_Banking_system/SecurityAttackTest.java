package vidat.com.Online_Banking_system;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAttackTest {

    private static final String PASSWORD = "Password123!";

    @Autowired
    MockMvc mvc;

    private String aliceToken;
    private String malloryToken;
    private long aliceAccountId;
    private long malloryAccountId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String alice = "alice_" + suffix;
        String mallory = "mallory_" + suffix;

        register(alice);
        register(mallory);
        aliceToken = login(alice);
        malloryToken = login(mallory);
        aliceAccountId = createAccount(aliceToken, "100.00");
        malloryAccountId = createAccount(malloryToken, "0.00");
    }

    // ---------- 7.7: 401 handling ----------

    @Test
    void noTokenGets401Json() throws Exception {
        mvc.perform(get("/api/account/" + aliceAccountId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    void garbageTokenGets401() throws Exception {
        mvc.perform(get("/api/account/" + aliceAccountId)
                        .header("Authorization", "Bearer not.a.real.token"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- login ----------

    @Test
    void wrongPasswordGets401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody_" + UUID.randomUUID() + "\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    // ---------- 7.4: view only your own account ----------

    @Test
    void ownerCanReadOwnAccount() throws Exception {
        mvc.perform(get("/api/account/" + aliceAccountId)
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk());
    }

    @Test
    void foreignUserCannotReadAccount() throws Exception {
        mvc.perform(get("/api/account/" + aliceAccountId)
                        .header("Authorization", "Bearer " + malloryToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account not found"));
    }

    @Test
    void foreignUserCannotDeposit() throws Exception {
        mvc.perform(post("/api/account/" + aliceAccountId + "/deposite")
                        .header("Authorization", "Bearer " + malloryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":50.00}"))
                .andExpect(status().isNotFound());
    }

    // ---------- 7.6: history only for the owner ----------

    @Test
    void ownerCanReadOwnHistory() throws Exception {
        mvc.perform(get("/api/account/" + aliceAccountId + "/transfers")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk());
    }

    @Test
    void foreignUserCannotReadHistory() throws Exception {
        mvc.perform(get("/api/account/" + aliceAccountId + "/transfers")
                        .header("Authorization", "Bearer " + malloryToken))
                .andExpect(status().isNotFound());
    }

    // ---------- 7.5: transfer only from your own account ----------

    @Test
    void foreignUserCannotTransferFromAnotherAccount() throws Exception {
        mvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + malloryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(aliceAccountId, malloryAccountId, "10.00")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }

    @Test
    void ownerCanTransferFromOwnAccount() throws Exception {
        mvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(aliceAccountId, malloryAccountId, "10.00")))
                .andExpect(status().isCreated());
    }

    @Test
    void attackLeavesOwnersBalanceUnchanged() throws Exception {
        mvc.perform(post("/api/transfers")
                .header("Authorization", "Bearer " + malloryToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(transferJson(aliceAccountId, malloryAccountId, "100.00")));

        mvc.perform(get("/api/account/" + aliceAccountId)
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    // ---------- helpers ----------

    private void register(String username) throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());
    }

    private String login(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private long createAccount(String token, String initialDeposit) throws Exception {
        String body = mvc.perform(post("/api/account/creation")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currency\":\"KES\",\"initialDeposit\":" + initialDeposit + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private String transferJson(long from, long to, String amount) {
        return "{\"fromAccountId\":" + from + ",\"toAccountId\":" + to + ",\"amount\":" + amount + "}";
    }
}