package it.fulminazzo.blocksmith.application

import it.fulminazzo.blocksmith.application.node.FieldAnnotationNode

final class ApplicationTestUtils {

    static {
        loadHandlers()
    }

    static void loadHandlers() {
        ApplicationHandlers.registerFieldAnnotationHandler(NoDependencies, {})
        ApplicationHandlers.registerFieldAnnotationHandler(SingleDependency, {})
        ApplicationHandlers.registerFieldAnnotationHandler(MultipleDependencies, {})
    }

    protected static FieldAnnotationNode newNode(
            final Class<? extends Application> applicationClass,
            final String fieldName
    ) {
        def field = applicationClass.getDeclaredField(fieldName)
        def annotation = field.annotations[0]
        return new FieldAnnotationNode(
                annotation,
                field,
                ApplicationHandlers.getFieldAnnotationHandler(annotation.annotationType()).orElseThrow()
        )
    }

}
