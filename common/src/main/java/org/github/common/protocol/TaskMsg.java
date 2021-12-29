package org.github.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zengchzh
 * @date 2021/12/11
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskMsg implements Serializable {

    private MsgType msgType;

    private Object data;
}
