package org.github.admin.service.impl;

import org.github.admin.model.entity.TaskLock;
import org.github.admin.repo.TaskLockRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author zengchzh
 * @date 2021/12/23
 */

@Service
public class TaskLockService {

    @Autowired
    private TaskLockRepo taskLockRepo;

    @Transactional(rollbackFor = Exception.class)
    public void lock(String lockName) {
        TaskLock taskLock = taskLockRepo.findByLockName(lockName);
        if (Objects.isNull(taskLock)) {
            taskLockRepo.saveAndFlush(new TaskLock(lockName));
            taskLockRepo.findByLockName(lockName);
        }
    }
//
//    @Lock(LockModeType.PESSIMISTIC_READ)
//    public void lockTable() {
//        taskLockRepo.findAll();
//    }
//
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    public TaskLock lockTable1(String id) {
//        return taskLockRepo.findById(id).orElse(null);
//    }
}
