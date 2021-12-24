package org.github.admin.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import org.github.common.TaskDesc;

import javax.persistence.*;
import java.util.*;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
@Entity
@Data
public class TaskInfo extends BaseEntity {

    private TaskDesc taskDesc = new TaskDesc();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TaskGroup taskGroup;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), name = "task_info_id")
    private Set<TaskTrigger> triggerSet = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskInfo taskInfo = (TaskInfo) o;
        return taskDesc.equals(taskInfo.taskDesc) &&
                taskGroup.equals(taskInfo.taskGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskDesc, taskGroup);
    }
}
