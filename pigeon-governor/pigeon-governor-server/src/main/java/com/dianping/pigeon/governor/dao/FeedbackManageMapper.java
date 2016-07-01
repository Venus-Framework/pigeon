package com.dianping.pigeon.governor.dao;

import com.dianping.pigeon.governor.model.Comment;
import com.dianping.pigeon.governor.model.Feedback;

import java.util.List;

/**
 * Created by shihuashen on 16/6/23.
 */
public interface FeedbackManageMapper {
    List<Feedback> paginationGetFeedbacks(int startIndex, int size);

    int getFeedbackCount();

    List<Comment> paginationGetComments(int feedbackId, int startIndex, int size);

    int getCommentsNumberOfFeedback(int feedbackId);

    Integer findReplyToCommentId(int replyId);

    void newCommentUpdate(int feedbackId);
    int getFeedbackIdOfComment(int commentId);
    int commentInsertAndGetId(Comment comment);
    void removeReplyRelation(int commentId);
    void deleteCommentUpdate(int feedbackId);
    Integer checkFeedbackSupport(int feedbackId ,String userName);
    Integer checkCommentSupport(int feedbackId,String userName);
    void unSupportFeedback(int feedbackId,String userName);
    void descFeedbackSupportCount(int feedbackId);
    void addFeedbackSupportCount(int feedbackId);
    void unSupportComment(int feedbackId,String userName);
    void descCommentSupportCount(int feedbackId);
    void addCommentSupportCount(int feedbackId);
}
