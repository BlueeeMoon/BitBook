package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.TransactionWithDescription;
import de.cotto.bitbook.backend.transaction.TransactionCompletionDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionHashCompletionProviderTest {
    private static final String INPUT = "abc";

    private final String[] hints = new String[0];

    @InjectMocks
    private TransactionHashCompletionProvider completionProvider;

    @Mock
    private TransactionCompletionDao transactionCompletionDao;

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @Mock
    private CompletionContext context;

    @Mock
    private MethodParameter methodParameter;

    @BeforeEach
    void setUp() {
        when(context.currentWordUpToCursor()).thenReturn(INPUT);
    }

    @Test
    void complete() {
        mockCompletionForTransaction(Set.of(TRANSACTION_HASH));
        mockCompletionForAdressTransactionHash(Set.of(TRANSACTION_HASH_2));
        assertProposalsForHashes(TRANSACTION_HASH_2, TRANSACTION_HASH);
    }

    @Test
    void complete_no_duplicates() {
        mockCompletionForTransaction(Set.of(TRANSACTION_HASH));
        mockCompletionForAdressTransactionHash(Set.of(TRANSACTION_HASH));
        assertProposalsForHashes(TRANSACTION_HASH);
    }

    @Test
    void complete_from_transaction_details() {
        mockCompletionForTransaction(Set.of(TRANSACTION_HASH));
        assertProposalsForHashes(TRANSACTION_HASH);
    }

    @Test
    void complete_from_address_transaction_hashes() {
        mockCompletionForAdressTransactionHash(Set.of(TRANSACTION_HASH));
        assertProposalsForHashes(TRANSACTION_HASH);
    }

    @Test
    void complete_from_description() {
        String description = "xxxyyy";
        when(transactionDescriptionService.getWithDescriptionInfix(INPUT))
                .thenReturn(Set.of(new TransactionWithDescription(TRANSACTION_HASH, description)));

        List<CompletionProposal> proposals = completionProvider.complete(methodParameter, context, hints);

        assertThat(proposals).usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new CompletionProposal(hashWithAnsiDescription(description)));
    }

    @Test
    void does_not_complete_short_hash() {
        when(context.currentWordUpToCursor()).thenReturn("ab");
        assertThat(completionProvider.complete(methodParameter, context, hints)).isEmpty();
        verifyNoInteractions(transactionCompletionDao);
    }

    private void mockCompletionForTransaction(Set<String> hashes) {
        when(transactionDescriptionService.get(any()))
                .then(invocation -> new TransactionWithDescription(invocation.getArgument(0)));
        when(transactionCompletionDao.completeFromTransactionDetails(INPUT)).thenReturn(hashes);
    }

    private void mockCompletionForAdressTransactionHash(Set<String> transactionHash2) {
        when(transactionDescriptionService.get(any()))
                .then(invocation -> new TransactionWithDescription(invocation.getArgument(0)));
        when(transactionCompletionDao.completeFromAddressTransactionHashes(INPUT))
                .thenReturn(transactionHash2);
    }

    private void assertProposalsForHashes(String... transactionHashes) {
        List<CompletionProposal> proposals = completionProvider.complete(methodParameter, context, hints);
        List<CompletionProposal> completionProposals =
                Arrays.stream(transactionHashes).map(CompletionProposal::new).collect(Collectors.toList());
        assertThat(proposals).usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(completionProposals);
    }

    private String hashWithAnsiDescription(String description) {
        return TRANSACTION_HASH
               + "\u00a0("
               + AnsiOutput.toString(AnsiColor.BRIGHT_BLACK, description, AnsiColor.DEFAULT)
               + ")";
    }
}