package org.github.admin.repo;

import org.github.admin.model.entity.TaskInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
public interface TaskInfoRepo extends JpaRepository<TaskInfo, Long> {
}
