package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.repository.MessageProviderRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageProviderService {

    private final MessageProviderRepository repository;

    public MessageProviderService(MessageProviderRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<MessageProvider> findActiveProvider(MessageChannel channel) {
        return findActiveProviders(channel).stream().findFirst();
    }

    @Transactional(readOnly = true)
    public List<MessageProvider> findActiveProviders(MessageChannel channel) {
        return repository.findByChannelAndStatusOrderByPriorityAscIdAsc(channel, MessageStatus.ACTIVE);
    }
}
