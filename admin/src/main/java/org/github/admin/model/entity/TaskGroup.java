package org.github.admin.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.*;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
@Entity
@Data
public class TaskGroup extends BaseEntity {

    @Column(unique = true)
    private String name;

    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "taskGroup", fetch = FetchType.EAGER)
    private Set<Point> pointSet = new HashSet<>();

    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT) // 一对多的类中还有一对多关系需要使用该注解
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "taskGroup", fetch = FetchType.EAGER)
    private Set<TaskInfo> taskInfoSet = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskGroup taskGroup = (TaskGroup) o;
        return name.equals(taskGroup.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
