package it.fulminazzo.blocksmith.checker

import com.puppycrawl.tools.checkstyle.Checker
import com.puppycrawl.tools.checkstyle.DefaultConfiguration
import com.puppycrawl.tools.checkstyle.api.AbstractCheck
import com.puppycrawl.tools.checkstyle.api.AuditEvent
import com.puppycrawl.tools.checkstyle.api.AuditListener
import it.fulminazzo.blocksmith.checker.validator.RankerValidator

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
        checker.process([file])
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
        def resourceName = "${NodeValidator.packageName.replace('.', '/')}/messages.properties"
        def resource = RankerValidator.classLoader.getResourceAsStream(resourceName)
        props.load(resource)
        return props
    }

}
