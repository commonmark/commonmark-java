package org.commonmark.integration;

// Prevent maven-gpg-plugin from failing with this error:
//     The project artifact has not been assembled yet.
//     Please do not invoke this goal before the lifecycle phase "package".
//
// Apparently it doesn't like a module that doesn't have any classes in main,
// because that means no jar is generated.
// And the javadoc plugin doesn't like if there's no classes with documentation,
//

/**
 * Module with integration tests.
 */
public class IntegrationTests {
}
