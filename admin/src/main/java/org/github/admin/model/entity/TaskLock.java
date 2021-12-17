package org.github.admin.model.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author zengchzh
 * @date 2021/12/17
 */

@Data
@Entity
public class TaskLock {


    @Id
    private String lockName;
}
