package com.nosto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ElasticsearchProperty {

    boolean DEFAULT_INDEX = true;
    boolean DEFAULT_STORE = true;
    boolean DEFAULT_ENABLED = true;
    boolean DEFAULT_NORMS = true;

    String name();

    ElasticsearchProperty.Type type();

    String analyzer() default "";

    String normalizer() default "";

    boolean index() default true;

    boolean store() default true;

    boolean enabled() default true;

    boolean norms() default true;

    String index_options() default "";

    String similarity() default "";

    String format() default "";

    public static enum Type {
        BOOLEAN("boolean"),
        INTEGER("integer"),
        LONG("long"),
        DOUBLE("double"),
        TEXT("text"),
        KEYWORD("keyword"),
        OBJECT("object"),
        NESTED("nested"),
        NESTED_OBJECT("object"),
        EMBEDDED((String)null),
        GEOPOINT("geo_point"),
        DATE("date"),
        COMPLETION("completion");

        private final String field;

        private Type(String field) {
            this.field = field;
        }

        public String toString() {
            return this.field;
        }
    }
}

