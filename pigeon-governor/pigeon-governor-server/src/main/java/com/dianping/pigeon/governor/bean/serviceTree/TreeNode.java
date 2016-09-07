package com.dianping.pigeon.governor.bean.serviceTree;

import java.util.List;

/**
 * Created by shihuashen on 16/8/12.
 */
public class TreeNode {
    private String name;
    private int childrenCount;
    private List<TreeNode> children;
    public TreeNode(String name,List<TreeNode> children){
        this.name = name;
        this.childrenCount = children.size();
        this.children = children;
    }
}
