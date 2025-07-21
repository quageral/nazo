
package com.nazo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

public class WordleControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  private Map<String, String> startRequest;
  private Map<String, String> guessRequest;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new WordleController()).build();
    objectMapper = new ObjectMapper();

    startRequest = new HashMap<>();
    startRequest.put("username", "testUser");

    guessRequest = new HashMap<>();
    guessRequest.put("guess", "HELLO");
  }

  @Test

  @DisplayName("开始游戏 - 成功创建会话")
  void startWordle_Success() throws Exception {
    mockMvc.perform(post("/api/wordle/start")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(startRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.sessionId").isNotEmpty())
        .andExpect(jsonPath("$.message").value("游戏会话已创建"));
  }

  @Test

  @DisplayName("开始游戏 - 缺少用户名")
  void startWordle_MissingUsername() throws Exception {
    startRequest.remove("username");

    mockMvc.perform(post("/api/wordle/start")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(startRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.sessionId").isNotEmpty());
  }

  @Test

  @DisplayName("猜测单词 - 会话不存在")
  void guessWordle_SessionNotFound() throws Exception {
    guessRequest.put("sessionId", "invalid-session-id");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("会话不存在"));
  }

  @Test

  @DisplayName("猜测单词 - 单词长度不是5")
  void guessWordle_InvalidLength_TooShort() throws Exception {
    // 先创建会话
    String sessionId = createGameSession();

    guessRequest.put("sessionId", sessionId);
    guessRequest.put("guess", "HI");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("猜测的单词长度必须为5"));
  }

  @Test

  @DisplayName("猜测单词 - 单词长度不是5 (太长)")
  void guessWordle_InvalidLength_TooLong() throws Exception {
    String sessionId = createGameSession();

    guessRequest.put("sessionId", sessionId);
    guessRequest.put("guess", "HELLOS");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("猜测的单词长度必须为5"));
  }

  @Test

  @DisplayName("猜测单词 - 包含非字母字符")
  void guessWordle_NonAlphabetic() throws Exception {
    String sessionId = createGameSession();

    guessRequest.put("sessionId", sessionId);
    guessRequest.put("guess", "HEL12");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("猜测的单词必须为5个字母"));
  }

  @Test

  @DisplayName("猜测单词 - 包含特殊字符")
  void guessWordle_SpecialCharacters() throws Exception {
    String sessionId = createGameSession();

    guessRequest.put("sessionId", sessionId);
    guessRequest.put("guess", "HEL@!");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("猜测的单词必须为5个字母"));
  }

  @Test

  @DisplayName("猜测单词 - 大小写混合")
  void guessWordle_MixedCase() throws Exception {
    String sessionId = createGameSession();

    guessRequest.put("sessionId", sessionId);
    guessRequest.put("guess", "Hello");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.result").isString())
        .andExpect(jsonPath("$.guessCount").value(1))
        .andExpect(jsonPath("$.answer").isString());
  }

  @Test

  @DisplayName("猜测单词 - 成功猜测")
  void guessWordle_Success() throws Exception {
    String sessionId = createGameSession();

    guessRequest.put("sessionId", sessionId);
    guessRequest.put("guess", "HELLO");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.result").isString())
        .andExpect(jsonPath("$.result").value(matchesPattern("[+x-]{5}")))
        .andExpect(jsonPath("$.guessCount").value(1))
        .andExpect(jsonPath("$.answer").isString());
  }

  @Test

  @DisplayName("猜测单词 - 多次猜测")
  void guessWordle_MultipleGuesses() throws Exception {
    String sessionId = createGameSession();

    // 第一次猜测
    guessRequest.put("sessionId", sessionId);
    guessRequest.put("guess", "HELLO");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.guessCount").value(1));

    // 第二次猜测
    guessRequest.put("guess", "WORLD");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.guessCount").value(2));
  }

  @Test

  @DisplayName("猜测单词 - 超过6次猜测限制")
  void guessWordle_ExceedGuessLimit() throws Exception {
    String sessionId = createGameSession();

    // 进行6次猜测
    for (int i = 1; i <= 6; i++) {
      guessRequest.put("sessionId", sessionId);
      guessRequest.put("guess", "GUESS");

      mockMvc.perform(post("/api/wordle/guess")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(guessRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.guessCount").value(i));
    }

    // 第7次猜测应该被拒绝
    guessRequest.put("guess", "FINAL");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("猜测次数已用完"));
  }

  @Test

  @DisplayName("结果检查 - 完全匹配")
  void checkGuess_PerfectMatch() throws Exception {
    String sessionId = createGameSession();
    String answer = getGameAnswer(sessionId);

    guessRequest.put("sessionId", sessionId);
    guessRequest.put("guess", answer);

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("+++++"));
  }

  @Test

  @DisplayName("结果检查 - 无匹配")
  void checkGuess_NoMatch() throws Exception {
    String sessionId = createGameSession();

    // 使用一个不太可能匹配的单词
    guessRequest.put("sessionId", sessionId);
    guessRequest.put("guess", "ZZZZZ");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("猜测的单词不在词库中"));
  }

  @Test

  @DisplayName("跨域支持 - CORS预检请求")
  void corsSupport() throws Exception {
    mockMvc.perform(post("/api/wordle/start")
        .header("Origin", "http://localhost:3000")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(startRequest)))
        .andExpect(status().isOk());
  }

  @Test

  @DisplayName("并发会话测试")
  void concurrentSessions() throws Exception {
    // 创建多个会话
    String sessionId1 = createGameSession("user1");
    String sessionId2 = createGameSession("user2");

    // 确保会话ID不同
    assert !sessionId1.equals(sessionId2);

    // 测试两个会话都能正常工作
    guessRequest.put("sessionId", sessionId1);
    guessRequest.put("guess", "HELLO");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    guessRequest.put("sessionId", sessionId2);
    guessRequest.put("guess", "WORLD");

    mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test

  @DisplayName("JSON格式错误处理")
  void invalidJsonRequest() throws Exception {
    mockMvc.perform(post("/api/wordle/start")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{invalid json"))
        .andExpect(status().isBadRequest());
  }

  // 辅助方法：创建游戏会话
  private String createGameSession() throws Exception {
    return createGameSession("testUser");
  }

  private String createGameSession(String username) throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("username", username);

    String response = mockMvc.perform(post("/api/wordle/start")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> responseMap = mapper.readValue(response, Map.class);
    return (String) responseMap.get("sessionId");
  }

  // 辅助方法：获取游戏答案（用于测试完全匹配）
  private String getGameAnswer(String sessionId) throws Exception {
    guessRequest.put("sessionId", sessionId);
    guessRequest.put("guess", "DUMMY");

    String response = mockMvc.perform(post("/api/wordle/guess")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(guessRequest)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> responseMap = mapper.readValue(response, Map.class);
    return (String) responseMap.get("answer");
  }
}