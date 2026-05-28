package com.ycyw.chat.chat_poc_backend.util;

import org.springframework.web.util.HtmlUtils;

public final class MessageSanitizer {

    private MessageSanitizer() {}

    /** Échappe le HTML pour prévenir XSS (OWASP). */
    public static String escapeForStorage(String raw) {
        if (raw == null) {
            return "";
        }
        return HtmlUtils.htmlEscape(raw.trim());
    }
}
