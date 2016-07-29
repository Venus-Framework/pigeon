package com.dianping.pigeon.governor.message.impl;

import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.message.EventFilter;
import com.dianping.pigeon.governor.message.FilterContainer;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shihuashen on 16/7/19.
 */
@Component
public class DefaultFilterContainer implements FilterContainer{
    //TODO 由于分布式的原因.需要考虑Cache层的设计方针.是否需要单独部署在一台节点上?
    //TODO data base service.初始化,注册,删除,都应当与数据库保持相应的一致性.而GET可以绕过内存进行访问.
    private TrieTree trieTree;
    public DefaultFilterContainer(){
        //TODO database init
        this.trieTree = new TrieTree();
    }

    @Override
    public List<EventFilter> getFilters(Event event) {
        String fullPath = event.getSignature();
        return this.trieTree.getMatchFilter(fullPath);
    }

    @Override
    public boolean registerFilter(EventFilter filter) {
        this.trieTree.add(filter);
        return true;
    }

    @Override
    public boolean removeFilter(EventFilter filter) {
        this.trieTree.deleteFilter(filter);
        return true;
    }


    class TrieTree{
        private Node root = new Node("");
        public TrieTree(){}
        public TrieTree(List<EventFilter> list){
            for(EventFilter eventFilter : list)
                add(eventFilter);
        }

        public void add(EventFilter eventFilter) {
            char[] signature = eventFilter.getSignature().toCharArray();
            Node currentNode = root;
            for(int i = 0;i<signature.length;i++){
                char temp = signature[i];
                if(!currentNode.containsChild(temp))
                    currentNode.addChild(signature[i],new Node(currentNode.value+temp));
                currentNode = currentNode.getChild(temp);
            }
            currentNode.addEventFilter(eventFilter);
        }
        public List<EventFilter> getMatchFilter(String fullPath){
            List<EventFilter> filters = new LinkedList<EventFilter>();
            char[] sequence = fullPath.toCharArray();
            Node currentNode = root;
            for(int i = 0 ; i<sequence.length;i++){
                char tmp = sequence[i];
                if(currentNode.containsChild(tmp)){
                    filters.addAll(currentNode.getFilters());
                    currentNode = currentNode.getChild(tmp);
                }
                else
                    break;
            }
            filters.addAll(currentNode.getFilters());
            return filters;
        }

        public void deleteFilter(EventFilter filter){
            String signature = filter.getSignature();
            Node containsNode = getNode(signature);
            containsNode.filters.remove(filter.getId());
        }
        private Node getNode(String signature){
            char[] sequence = signature.toCharArray();
            Node currentNode = root;
            for(int i =0; i<sequence.length;i++){
                char tmp = sequence[i];
                if(currentNode.containsChild(tmp))
                    currentNode = currentNode.getChild(tmp);
            }
            return currentNode;
        }
    }
    class Node{
        private final String value;
        private ConcurrentHashMap<Character,Node> children;
        private ConcurrentHashMap<Long,EventFilter> filters;
        public Node(String value){
            this.value = value;
            this.children = new ConcurrentHashMap<Character, Node>();
            this.filters = new ConcurrentHashMap<Long, EventFilter>();
        }
        public void addChild(char c,Node node){
            this.children.put(c,node);
        }
        public Node getChild(char c){
            return children.get(c);
        }
        public boolean containsChild(char c){
            return children.containsKey(c);
        }
        public void addEventFilter(EventFilter filter){
            this.filters.put(filter.getId(),filter);
        }
        public Collection<EventFilter> getFilters(){
            return this.filters.values();
        }
    }
}
