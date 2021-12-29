package org.github.common.register;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author zengchzh
 * @date 2021/7/19
 */

public class ZkRegister {

    private final ZkClient zkClient;

    private static final String ROOT_NODE = "/task-scheduler";

    private static final String NODE_SEPARATOR = "/";

    private static final Map<String, List<ServiceObject>> SERVICE_CACHE = new ConcurrentHashMap<>();

    public static List<ServiceObject> getCache(String serviceName) {
        return SERVICE_CACHE.get(serviceName) == null ? new CopyOnWriteArrayList<>() : SERVICE_CACHE.get(serviceName);
    }

    public static void putCache(String serviceName, List<ServiceObject> serviceObjectList) {
        SERVICE_CACHE.put(serviceName, serviceObjectList);
    }

    public ZkRegister(String address) {
        zkClient = new ZkClient(address);
        zkClient.setZkSerializer(new SerializableSerializer());
        if (!zkClient.exists(ROOT_NODE)) {
            zkClient.createPersistent(ROOT_NODE, true);
        }
    }

    public ServiceObject register(ServiceObject serviceObject) {
        String serviceRootPath = ROOT_NODE + NODE_SEPARATOR + serviceObject.getGroupName();
        if (!zkClient.exists(serviceRootPath)) {
            zkClient.createPersistent(serviceRootPath, true);
        }
        String childPath = serviceRootPath + NODE_SEPARATOR + serviceObject.getAddress();
        if (zkClient.exists(childPath)) {
            zkClient.delete(childPath);
        }
        zkClient.createEphemeral(childPath, serviceObject);
        return serviceObject;
    }

    public List<ServiceObject> getAll() {
        List<String> childPath = zkClient.getChildren(ROOT_NODE);
        List<ServiceObject> result = new ArrayList<>();
        for (String path : childPath) {
            result.addAll(getAll(path));
        }
        return result;
    }

    public List<ServiceObject> getAll(String serviceName) {
        return getService(serviceName);
    }


    private List<ServiceObject> getService(String serviceName) {
        String serviceRootPath = ROOT_NODE + NODE_SEPARATOR + serviceName;
        List<String> childPath = zkClient.getChildren(serviceRootPath);
        List<ServiceObject> result = getCache(serviceName);
        if (result.size() == 0) {
            for (String path : childPath) {
                String nodePath = serviceRootPath + NODE_SEPARATOR + path;
                ServiceObject so = zkClient.readData(nodePath);
                result.add(so);
            }
            putCache(serviceName, result);
            subService(serviceName);
        }
        return result;
    }

    private void subService(String serviceName) {
        String serviceRootPath = ROOT_NODE + NODE_SEPARATOR + serviceName;
        IZkChildListener childListener = new IZkChildListener() {
            @Override
            public void handleChildChange(String s, List<String> list) throws Exception {
                List<ServiceObject> serviceObjectList = new CopyOnWriteArrayList<>();
                for (String path : list) {
                    String nodePath = serviceRootPath + NODE_SEPARATOR + path;
                    serviceObjectList.add(zkClient.readData(nodePath));
                }
                putCache(serviceName, serviceObjectList);
            }
        };
        zkClient.subscribeChildChanges(serviceRootPath, childListener);
    }
}
