package org.github.admin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TaskGroup taskGroup;

    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "taskInfo", fetch = FetchType.EAGER)
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
