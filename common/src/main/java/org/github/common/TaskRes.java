package org.github.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zengchzh
 * @date 2021/12/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRes implements Serializable {

    /**
     * 请求id
     */
    private String requestId;
    /**
     * 错误信息
     */
    private String error;
    /**
     * 调用返回结果
     */
    private Object result;

}
