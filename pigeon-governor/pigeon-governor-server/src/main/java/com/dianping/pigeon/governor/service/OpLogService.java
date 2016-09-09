package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.model.OpLog;

import java.util.concurrent.Future;

/**
 * Created by chenchongze on 15/11/11.
 */
public interface OpLogService {

     int create(OpLog opLog);
     Future<Integer> asyncCreate(final OpLog opLog);

}
