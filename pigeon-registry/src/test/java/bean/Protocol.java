package bean;

import com.dianping.pigeon.registry.Registry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 16/5/30.
 */
public class Protocol {

    public static void main(String[] args) {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put("service1", true);
        map.put("service2", false);

        ObjectMapper mapper = new ObjectMapper();

        String config = null;
        try {
            config = mapper.writeValueAsString(map);
            System.out.println(config);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }

        Map map2 = new ConcurrentHashMap<String, Boolean>();
        try {
            map2 = mapper.readValue(config, ConcurrentHashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Object key : map2.keySet()) {
            Boolean b = (Boolean) map2.get(key);
            System.out.println(b);
        }
    }
}
