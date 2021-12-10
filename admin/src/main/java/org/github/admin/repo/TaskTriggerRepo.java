package org.github.admin.repo;

import org.github.admin.entity.TaskTrigger;
import org.github.admin.entity.TriggerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
public interface TaskTriggerRepo extends JpaRepository<TaskTrigger, Long> {

    Page<TaskTrigger> findAllByStatusAndNextTimeIsLessThanEqual(TriggerStatus status, Long nextTime, Pageable pageable);
}
