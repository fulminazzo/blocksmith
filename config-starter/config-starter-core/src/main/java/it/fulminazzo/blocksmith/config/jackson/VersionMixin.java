package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Annotations;
import it.fulminazzo.blocksmith.config.ConfigVersion;
import it.fulminazzo.blocksmith.reflect.Reflect;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

@JsonAppend(prepend = true, props = @JsonAppend.Prop(
        name = "version",
        value = VersionMixin.VersionPropertyWriter.class,
        include = JsonInclude.Include.NON_NULL
))
interface VersionMixin {

    final class VersionPropertyWriter extends VirtualBeanPropertyWriter {

        public VersionPropertyWriter(final @NotNull BeanPropertyDefinition propDef,
                                     final @NotNull Annotations annotations,
                                     final @NotNull JavaType declaredType) {
            super(propDef, annotations, declaredType);
        }

        @Override
        protected Object value(final @NotNull Object bean,
                               final @NotNull JsonGenerator generator,
                               final @NotNull SerializerProvider provider) throws Exception {
            Reflect reflect = Reflect.on(bean.getClass());
            return reflect
                    .getFields(f -> Modifier.isStatic(f.getModifiers()) && f.getType().equals(ConfigVersion.class))
                    .stream().findFirst()
                    .map(f -> reflect.get(f).<ConfigVersion>get().getVersion())
                    .orElse(null);
        }

        @Override
        public VirtualBeanPropertyWriter withConfig(final @NotNull MapperConfig<?> config,
                                                    final @NotNull AnnotatedClass declaringClass,
                                                    final @NotNull BeanPropertyDefinition propDef,
                                                    final @NotNull JavaType type) {
            return new VersionPropertyWriter(propDef, declaringClass.getAnnotations(), type);
        }

    }

}
