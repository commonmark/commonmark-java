package com.atlassian.rstocker.cm.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.atlassian.rstocker.cm.spec.SpecExample;
import com.atlassian.rstocker.cm.spec.SpecReader;

@RunWith(Parameterized.class)
public class SpecTest extends RenderingTestCase {

	private final SpecExample example;

	@Parameters(name = "{0}")
	public static List<Object[]> data() throws Exception {
		InputStream stream = SpecTest.class.getResourceAsStream("/spec.txt");
		if (stream == null) {
			throw new IllegalStateException(
					"Could not load spec.txt classpath resource");
		}

		try (SpecReader reader = new SpecReader(stream)) {
			List<SpecExample> examples = reader.read();
			return examples.stream()
//					.filter(example -> example.toString().equals("Section \"Links\" example 1"))
					.map(example -> new Object[]{example})
					.collect(Collectors.toList());
		}
	}

	public SpecTest(SpecExample example) {
		this.example = example;
	}

	@Test
	public void testHtmlRendering() {
        assertRendering(example.getSource(), example.getHtml());
	}

}
