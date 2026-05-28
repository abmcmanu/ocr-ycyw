package com.ycyw.chat.chat_poc_backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageSanitizerTest {

    @Test
    void testEscapeHtml() {
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", MessageSanitizer.escapeForStorage("<script>alert(1)</script>"));
    }

    @Test
    void testNull() {
        assertEquals("", MessageSanitizer.escapeForStorage(null));
    }

    @Test
    void testNormalText() {
        assertEquals("Hello world", MessageSanitizer.escapeForStorage("  Hello world  "));
    }
}
