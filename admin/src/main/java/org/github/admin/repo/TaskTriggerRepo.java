package org.github.admin.repo;

import org.github.admin.model.entity.TaskTrigger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
public interface TaskTriggerRepo extends JpaRepository<TaskTrigger, Long> {

    Page<TaskTrigger> findAllByStatusAndNextTimeIsLessThanEqual(TaskTrigger.TriggerStatus status, Long nextTime, Pageable pageable);
}
