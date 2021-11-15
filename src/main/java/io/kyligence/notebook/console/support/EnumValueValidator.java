package io.kyligence.notebook.console.support;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumValueValidator implements ConstraintValidator<EnumValid, CharSequence> {
    private Set<String> acceptedValues;
    private Boolean ignoreEmpty;
    private Boolean nullable;
    @Override
    public void initialize(EnumValid annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
        ignoreEmpty = annotation.ignoreEmpty();
        nullable = annotation.nullable();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) return nullable;
        return (ignoreEmpty && value.length() == 0) || acceptedValues.contains(value.toString().toUpperCase());
    }
}
