package org.github.admin.entity;

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
public class Point extends BaseEntity {

    private String ip;

    private int port;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TaskGroup taskGroup;
}
