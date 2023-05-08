package io.github.guojiaxing1995.easyJmeter.bo;

import io.github.guojiaxing1995.easyJmeter.model.GroupDO;
import io.github.guojiaxing1995.easyJmeter.model.PermissionDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * @author pedro@TaleLin
 * @author Juzi@TaleLin
 * @author colorful@TaleLin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupPermissionBO {
    private Integer id;

    private String name;

    private String info;

    private List<PermissionDO> permissions;

    public GroupPermissionBO(GroupDO group, List<PermissionDO> permissions) {
        BeanUtils.copyProperties(group, this);
        this.permissions = permissions;
    }
}
