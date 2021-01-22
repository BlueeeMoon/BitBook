package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.AddressWithDescription;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressWithDescriptionTest {
    private static final String TOO_LONG =
            "abcaaaaaaaaaaaaaaaXaaaaaaaaaaaaaaaaaaaaaaaaXIIIIIIIIIIIIIIIIIZ";
    private static final String SHORTENED_45 = "abcaaaaaaaaaaaaaaaXaaaaaaaaaaaaaaaaaaaaaaaaX…";
    private static final String SHORTENED_20 = "abcaaaaaaaaaaaaaaaX…";
    private static final AddressWithDescription ADDRESS_WITH_DESCRIPTION =
            create("x", "y");

    @Test
    void getAddress() {
        assertThat(ADDRESS_WITH_DESCRIPTION.getAddress()).isEqualTo("x");
    }

    @Test
    void getDescription() {
        assertThat(ADDRESS_WITH_DESCRIPTION.getDescription()).isEqualTo("y");
    }

    @Test
    void compareTo_smaller_description() {
        assertThat(create("z", "a").compareTo(create("a", "z"))).isLessThan(0);
    }

    @Test
    void compareTo_same_description_smaller_address() {
        assertThat(create("a", "y").compareTo(create("z", "y"))).isLessThan(0);
    }

    @Test
    void compareTo_same_description_same_address() {
        assertThat(create("x", "y").compareTo(ADDRESS_WITH_DESCRIPTION)).isEqualTo(0);
    }

    @Test
    void compareTo_same_description_larger_address() {
        assertThat(create("z", "y").compareTo(create("a", "y"))).isGreaterThan(0);
    }

    @Test
    void compareTo_larger_description() {
        assertThat(create("a", "z").compareTo(create("z", "a"))).isGreaterThan(0);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(AddressWithDescription.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        String formattedAddress = StringUtils.leftPad(ADDRESS_WITH_DESCRIPTION.getAddress(), 45);
        String formattedDescription = StringUtils.leftPad(ADDRESS_WITH_DESCRIPTION.getDescription(), 20);
        assertThat(ADDRESS_WITH_DESCRIPTION).hasToString(formattedAddress + " " + formattedDescription);
    }

    @Test
    void testToString_long_address() {
        AddressWithDescription addressWithDescription = new AddressWithDescription(
                TOO_LONG,
                ADDRESS_WITH_DESCRIPTION.getDescription()
        );
        assertThat(addressWithDescription).hasToString(
                SHORTENED_45 +
                " " +
                StringUtils.leftPad(addressWithDescription.getDescription(), 20)
        );
    }

    @Test
    void testToString_long_description() {
        AddressWithDescription addressWithDescription = new AddressWithDescription(
                ADDRESS_WITH_DESCRIPTION.getAddress(),
                TOO_LONG
        );
        assertThat(addressWithDescription).hasToString(
                StringUtils.leftPad(addressWithDescription.getAddress(), 45) +
                " " +
                SHORTENED_20
        );
    }

    @Test
    void testToString_without_description() {
        assertThat(new AddressWithDescription("x")).hasToString(
                StringUtils.leftPad("x", 45) + " " + StringUtils.leftPad("", 20)
        );
    }

    @Test
    void getDescription_without_description() {
        assertThat(new AddressWithDescription("x").getDescription()).isEqualTo("");
    }

    @Test
    void getFormattedAddress() {
        assertThat(ADDRESS_WITH_DESCRIPTION.getFormattedAddress())
                .isEqualTo(StringUtils.leftPad(ADDRESS_WITH_DESCRIPTION.getAddress(), 45));
    }

    @Test
    void getFormattedAddress_long() {
        assertThat(new AddressWithDescription(
                TOO_LONG,
                ADDRESS_WITH_DESCRIPTION.getDescription()
        ).getFormattedAddress()).isEqualTo(SHORTENED_45);
    }

    @Test
    void getFormattedDescription() {
        assertThat(ADDRESS_WITH_DESCRIPTION.getFormattedDescription())
                .isEqualTo(StringUtils.leftPad(ADDRESS_WITH_DESCRIPTION.getDescription(), 20));
    }

    @Test
    void getFormattedDescription_long() {
        assertThat(new AddressWithDescription(
                ADDRESS_WITH_DESCRIPTION.getAddress(),
                TOO_LONG
        ).getFormattedDescription()).isEqualTo(SHORTENED_20);
    }

    @Test
    void getFormattedWithInfix() {
        assertThat(ADDRESS_WITH_DESCRIPTION.getFormattedWithInfix(Test.class)).isEqualTo(
                ADDRESS_WITH_DESCRIPTION.getFormattedAddress() +
                " interface org.junit.jupiter.api.Test " +
                ADDRESS_WITH_DESCRIPTION.getFormattedDescription()
        );
    }

    private static AddressWithDescription create(String address, String description) {
        return new AddressWithDescription(address, description);
    }
}