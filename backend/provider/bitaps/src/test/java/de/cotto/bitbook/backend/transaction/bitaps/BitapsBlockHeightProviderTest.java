package de.cotto.bitbook.backend.transaction.bitaps;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BitapsBlockHeightProviderTest {

    @InjectMocks
    private BitapsBlockHeightProvider provider;

    @Mock
    private BitapsClient bitapsClient;

    @Test
    void getBlockHeight() {
        when(bitapsClient.getBlockHeight()).thenReturn(Optional.of(new BitapsBlockHeightDto(BLOCK_HEIGHT)));
        assertThat(provider.get()).contains(BLOCK_HEIGHT);
    }

    @Test
    void get_with_argument() {
        when(bitapsClient.getBlockHeight()).thenReturn(Optional.of(new BitapsBlockHeightDto(BLOCK_HEIGHT)));
        assertThat(provider.get("x")).contains(BLOCK_HEIGHT);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BitapsBlockHeightProvider");
    }
}