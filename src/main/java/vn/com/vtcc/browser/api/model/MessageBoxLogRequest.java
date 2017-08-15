package vn.com.vtcc.browser.api.model;

import java.util.Date;

/**
 * Created by giang on 04/08/2017.
 */
public class MessageBoxLogRequest {
    private Long id;
    private String jobId;
    private String idFireBase;
    private Date timestamp;
    private String deviceType;
    private String appVersion;

    public MessageBoxLogRequest(Long id, String jobId, String idFireBase, Date timestamp, String deviceType, String appVersion) {
        super();
        this.id = id;
        this.jobId = jobId;
        this.idFireBase = idFireBase;
        this.timestamp = timestamp;
        this.deviceType = deviceType;
        this.appVersion = appVersion;
    }

    public MessageBoxLogRequest() {

    }

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getIdFireBase() {
        return idFireBase;
    }


    public void setIdFireBase(String idFireBase) {
        this.idFireBase = idFireBase;
    }


    public String getJobId() {
        return jobId;
    }

    public void setTimestamp(Date timestamp) {this.timestamp = timestamp;}

    public Date getTimestamp() {return this.timestamp;}

    public void setDeviceType(String deviceType) {this.deviceType = deviceType;}

    public String getDeviceType() {return this.deviceType;}

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setAppVersion(String appVersion) {this.appVersion = appVersion;}

    public String getAppVersion() {return this.appVersion;}


}
