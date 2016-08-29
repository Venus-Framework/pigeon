package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.bean.op.FilterBean;
import com.dianping.pigeon.governor.bean.op.OpLogBean;

import java.util.List;

/**
 * Created by shihuashen on 16/8/9.
 */
public interface OpLogManageService {
    List<OpLogBean> getTopNOpLog(int size);
    int getTotal();
    List<OpLogBean> filterOpLog(FilterBean filter);
}
