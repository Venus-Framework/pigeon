package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.commentsBean.CommentBean;
import com.dianping.pigeon.governor.bean.commentsBean.FeedbackBean;
import com.dianping.pigeon.governor.model.Comment;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * Created by shihuashen on 16/6/16.
 */
@Controller
public class FeedbackController extends BaseController{
    @Autowired
    private FeedbackService feedbackService;
    @RequestMapping(value = {"/feedback"},method = RequestMethod.GET)
    public String feedbackMainPage(HttpServletRequest request,HttpServletResponse response,ModelMap modelMap){
        commonnav(modelMap,request);
        return "/feedback/main";
    }


    @RequestMapping(value = {"/feedback/feedbacksInit"},method = RequestMethod.POST)
    public String feedbacksInit(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){
        List<FeedbackBean> list = feedbackService.getFeedbacks(0,10,getUserInfo(request));
        int totalNumber = feedbackService.getFeedbackTotalNumbers();
        modelMap.put("feedbacks",list);
        modelMap.put("totalNumber",totalNumber);
        return "/feedback/feedbacksWithPagination";
    }
    @RequestMapping(value = {"/feedback/commentsInit"},method = RequestMethod.POST)
    public String commentsInit(HttpServletRequest request,HttpServletResponse response,ModelMap modelMap){
        String feedbackId = request.getParameter("feedbackId");
        List<CommentBean> comments = feedbackService.getComments(Integer.valueOf(feedbackId),0,10,getUserInfo(request));
        int totalNumber = feedbackService.getCommentTotalNumbers(Integer.valueOf(feedbackId));
        modelMap.put("comments",comments);
        modelMap.put("commentsNumber",totalNumber);
        modelMap.put("feedbackId",feedbackId);
        return "/feedback/commentsWithPagination";
    }

    @RequestMapping(value={"/feedback/feedbacks"},method = RequestMethod.POST)
    public String paginationFeedbacks(HttpServletRequest request,HttpServletResponse response,ModelMap modelMap){
        String start = request.getParameter("start");
        String size = request.getParameter("size");
        modelMap.put("feedbacks",feedbackService.getFeedbacks(Integer.valueOf(start),Integer.valueOf(size),getUserInfo(request)));
        return "/feedback/feedbacks";
    }

    @RequestMapping(value={"/feedback/comments"},method = RequestMethod.POST)
    public String paginationComments(HttpServletRequest request,HttpServletResponse response,ModelMap modelMap){
        String feedbackId = request.getParameter("feedbackId");
        String start = request.getParameter("start");
        String size = request.getParameter("size");
        modelMap.put("comments",feedbackService.getComments(Integer.valueOf(feedbackId),Integer.valueOf(start),Integer.valueOf(size),getUserInfo(request)));
        modelMap.put("feedbackId",feedbackId);
        return "/feedback/comments";
    }

    @RequestMapping(value={"/feedback/newComment"},method = RequestMethod.POST)
    public void addComments(HttpServletRequest request,HttpServletResponse response,ModelMap modelMap){
        int feedbackId = Integer.valueOf(request.getParameter("feedbackId"));
        String content = request.getParameter("content");
        User user = getUserInfo(request);
        feedbackService.addComments(feedbackId,content,user);
    }

    @RequestMapping(value={"/feedback/newReply"},method = RequestMethod.POST)
    public void addReply(HttpServletRequest request,HttpServletResponse response){
        int feedbackId = Integer.valueOf(request.getParameter("commentId"));
        String content = request.getParameter("content");
        User user = getUserInfo(request);
        feedbackService.addReply(feedbackId,content,user);
    }

    @RequestMapping(value={"/feedback/newFeedback"},method = RequestMethod.POST)
    public void addFeedback(HttpServletRequest request,HttpServletResponse response){
        String feedbackTitle = request.getParameter("title");
        String feedbackContent = request.getParameter("content");
        User user = getUserInfo(request);
        feedbackService.addFeedback(feedbackTitle,feedbackContent,user);
        return ;
    }

    @RequestMapping(value={"/feedback/deleteFeedback"},method = RequestMethod.POST)
    public void deleteFeedback(HttpServletRequest request,HttpServletResponse response){
        String feedbackId = request.getParameter("feedbackId");
        feedbackService.deleteFeedback(Integer.valueOf(feedbackId));
        return ;
    }

    @RequestMapping(value={"/feedback/deleteComment"},method = RequestMethod.POST)
    public void deleteComment(HttpServletRequest request,HttpServletResponse response){
        String commentId = request.getParameter("commentId");
        feedbackService.deleteComment(Integer.valueOf(commentId));
    }

    @RequestMapping(value = {"/feedback/support"},method = RequestMethod.POST)
    public void supportFeedback(HttpServletRequest request, HttpServletResponse response){
        String feedbackId = request.getParameter("feedbackId");
        String userName = getUserInfo(request).getDpaccount();
        feedbackService.feedbackStateChange(Integer.valueOf(feedbackId),userName);
        return ;
    }

    @RequestMapping(value = {"/feedback/comment/support"},method = RequestMethod.POST)
    public void supportComment(HttpServletRequest request,HttpServletResponse response){
        String commentId = request.getParameter("commentId");
        String userName = getUserInfo(request).getDpaccount();
        feedbackService.commentStateChange(Integer.valueOf(commentId),userName);
    }
}
