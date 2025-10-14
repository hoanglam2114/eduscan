// package org.project.backend.service.llm;

package org.project.backend.service.assist;
public interface LlmProvider {
    /**
     * Tạo nội dung dựa trên một yêu cầu (prompt).
     * @param prompt Yêu cầu của người dùng.
     * @return Nội dung đã được AI tạo.
     */
    String generateContent(String prompt) throws Exception;

    String getProviderName();
}