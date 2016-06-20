
package com.sos.joc.jobchain.model;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.common.model.Files;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * fileOrderSource (volatile part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "directory",
    "files"
})
public class FileWatchingNodeVSchema {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("directory")
    private String directory;
    @JsonProperty("files")
    private Files files;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     * @return
     *     The directory
     */
    @JsonProperty("directory")
    public String getDirectory() {
        return directory;
    }

    /**
     * 
     * (Required)
     * 
     * @param directory
     *     The directory
     */
    @JsonProperty("directory")
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * 
     * @return
     *     The files
     */
    @JsonProperty("files")
    public Files getFiles() {
        return files;
    }

    /**
     * 
     * @param files
     *     The files
     */
    @JsonProperty("files")
    public void setFiles(Files files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(directory).append(files).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FileWatchingNodeVSchema) == false) {
            return false;
        }
        FileWatchingNodeVSchema rhs = ((FileWatchingNodeVSchema) other);
        return new EqualsBuilder().append(directory, rhs.directory).append(files, rhs.files).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
