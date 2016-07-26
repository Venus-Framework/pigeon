package com.dianping.pigeon.governor.service.impl;

import com.dianping.pigeon.governor.bean.comments.CommentBean;
import com.dianping.pigeon.governor.bean.comments.FeedbackBean;
import com.dianping.pigeon.governor.dao.*;
import com.dianping.pigeon.governor.model.*;
import com.dianping.pigeon.governor.service.FeedbackService;
import com.dianping.pigeon.governor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shihuashen on 16/6/22.
 */

@Service
public class FeedbackServiceImpl implements FeedbackService {
    @Autowired
    private FeedbackManageMapper feedbackManageMapper;
    @Autowired
    private FeedbackMapper feedbackMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private CommentReplyMapper commentReplyMapper;
    @Autowired
    private FeedbackSupportMapper feedbackSupportMapper;
    @Autowired
    private CommentSupportMapper commentSupportMapper;

    @Override
    public List<FeedbackBean> getFeedbacks(int start, int size,User user) {
        List<Feedback> dbFeedbacks = feedbackManageMapper.paginationGetFeedbacks(start,size);
        List<FeedbackBean> ans = new LinkedList<FeedbackBean>();
        for(Iterator<Feedback> iter = dbFeedbacks.iterator(); iter.hasNext();)
            ans.add(convertFeedbackModelToBean(iter.next(),user));
        return ans;
    }

    @Override
    public int getFeedbackTotalNumbers() {
        return feedbackManageMapper.getFeedbackCount();
    }

    @Override
    public int getCommentTotalNumbers(int feedbackId) {
        return feedbackMapper.selectByPrimaryKey(feedbackId).getCommentsNumber();
    }

    @Override
    public List<CommentBean> getComments(int feedbackId, int start, int size,User user) {
        List<Comment> dbComments = feedbackManageMapper.paginationGetComments(feedbackId, start, size);
        List<CommentBean> ans = new LinkedList<CommentBean>();
        for (Iterator<Comment> iter = dbComments.iterator(); iter.hasNext(); )
            ans.add(convertCommentModelToBean(iter.next(), user));
        return ans;
    }

    private CommentBean convertCommentModelToBean(Comment model, User user) {
        CommentBean bean = new CommentBean();
        bean.setId(model.getId());
        bean.setTitle("");
        bean.setContent(model.getContent());
        bean.setUpdateTime(model.getUpdatetime());
        bean.setAuthor(userService.retrieveByDpaccount(model.getUserid()).getUsername());
        bean.setReplyAuthor(getReplyToUserName(model));
        bean.setEmpowered(checkAuthority(user,model.getUserid()));
        bean.setSupportedNumber(model.getSupportsNumber());
        //TODO get supported state
        int commentId = model.getId();
        String userName  = model.getUserid();
        if(feedbackManageMapper.checkCommentSupport(commentId,userName)!=null)
            bean.setSupported(true);
        else
            bean.setSupported(false);
        return bean;
    }

    private String getReplyToUserName(Comment model){
        int commentId = model.getId();
        Integer replyId = feedbackManageMapper.findReplyToCommentId(commentId);
        if(replyId==null)
            return "";
        else{
            Comment originComment = commentMapper.selectByPrimaryKey(replyId.intValue());
            return userService.retrieveByDpaccount(originComment.getUserid()).getUsername();
        }
    }



    @Override
    public void addComments(int feedbackId, String content, User user) {
        Comment comment = new Comment();
        comment.setFeedbackId(feedbackId);
        comment.setContent(content);
        comment.setUserid(user.getDpaccount());
        comment.setSupportsNumber(0);
        commentMapper.insertSelective(comment);
        feedbackManageMapper.newCommentUpdate(feedbackId);
    }

    @Override
    public void addFeedback(String title, String content,User user) {
        Feedback feedback = new Feedback();
        feedback.setTitle(title);
        feedback.setContent(content);
        feedback.setSupportsNumber(0);
        feedback.setCommentsNumber(0);
        feedback.setUserid(user.getDpaccount());
        feedbackMapper.insertSelective(feedback);
    }

    @Override
    public void addReply(int commentId, String content, User user) {
        int feedbackId = feedbackManageMapper.getFeedbackIdOfComment(commentId);
        Comment comment = new Comment();
        comment.setFeedbackId(feedbackId);
        comment.setContent(content);
        comment.setUserid(user.getDpaccount());
        comment.setSupportsNumber(0);
        feedbackManageMapper.commentInsertAndGetId(comment);
        int newCommentId = comment.getId();
        System.out.println("newCommentId:"+newCommentId);
        feedbackManageMapper.newCommentUpdate(feedbackId);
        CommentReply relation = new CommentReply();
        relation.setOriginId(commentId);
        relation.setReplyId(newCommentId);
        commentReplyMapper.insertSelective(relation);
    }

    @Override
    public void deleteFeedback(int feedbackId) {
        feedbackMapper.deleteByPrimaryKey(feedbackId);
        //TODO batch delete all of the comments belongs to this feedback;
        //TODO delete the support tables.
    }

    @Override
    public void deleteComment(int commentId) {
        feedbackManageMapper.deleteCommentUpdate(feedbackManageMapper.getFeedbackIdOfComment(commentId));
        commentMapper.deleteByPrimaryKey(commentId);
        feedbackManageMapper.removeReplyRelation(commentId);
        //TODO delete the support tables.
    }

    @Override
    public void feedbackStateChange(int feedbackId, String userName) {
        if(feedbackManageMapper.checkFeedbackSupport(feedbackId,userName)!=null){
            feedbackManageMapper.unSupportFeedback(feedbackId,userName);
            feedbackManageMapper.descFeedbackSupportCount(feedbackId);
        }else{
            FeedbackSupport feedbackSupport = new FeedbackSupport();
            feedbackSupport.setFeedbackId(feedbackId);
            feedbackSupport.setUserName(userName);
            feedbackSupportMapper.insertSelective(feedbackSupport);
            feedbackManageMapper.addFeedbackSupportCount(feedbackId);
        }
    }

    @Override
    public void commentStateChange(int commentId, String userName) {
        if(feedbackManageMapper.checkCommentSupport(commentId,userName)!=null){
            feedbackManageMapper.unSupportComment(commentId,userName);
            feedbackManageMapper.descCommentSupportCount(commentId);
        }else{
            CommentSupport commentSupport = new CommentSupport();
            commentSupport.setCommentId(commentId);
            commentSupport.setUserName(userName);
            commentSupportMapper.insertSelective(commentSupport);
            feedbackManageMapper.addCommentSupportCount(commentId);
        }
    }





    private FeedbackBean convertFeedbackModelToBean(Feedback model,User user){
        FeedbackBean bean = new FeedbackBean();
        bean.setId(model.getId());
        bean.setSupportsNumber(model.getSupportsNumber());
        bean.setCommentsNumber(model.getCommentsNumber());
        bean.setTitle(model.getTitle());
        bean.setContent(model.getContent());
        bean.setAuthor(userService.retrieveByDpaccount(model.getUserid()).getUsername());
        bean.setUpdateTime(model.getUpdatetime());
        bean.setEmpowered(checkAuthority(user,model.getUserid()));
        String userName = model.getUserid();
        int feedbackId = model.getId();
        if(feedbackManageMapper.checkFeedbackSupport(feedbackId,userName)!=null)
            bean.setSupported(true);
        else
            bean.setSupported(false);
        return bean;
    }

    private boolean checkAuthority(User user,String modelUserId){
        if(user.getRoleid()==1)
            return true;
        if(user.getDpaccount().equals(modelUserId))
            return true;
        return false;
    }
}
