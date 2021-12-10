package org.github.admin.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author zengchzh
 * @date 2021/12/10
 */

@MappedSuperclass
@Data
public class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * jpa version 乐观锁，解决并发更新的问题
     */
    @Version
    private Integer version;

    /**
     * 在实体保存到数据库之前执行的操作
     */
    @PrePersist
    public void prePersist(){
        this.createTime = this.updateTime = new Date();
    }

    /**
     * 在实体更新到数据库之前执行的操作
     */
    @PreUpdate
    public void preUpdate(){
        this.updateTime = new Date();
    }

    /**
     * 在实体从数据库删除之前执行的操作
     */
    @PreRemove
    public void preRemove(){
        this.updateTime = new Date();
    }
}
