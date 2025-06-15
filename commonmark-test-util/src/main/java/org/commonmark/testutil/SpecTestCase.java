package org.commonmark.testutil;

import org.commonmark.testutil.example.Example;
import org.commonmark.testutil.example.ExampleReader;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

@ParameterizedClass
@MethodSource("data")
public abstract class SpecTestCase {

    @Parameter
    protected Example example;

    static List<Example> data() {
        return ExampleReader.readExamples(TestResources.getSpec());
    }
}
