package org.commonmark.testutil;

import java.util.List;
import org.commonmark.testutil.example.Example;
import org.commonmark.testutil.example.ExampleReader;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

@ParameterizedClass
@MethodSource("data")
public abstract class SpecTestCase {

    @Parameter protected Example example;

    static List<Example> data() {
        return ExampleReader.readExamples(TestResources.getSpec());
    }
}
