package com.ecommerce.app.module.communication.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.model.ProviderType;
import org.junit.jupiter.api.Test;

class MessageProviderFormSecurityTest {

    @Test
    void editFormDoesNotExposeSecretsAndBlankSubmissionPreservesThem() {
        MessageProvider provider = providerWithSecrets();

        MessageProviderForm form = MessageProviderForm.fromProvider(provider);

        assertNull(form.getApiKey());
        assertNull(form.getApiSecret());
        assertNull(form.getConfigJson());
        assertTrue(form.isApiKeyConfigured());
        assertTrue(form.isApiSecretConfigured());
        assertTrue(form.isConfigJsonConfigured());

        form.applyTo(provider);

        assertEquals("existing-key", provider.getApiKey());
        assertEquals("existing-secret", provider.getApiSecret());
        assertEquals("{\"privateToken\":\"existing-token\"}", provider.getConfigJson());
    }

    @Test
    void credentialsCanOnlyBeReplacedOrExplicitlyCleared() {
        MessageProvider provider = providerWithSecrets();
        MessageProviderForm replacement = MessageProviderForm.fromProvider(provider);
        replacement.setApiKey(" replacement-key ");
        replacement.setApiSecret(" replacement-secret ");
        replacement.setConfigJson(" {\"region\":\"bd\"} ");

        replacement.applyTo(provider);

        assertEquals("replacement-key", provider.getApiKey());
        assertEquals("replacement-secret", provider.getApiSecret());
        assertEquals("{\"region\":\"bd\"}", provider.getConfigJson());

        MessageProviderForm clearing = MessageProviderForm.fromProvider(provider);
        clearing.setClearApiKey(true);
        clearing.setClearApiSecret(true);
        clearing.setClearConfigJson(true);
        clearing.applyTo(provider);

        assertNull(provider.getApiKey());
        assertNull(provider.getApiSecret());
        assertNull(provider.getConfigJson());
    }

    private MessageProvider providerWithSecrets() {
        MessageProvider provider = new MessageProvider();
        provider.setProviderName("Production SMS");
        provider.setChannel(MessageChannel.SMS);
        provider.setProviderType(ProviderType.SMS_GATEWAY);
        provider.setStatus(MessageStatus.ACTIVE);
        provider.setPriority(10);
        provider.setApiKey("existing-key");
        provider.setApiSecret("existing-secret");
        provider.setConfigJson("{\"privateToken\":\"existing-token\"}");
        return provider;
    }
}
