package com.nosto.elastijack;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.testcontainers.shaded.org.apache.commons.lang.builder.EqualsBuilder;
import org.testcontainers.shaded.org.apache.commons.lang.builder.HashCodeBuilder;
import org.testcontainers.shaded.org.apache.commons.lang.builder.ToStringBuilder;
import org.testcontainers.shaded.org.apache.commons.lang.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nosto.ElasticsearchProperty;
import com.nosto.elasticsearch.ElasticsearchMappings;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings({"FieldCanBeLocal", "EqualsWhichDoesntCheckParameterClass"})
public class TestJavaBean implements ElasticsearchMappings {

    @ElasticsearchProperty(name = "foo", type = ElasticsearchProperty.Type.KEYWORD)
    private final String foo;

    @ElasticsearchProperty(name = "date", type = ElasticsearchProperty.Type.DATE, format = "yyyy-MM-dd")
    private final LocalDate date;

    @ElasticsearchProperty(name = "datetime", type = ElasticsearchProperty.Type.DATE)
    private final LocalDateTime dateTime;

    @JsonCreator
    public TestJavaBean(
            @JsonProperty("foo") String foo,
            @JsonProperty("date") LocalDate date,
            @JsonProperty("dateTime") LocalDateTime dateTime) {
        this.foo = foo;
        this.date = date;
        this.dateTime = dateTime;
    }

    @JsonProperty(required = true)
    public Optional<String> getFoo() {
        return Optional.of(foo);
    }

    @JsonProperty(required = true)
    public LocalDate getDate() {
        return date;
    }

    @JsonProperty(required = true)
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
