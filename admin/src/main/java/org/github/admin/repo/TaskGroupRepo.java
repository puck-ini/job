package org.github.admin.repo;

import org.github.admin.entity.TaskGroup;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
public interface TaskGroupRepo extends JpaRepository<TaskGroup, Long> {
}
