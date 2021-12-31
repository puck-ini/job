package org.github.admin.repo;

import org.github.admin.model.entity.TaskGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
public interface TaskGroupRepo extends JpaRepository<TaskGroup, Long> {

    Optional<TaskGroup> findByName(String name);
}
