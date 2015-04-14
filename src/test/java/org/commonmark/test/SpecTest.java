package org.commonmark.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.commonmark.spec.SpecExample;
import org.commonmark.spec.SpecReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

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
			List<Object[]> data = new ArrayList<>();
			for (SpecExample example : examples) {
				data.add(new Object[] { example });
			}
			return data;
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
