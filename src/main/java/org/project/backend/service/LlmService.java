package org.project.backend.service;

import org.project.backend.service.assist.LlmProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LlmService {

    private final Map<String, LlmProvider> llmProviders;

    @Autowired
    public LlmService(Map<String, LlmProvider> llmProviders) {
        this.llmProviders = llmProviders;
    }

    /**
     * Tạo nội dung dựa trên prompt và provider được chọn
     * @param prompt Yêu cầu của người dùng
     * @param providerName Tên provider (geminiLlm, openaiLlm, etc.)
     * @return Nội dung được tạo bởi AI
     * @throws Exception Nếu có lỗi xảy ra
     */
    public String generateContent(String prompt, String providerName) throws Exception {
        LlmProvider provider = llmProviders.get(providerName);

        if (provider == null) {
            throw new IllegalArgumentException("Provider không tồn tại: " + providerName);
        }

        return provider.generateContent(prompt);
    }

    /**
     * Lấy danh sách tất cả providers có sẵn
     * @return Map của provider names
     */
    public Map<String, LlmProvider> getAvailableProviders() {
        return llmProviders;
    }
}