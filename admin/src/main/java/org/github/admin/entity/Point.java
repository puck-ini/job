package org.github.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
@Entity
@Data
public class Point extends BaseEntity {

    private String ip;

    private int port;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TaskGroup taskGroup;

    public Point() {}

    public Point(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Point(String ip, int port, TaskGroup taskGroup) {
        this.ip = ip;
        this.port = port;
        this.taskGroup = taskGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Point point = (Point) o;
        return port == point.port &&
                ip.equals(point.ip) &&
                Objects.equals(taskGroup, point.taskGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port, taskGroup);
    }
}
