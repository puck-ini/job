package org.github.common.types;


import lombok.Data;


import javax.persistence.*;


/**
 * @author zengchzh
 * @date 2021/12/10
 */
@Embeddable
@Data
public class Point {

    private String ip;

    private int port;

    public Point() {}

    public Point(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}
