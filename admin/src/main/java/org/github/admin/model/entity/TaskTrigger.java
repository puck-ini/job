package org.github.admin.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class TaskTrigger extends BaseEntity {

    /**
     * 任务参数，Object[] json
     */
    private String parameters;
    /**
     * cron 表达式
     */
    private String cronExpression;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 上次运行时间
     */
    private Long lastTime;

    /**
     * 下次运行时间
     */
    private Long nextTime;
    /**
     * 运行状态
     */
    @Enumerated(EnumType.STRING)
    private TriggerStatus status = TriggerStatus.STOP;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TaskInfo taskInfo;


    public enum TriggerStatus {

        RUNNING,
        STOP
        ;
    }

}
