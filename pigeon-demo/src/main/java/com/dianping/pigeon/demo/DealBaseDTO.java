/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class DealBaseDTO implements Serializable {

    private static final long serialVersionUID = 2178057282673375790L;

    private int dealId;
    private int dealGroupId;
    private String shortTitle;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private BigDecimal cost;

    private int maxJoin;
    private int currentJoin;

    /**
     * 1 正常生成券 2 生成制定券/导码单 3 无序列码券 4 tuangou card 5 普通对接单 6 导码对接单 9 美团发券 
     */
    private int receiptType;

    /**
     * 0 非快递单 1 快递单
     */
    private int deliverType;

    /**
     * 新单子都是1
     */
    private int dealStatus;
    /**
     * 团购券有效期的开始日期
     */
    private Date receiptBeginDate;
    /**
     * 团购券有效期的结束日期
     */
    private Date receiptEndDate;

    /**
     * 自动退款日期
     */
    private Date refundDate;

    /**
     * 是否提供发票 0:不提供 1:提供
     */
    private boolean provideInvoice;

    /**
     * 第三方ID
     */
    private int thirdPartyId;


    private int dealGroupPublishVersion;

    public int getDealId() {
        return dealId;
    }

    public void setDealId(int dealId) {
        this.dealId = dealId;
    }

    public int getDealGroupId() {
        return dealGroupId;
    }

    public void setDealGroupId(int dealGroupId) {
        this.dealGroupId = dealGroupId;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(BigDecimal marketPrice) {
        this.marketPrice = marketPrice;
    }

    public int getDealStatus() {
        return dealStatus;
    }

    public void setDealStatus(int dealStatus) {
        this.dealStatus = dealStatus;
    }

    public Date getReceiptBeginDate() {
        return receiptBeginDate;
    }

    public void setReceiptBeginDate(Date receiptBeginDate) {
        this.receiptBeginDate = receiptBeginDate;
    }

    public Date getReceiptEndDate() {
        return receiptEndDate;
    }

    public void setReceiptEndDate(Date receiptEndDate) {
        this.receiptEndDate = receiptEndDate;
    }

    public Date getRefundDate() {
        return refundDate;
    }

    public void setRefundDate(Date refundDate) {
        this.refundDate = refundDate;
    }

    public int getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(int receiptType) {
        this.receiptType = receiptType;
    }

    public int getDeliverType() {
        return deliverType;
    }

    public void setDeliverType(int deliverType) {
        this.deliverType = deliverType;
    }

    public boolean isProvideInvoice() {
        return provideInvoice;
    }

    public void setProvideInvoice(boolean provideInvoice) {
        this.provideInvoice = provideInvoice;
    }

    public int getThirdPartyId() {
        return thirdPartyId;
    }

    public void setThirdPartyId(int thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }

    public int getDealGroupPublishVersion() {
        return dealGroupPublishVersion;
    }

    public void setDealGroupPublishVersion(int dealGroupPublishVersion) {
        this.dealGroupPublishVersion = dealGroupPublishVersion;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public int getMaxJoin() {
        return maxJoin;
    }

    public void setMaxJoin(int maxJoin) {
        this.maxJoin = maxJoin;
    }

    public int getCurrentJoin() {
        return currentJoin;
    }

    public void setCurrentJoin(int currentJoin) {
        this.currentJoin = currentJoin;
    }
}
