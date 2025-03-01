package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.lnd.model.PoolLeaseFixtures.POOL_LEASE;
import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;
import static de.cotto.bitbook.ownership.OwnershipStatus.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoolLeasesServiceTest {
    private static final String DEFAULT_DESCRIPTION = "pool account";

    @InjectMocks
    private PoolLeasesService poolLeasesService;

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private TransactionService transactionService;

    private String channelAddress;
    private String changeAddress;

    @BeforeEach
    void setUp() {
        when(transactionService.getTransactionDetails(POOL_LEASE.getTransactionHash())).thenReturn(TRANSACTION);
        lenient().when(addressOwnershipService.getOwnershipStatus(INPUT_ADDRESS_1)).thenReturn(UNKNOWN);
        lenient().when(addressOwnershipService.getOwnershipStatus(INPUT_ADDRESS_2)).thenReturn(OWNED);
        lenient().when(addressDescriptionService.getDescription(any())).thenReturn("");
        channelAddress = OUTPUT_ADDRESS_1;
        changeAddress = OUTPUT_ADDRESS_2;
    }

    @Test
    void success() {
        assertThat(poolLeasesService.addFromLeases(Set.of(POOL_LEASE))).isEqualTo(1);
    }

    @Test
    void sets_transaction_description() {
        poolLeasesService.addFromLeases(Set.of(POOL_LEASE));
        verify(transactionDescriptionService).set(
                POOL_LEASE.getTransactionHash(),
                "Opening Channel with " + POOL_LEASE.getPubKey()
        );
    }

    @Test
    void sets_channel_address_description() {
        poolLeasesService.addFromLeases(Set.of(POOL_LEASE));

        String channelDescription = "Lightning Channel with " + POOL_LEASE.getPubKey();
        verify(addressDescriptionService).set(channelAddress, channelDescription);
        verify(addressDescriptionService, never()).set(changeAddress, channelDescription);
    }

    @Test
    void marks_channel_address_as_owned() {
        poolLeasesService.addFromLeases(Set.of(POOL_LEASE));
        verify(addressOwnershipService).setAddressAsOwned(channelAddress);
    }

    @Test
    void sets_description_for_change_address() {
        poolLeasesService.addFromLeases(Set.of(POOL_LEASE));
        verify(addressDescriptionService).set(changeAddress, DEFAULT_DESCRIPTION);
        verify(addressDescriptionService, never()).set(channelAddress, DEFAULT_DESCRIPTION);
    }

    @Test
    void sets_description_for_change_address_description_from_owned_input() {
        String description = "pool account 123";
        when(addressDescriptionService.getDescription(INPUT_ADDRESS_2)).thenReturn(description);
        poolLeasesService.addFromLeases(Set.of(POOL_LEASE));
        verify(addressDescriptionService).set(changeAddress, description);
    }

    @Test
    void marks_change_address_as_owned() {
        poolLeasesService.addFromLeases(Set.of(POOL_LEASE));
        verify(addressOwnershipService).setAddressAsOwned(changeAddress);
    }

    @Test
    void unknown_transaction_details() {
        when(transactionService.getTransactionDetails(POOL_LEASE.getTransactionHash())).thenReturn(Transaction.UNKNOWN);
        assertThat(poolLeasesService.addFromLeases(Set.of(POOL_LEASE))).isEqualTo(0);
    }
}