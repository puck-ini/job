package org.github.admin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.github.common.TaskDesc;

import javax.persistence.*;
import java.util.List;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class TaskInfo extends BaseEntity {

    private TaskDesc taskDesc = new TaskDesc();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TaskGroup taskGroup;

    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "taskInfo", fetch = FetchType.EAGER)
    private List<TaskTrigger> triggerList;
}
