package org.github.admin.req;

import lombok.Data;
import org.github.admin.entity.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zengchzh
 * @date 2021/12/10
 */
@Data
public class CreateGroupReq {

    private String name;

    private List<Point> pointList = new ArrayList<>();
}
