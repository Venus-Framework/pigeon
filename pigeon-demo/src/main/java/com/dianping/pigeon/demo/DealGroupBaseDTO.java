/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class DealGroupBaseDTO implements Serializable {

    private static final long serialVersionUID = 2178057282673375790L;

    /**
     * 团单id
     */
    private int dealGroupId;

    /**
     * 团单标题
     */
    private String dealGroupShortTitle;
    /**
     * 标题描述
     */
    private String dealGroupTitleDesc;

    private int maxJoin;
    private int currentJoin;

    /**
     * 默认图片
     */
    private String defaultPic;
    /**
     * 团单价格
     */
    private BigDecimal dealGroupPrice;
    /**
     * 团单原价
     */
    private BigDecimal marketPrice;

    /**
     * 开始时间
     */
    private Date beginDate;
    /**
     * 终止时间
     */
    private Date endDate;
    /**
     * 售卖完毕的时间
     */
    private Date finishDate;
    /**
     * 单用户最大购买量
     */
    private int maxPerUser;
    /**
     * 单用户最小购买量
     */
    private int minPerUser;

    /**
     * 0 不能购买 >=1 可以购买
     */
    private int status;
    /**
     * 0-不支持退款  1-支持随时退   2-支持7天退换
     */
    private int autoRefundSwitch;
    /**
     * 团单图片，以'|'分隔
     */
    private String dealGroupPics;

    /**
     * 购买渠道 0 不限 1 网站 2 手机 4 wap 8 thirdparty第三方 16 event活动
     */
    private int saleChannel;

    /**
     * 发布状态 0 预览单，只预览未发布 1 已发布
     */
    private int publishStatus;

    /**
     * 1: 常规单 2: 抽奖单 3: 秒杀单(废弃) 4: 储值卡-4: 老的储值卡-1: 实物商品5: 酒店商品(酒店预订) 6：旅游商品
     */
    private int dealGroupType;

    /**
     * 是否支持过期自动退
     */
    private boolean overdueAutoRefund;

    /**
     * 是否可使用优惠券 false 不可以 true 可以
     */
    private boolean canUseCoupon;

    /**
     * 是否使用第三方认证 false 不使用 true 使用
     */
    private boolean thirdPartVerify;

    /**
     * 可用支付渠道 0：所有渠道；1：余额；2：支付宝pc；8：支付宝wap
     */
    private int payChannelIDAllowed;

    /**
     * 优惠规则id，供支付使用
     */
    private int discountRuleID;

    /**
     * 是否锁库存
     */
    private boolean blockStock;

    /**
     * 发布版本，与后台关联，目前供结算使用
     */
    private int publishVersion;

    /**
     * 是否抽奖单
     */
    private boolean lottery;

    private String productTitle;
    private String featureTitle;

    private List<DealBaseDTO> deals;
    
    private int sourceId; // 102=apollo, 200=meituan

    public String getDealGroupShortTitle() {
        return dealGroupShortTitle;
    }

    public void setDealGroupShortTitle(String dealGroupShortTitle) {
        this.dealGroupShortTitle = dealGroupShortTitle;
    }

    public String getDealGroupTitleDesc() {
        return dealGroupTitleDesc;
    }

    public void setDealGroupTitleDesc(String dealGroupTitleDesc) {
        this.dealGroupTitleDesc = dealGroupTitleDesc;
    }

    public String getDefaultPic() {
        return defaultPic;
    }

    public void setDefaultPic(String defaultPic) {
        this.defaultPic = defaultPic;
    }

    public BigDecimal getDealGroupPrice() {
        return dealGroupPrice;
    }

    public void setDealGroupPrice(BigDecimal dealGroupPrice) {
        this.dealGroupPrice = dealGroupPrice;
    }

    public BigDecimal getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(BigDecimal marketPrice) {
        this.marketPrice = marketPrice;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public int getMaxPerUser() {
        return maxPerUser;
    }

    public void setMaxPerUser(int maxPerUser) {
        this.maxPerUser = maxPerUser;
    }

    public int getMinPerUser() {
        return minPerUser;
    }

    public void setMinPerUser(int minPerUser) {
        this.minPerUser = minPerUser;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAutoRefundSwitch() {
        return autoRefundSwitch;
    }

    public void setAutoRefundSwitch(int autoRefundSwitch) {
        this.autoRefundSwitch = autoRefundSwitch;
    }

    public String getDealGroupPics() {
        return dealGroupPics;
    }

    public void setDealGroupPics(String dealGroupPics) {
        this.dealGroupPics = dealGroupPics;
    }

    public List<DealBaseDTO> getDeals() {
        return deals;
    }

    public void setDeals(List<DealBaseDTO> deals) {
        this.deals = deals;
    }

    public int getDealGroupId() {
        return dealGroupId;
    }

    public void setDealGroupId(int dealGroupId) {
        this.dealGroupId = dealGroupId;
    }

    public int getSaleChannel() {
        return saleChannel;
    }

    public void setSaleChannel(int saleChannel) {
        this.saleChannel = saleChannel;
    }

    public int getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(int publishStatus) {
        this.publishStatus = publishStatus;
    }

    public int getDealGroupType() {
        return dealGroupType;
    }

    public void setDealGroupType(int dealGroupType) {
        this.dealGroupType = dealGroupType;
    }

    public boolean isOverdueAutoRefund() {
        return overdueAutoRefund;
    }

    public void setOverdueAutoRefund(boolean overdueAutoRefund) {
        this.overdueAutoRefund = overdueAutoRefund;
    }

    public boolean isCanUseCoupon() {
        return canUseCoupon;
    }

    public void setCanUseCoupon(boolean canUseCoupon) {
        this.canUseCoupon = canUseCoupon;
    }

    public boolean isThirdPartVerify() {
        return thirdPartVerify;
    }

    public void setThirdPartVerify(boolean thirdPartVerify) {
        this.thirdPartVerify = thirdPartVerify;
    }

    public int getPayChannelIDAllowed() {
        return payChannelIDAllowed;
    }

    public void setPayChannelIDAllowed(int payChannelIDAllowed) {
        this.payChannelIDAllowed = payChannelIDAllowed;
    }

    public int getDiscountRuleID() {
        return discountRuleID;
    }

    public void setDiscountRuleID(int discountRuleID) {
        this.discountRuleID = discountRuleID;
    }

    public boolean isBlockStock() {
        return blockStock;
    }

    public void setBlockStock(boolean blockStock) {
        this.blockStock = blockStock;
    }

    public int getPublishVersion() {
        return publishVersion;
    }

    public void setPublishVersion(int publishVersion) {
        this.publishVersion = publishVersion;
    }

    public boolean isLottery() {
        return lottery;
    }

    public void setLottery(boolean lottery) {
        this.lottery = lottery;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getFeatureTitle() {
        return featureTitle;
    }

    public void setFeatureTitle(String featureTitle) {
        this.featureTitle = featureTitle;
    }

    public int getCurrentJoin() {
        return currentJoin;
    }

    public void setCurrentJoin(int currentJoin) {
        this.currentJoin = currentJoin;
    }

    public int getMaxJoin() {
        return maxJoin;
    }

    public void setMaxJoin(int maxJoin) {
        this.maxJoin = maxJoin;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }
}