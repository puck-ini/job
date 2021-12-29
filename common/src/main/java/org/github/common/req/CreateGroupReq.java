package org.github.common.req;

import lombok.Data;
import org.github.common.types.Point;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
@Data
public class CreateGroupReq {

    private String name;

    private Set<Point> pointSet = new HashSet<>();
}
