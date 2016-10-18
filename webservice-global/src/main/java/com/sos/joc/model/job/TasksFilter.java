
package com.sos.joc.model.job;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "job",
    "comment",
    "taskIds"
})
public class TasksFilter {

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    private String job;
    /**
     * Field to comment manually job modifications which can be logged.
     * 
     */
    @JsonProperty("comment")
    private String comment;
    @JsonProperty("taskIds")
    private List<TaskId> taskIds = new ArrayList<TaskId>();

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     * @return
     *     The job
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     * @param job
     *     The job
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * Field to comment manually job modifications which can be logged.
     * 
     * @return
     *     The comment
     */
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    /**
     * Field to comment manually job modifications which can be logged.
     * 
     * @param comment
     *     The comment
     */
    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 
     * @return
     *     The taskIds
     */
    @JsonProperty("taskIds")
    public List<TaskId> getTaskIds() {
        return taskIds;
    }

    /**
     * 
     * @param taskIds
     *     The taskIds
     */
    @JsonProperty("taskIds")
    public void setTaskIds(List<TaskId> taskIds) {
        this.taskIds = taskIds;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(job).append(comment).append(taskIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TasksFilter) == false) {
            return false;
        }
        TasksFilter rhs = ((TasksFilter) other);
        return new EqualsBuilder().append(job, rhs.job).append(comment, rhs.comment).append(taskIds, rhs.taskIds).isEquals();
    }

}