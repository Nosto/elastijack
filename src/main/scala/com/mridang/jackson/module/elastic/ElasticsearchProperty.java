package com.mridang.jackson.module.elastic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for mapping model fields to Elasticsearch.
 *
 * @author olli
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ElasticsearchProperty {

    boolean DEFAULT_INDEX = true;
    boolean DEFAULT_STORE = true;
    boolean DEFAULT_ENABLED = true;
    boolean DEFAULT_NORMS = true;

    @SuppressWarnings("unused")
    enum Type {
        BOOLEAN("boolean"),
        INTEGER("integer"),
        LONG("long"),
        DOUBLE("double"),
        TEXT("text"),
        KEYWORD("keyword"),
        OBJECT("object"),
        NESTED("nested"),
        /**
         * Behaves like NESTED in model code but use object type in elasticsearch mapping.
         * This comes in handy when the mongo model must be altered but we don't want the
         * structure to elasticsearch (like variations in product model)
         */
        NESTED_OBJECT("object"),
        EMBEDDED(null),
        GEOPOINT("geo_point"),
        DATE("date"),
        /**
         * A type for supporting search completions.
         * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters-completion.html
         */
        COMPLETION("completion");

        private final String field;
        Type(String field) {
            this.field = field;
        }

        @Override
        public String toString() {
            return field;
        }
    }

    String name();
    Type type();
    String analyzer() default "";
    String normalizer() default "";
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean index() default DEFAULT_INDEX;
    boolean store() default DEFAULT_STORE;
    boolean enabled() default DEFAULT_ENABLED;
    boolean norms() default DEFAULT_NORMS;
    String index_options() default "";
    String similarity() default "";
    String format() default "";
}
