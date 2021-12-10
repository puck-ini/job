package org.github.admin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class TaskGroup extends BaseEntity {

    private String name;

    @Fetch(FetchMode.SUBSELECT)
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "taskGroup", fetch = FetchType.EAGER)
    private List<Point> pointList;

    @Fetch(FetchMode.SUBSELECT) // 一对多的类中还有一对多关系需要使用该注解
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "taskGroup", fetch = FetchType.EAGER)
    private List<Task> taskList;
}
