package org.github.common.register;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author zengchzh
 * @date 2021/3/11
 *
 * 服务对象，用于注册中心的服务发现和注册
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceObject implements Serializable {

    private static final long serialVersionUID = 7255243583029667457L;
    /**
     * 服务名
     */
    private String groupName;
    /**
     * 服务ip
     */
    private String ip;
    /**
     * 服务端口
     */
    private Integer port;
    /**
     * 服务地址
     */
    private String address;

    public String getAddress() {
        if (address != null && !Objects.equals(address, "")) {
            address = ip + ":" + port;
        }
        return address;
    }
}
