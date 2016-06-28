package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.bean.commentsBean.CommentBean;
import com.dianping.pigeon.governor.bean.commentsBean.FeedbackBean;
import com.dianping.pigeon.governor.model.User;

import java.util.List;

/**
 * Created by shihuashen on 16/6/22.
 */
public interface FeedbackService {
    List<FeedbackBean> getFeedbacks(int start, int size,User user);
    int getFeedbackTotalNumbers();
    int getCommentTotalNumbers(int feedbackId);
    List<CommentBean> getComments(int feedbackId, int start, int size,User user);
    void addComments(int feedbackId, String content, User user);
    void addFeedback(String title,String content,User user);
    void addReply(int commentId,String content,User user);
    void deleteFeedback(int feedbackId);
    void deleteComment(int commentId);
    void feedbackStateChange(int feedbackId, String userName);

    void commentStateChange(int commentId, String userName);
}
