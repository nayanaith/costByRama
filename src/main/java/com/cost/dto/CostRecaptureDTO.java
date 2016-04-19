package com.cost.dto;

import java.util.List;

/**
 * DTO for Cost Recapture
 *
 * @author Rameshkumar.Venkatachalam
 */
public class CostRecaptureDTO {

    private String effectiveDate;

    private boolean isCostZero;

    private String message;

    private String offeringCode;

    private List<String> orderCenter;

    private List<String> profileIdList;

    private String scheduleGroup;

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public String getMessage() {
        return message;
    }

    public String getOfferingCode() {
        return offeringCode;
    }

    public List<String> getOrderCenter() {
        return orderCenter;
    }

    public List<String> getProfileIdList() {
        return profileIdList;
    }

    public String getScheduleGroup() {
        return scheduleGroup;
    }

    public boolean isCostZero() {
        return isCostZero;
    }

    public void setCostZero(final boolean isCostZero) {
        this.isCostZero = isCostZero;
    }

    public void setEffectiveDate(final String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setOfferingCode(final String offeringCode) {
        this.offeringCode = offeringCode;
    }

    public void setOrderCenter(final List<String> orderCenter) {
        this.orderCenter = orderCenter;
    }

    public void setProfileIdList(final List<String> profileIdList) {
        this.profileIdList = profileIdList;
    }

    public void setScheduleGroup(final String scheduleGroup) {
        this.scheduleGroup = scheduleGroup;
    }
}
