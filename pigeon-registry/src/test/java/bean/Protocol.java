package bean;

import com.dianping.pigeon.domain.HostInfo;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by chenchongze on 16/5/30.
 */
public class Protocol {

    public static void main(String[] args) {
        HostInfo hostInfo1 = new HostInfo("11", 11, 0);
        HostInfo hostInfo2 = new HostInfo("11", 11, 1);
        Set<HostInfo> hostInfos = Sets.newHashSet();
        hostInfos.add(hostInfo1);
        hostInfos.remove(hostInfo2);
        hostInfos.add(hostInfo2);

        for (HostInfo hostInfo : hostInfos) {
            System.out.println(hostInfo);
        }

    }
}
