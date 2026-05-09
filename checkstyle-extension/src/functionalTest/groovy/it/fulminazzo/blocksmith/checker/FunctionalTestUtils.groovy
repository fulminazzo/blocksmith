package it.fulminazzo.blocksmith.checker

import com.puppycrawl.tools.checkstyle.Checker
import com.puppycrawl.tools.checkstyle.DefaultConfiguration
import com.puppycrawl.tools.checkstyle.api.AbstractCheck
import com.puppycrawl.tools.checkstyle.api.AuditEvent
import com.puppycrawl.tools.checkstyle.api.AuditListener
import it.fulminazzo.blocksmith.checker.validator.RankerValidator

import java.util.function.Function

/**
 * A collection of utilities for functional tests.
 */
final class FunctionalTestUtils {

    /**
     * Executes the checkstyle checker and returns a list of violations derived from the given configuration.
     *
     * @param inputFile the input file to check
     * @param checkType the type of the configuration
     * @return the violations
     */
    static List<AuditEvent> runCheck(final String inputFile, final Class<? extends AbstractCheck> checkType) {
        return runCheck(inputFile, checkType, null)
    }

    /**
     * Executes the checkstyle checker and returns a list of violations derived from the given configuration.
     *
     * @param inputFile the input file to check
     * @param checkType the type of the configuration
     * @param linesTransformer a function to parse placeholders in the file lines (can be null)
     * @return the violations
     */
    static List<AuditEvent> runCheck(
            final String inputFile,
            final Class<? extends AbstractCheck> checkType,
            final Function<String, String> linesTransformer
    ) {
        def checkConfig = new DefaultConfiguration(checkType.name)

        def treeWalker = new DefaultConfiguration('TreeWalker')
        treeWalker.addChild(checkConfig)

        def rootConfig = new DefaultConfiguration('Checker')
        rootConfig.addChild(treeWalker)

        def checker = new Checker()
        checker.setModuleClassLoader(Thread.currentThread().contextClassLoader)
        checker.configure(rootConfig)

        def violations = []
        checker.addListener(new AuditListener() {
            @Override
            void auditStarted(final AuditEvent event) {

            }

            @Override
            void auditFinished(final AuditEvent event) {

            }

            @Override
            void fileStarted(final AuditEvent event) {

            }

            @Override
            void fileFinished(final AuditEvent event) {

            }

            @Override
            void addError(final AuditEvent event) {
                violations << event
            }

            @Override
            void addException(final AuditEvent event, final Throwable throwable) {

            }
        })


        def resourceName = "/${checkType.packageName.replace('.', '/')}/${inputFile}.java"
        def file = new File(checkType.getResource(resourceName).toURI())
        def original = file.readLines()
        def lines = file.readLines()
        if (linesTransformer != null) {
            file.delete()
            file << lines.collect { linesTransformer(it) }.join('\n')
        }
        checker.process([file])
        file.delete()
        file << original.join('\n')
        printViolations(violations)
        return violations
    }

    static String message(final String partialMessageCode) {
        return messages
                .findAll { it.key.toString().contains(partialMessageCode) }
                .collect { it.value }
                .find()
    }

    static Properties getMessages() {
        def props = new Properties()
        def resourceName = "${NodeOrderValidator.packageName.replace('.', '/')}/messages.properties"
        def resource = RankerValidator.classLoader.getResourceAsStream(resourceName)
        props.load(resource)
        return props
    }

    static void printViolations(final List<AuditEvent> violations) {
        violations.forEach { println("${it.line}:${it.column} - ${it.message}") }
    }

}
