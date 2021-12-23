package org.github.admin.repo;

import org.github.admin.model.entity.TaskLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;

/**
 * @author zengchzh
 * @date 2021/12/17
 */
public interface TaskLockRepo extends JpaRepository<TaskLock, String> {

    /**
     * 上行级锁。在查询方法上使用Query或从方法名称派生查询时才会有效。
     * @param lockName 锁名
     * @return 返回锁对象
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    TaskLock findByLockName(String lockName);
}
