package org.github.admin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    /**
     * 任务名
     */
    private String taskName;

    /**
     * 任务所在的类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 任务参数类型，Class<?>[] json
     */
    private String parameterTypes;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TaskGroup taskGroup;

    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "taskInfo", fetch = FetchType.EAGER)
    private List<TaskTrigger> triggerList;
}
