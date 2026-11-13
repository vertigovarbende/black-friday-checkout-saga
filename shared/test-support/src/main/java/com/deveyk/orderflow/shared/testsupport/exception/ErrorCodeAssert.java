package com.deveyk.orderflow.shared.testsupport.exception;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import org.assertj.core.api.SoftAssertions;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class ErrorCodeAssert {

    private static final Pattern PREFIX_FORMAT = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern CODE_FORMAT = Pattern.compile("^[A-Z]{3}_\\d{3}$");
    private static final Pattern LONE_APOSTROPHE = Pattern.compile("(?<!')'(?!')");

    private ErrorCodeAssert() {
    }

    public static <E extends Enum<E> & ErrorCode> void assertValid(final Class<E> errorCodeType, final String prefix) {

        if (prefix == null || !PREFIX_FORMAT.matcher(prefix).matches()) {
            throw new IllegalArgumentException("Prefix must be three upper-case letters, was: " + prefix);
        }

        final E[] constants = errorCodeType.getEnumConstants();
        final Set<String> seenCodes = new HashSet<>();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(constants).as("%s has no constants", errorCodeType.getSimpleName()).isNotEmpty();

            for (final E constant : constants) {
                final String name = errorCodeType.getSimpleName() + "." + constant.name();
                final String code = constant.code();
                final String message = constant.defaultMessage();

                softly.assertThat(code).as("%s code", name).matches(CODE_FORMAT).startsWith(prefix + "_");
                softly.assertThat(seenCodes.add(code)).as("%s code %s is used more than once", name, code).isTrue();
                softly.assertThat(constant.header()).as("%s header", name).isNotBlank();
                softly.assertThat(message).as("%s defaultMessage", name).isNotBlank();

                if (message != null) {
                    softly.assertThatCode(() -> new MessageFormat(message))
                            .as("%s defaultMessage is a valid MessageFormat pattern", name)
                            .doesNotThrowAnyException();

                    softly.assertThat(LONE_APOSTROPHE.matcher(message).find())
                            .as("%s defaultMessage has a single apostrophe; write it as '' (MessageFormat)", name)
                            .isFalse();

                }
            }
        });

    }
}
