package org.github.admin.req;

import lombok.Data;
import org.github.admin.entity.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
