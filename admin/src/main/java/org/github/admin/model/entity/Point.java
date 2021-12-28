package org.github.admin.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Objects;

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
