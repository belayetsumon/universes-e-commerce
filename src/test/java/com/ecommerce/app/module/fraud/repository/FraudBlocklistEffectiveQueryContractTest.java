package com.ecommerce.app.module.fraud.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.app.module.fraud.model.FraudBlockType;
import java.time.LocalDateTime;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

class FraudBlocklistEffectiveQueryContractTest {

    @Test
    void effectiveLookupExcludesExpiredTemporaryBlocksAtTheDatabaseBoundary() throws Exception {
        Query query = FraudBlocklistRepository.class
                .getMethod(
                        "existsEffectiveBlock",
                        FraudBlockType.class,
                        Collection.class,
                        LocalDateTime.class
                )
                .getAnnotation(Query.class);
        String value = query.value().replaceAll("\\s+", " ").toLowerCase();

        assertTrue(value.contains("block.active = true"));
        assertTrue(value.contains("block.hashedvalue in :hashedvalues"));
        assertTrue(value.contains("fraudblockscope.global"));
        assertTrue(value.contains("block.temporary = false"));
        assertTrue(value.contains("block.expiresat is null"));
        assertTrue(value.contains("block.expiresat > :now"));
    }
}
