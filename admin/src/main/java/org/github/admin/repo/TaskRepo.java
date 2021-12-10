package org.github.admin.repo;

import org.github.admin.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
public interface TaskRepo extends JpaRepository<Task, Long> {
}
