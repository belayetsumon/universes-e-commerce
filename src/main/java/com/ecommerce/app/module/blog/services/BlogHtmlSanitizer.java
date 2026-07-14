package com.ecommerce.app.module.blog.services;

import org.springframework.stereotype.Component;

@Component
public class BlogHtmlSanitizer {

    public String sanitize(String html) {
        if (html == null) {
            return "";
        }
        return html
                .replaceAll("(?is)<script.*?</script>", "")
                .replaceAll("(?is)<style.*?</style>", "")
                .replaceAll("(?i)\\son[a-z]+\\s*=\\s*\"[^\"]*\"", "")
                .replaceAll("(?i)\\son[a-z]+\\s*=\\s*'[^']*'", "")
                .replaceAll("(?i)javascript:", "");
    }
}
