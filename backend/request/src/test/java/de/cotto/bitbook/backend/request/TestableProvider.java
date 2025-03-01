package de.cotto.bitbook.backend.request;

import de.cotto.bitbook.backend.Provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class TestableProvider implements Provider<String, Integer> {
    protected final List<String> seenKeys = Collections.synchronizedList(new ArrayList<>());

    public TestableProvider() {
        // default constructor
    }

    @Override
    public Optional<Integer> get(String key) {
        seenKeys.add(key);
        if (key == null) {
            return Optional.empty();
        }
        if ("".equals(key)) {
            return Optional.empty();
        }
        if ("wait".equals(key)) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return Optional.of(key.length());
    }

    @Override
    public String getName() {
        return "TestableProvider";
    }
}
