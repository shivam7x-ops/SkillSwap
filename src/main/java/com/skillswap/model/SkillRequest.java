package com.skillswap.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for a skill-exchange request between two students.
 *
 * <p>Stored in Firebase under {@code /requests/{requestId}}.
 *
 * <h3>Lifecycle</h3>
 * <pre>
 *   PENDING  ──▶  ACCEPTED
 *        │
 *        └──▶  REJECTED
 * </pre>
 */
public class SkillRequest {

    /** Possible states of a request. */
    public enum Status { PENDING, ACCEPTED, REJECTED }

    private String requestId;
    private String senderUid;
    private String senderName;
    private String receiverUid;
    private String receiverName;
    private String skill;          // the skill being exchanged
    private String status;         // stored as a String for Firebase compat
    private long   timestamp;

    /** No-arg constructor for Firebase deserialisation. */
    public SkillRequest() { }

    public SkillRequest(String requestId,
                        String senderUid, String senderName,
                        String receiverUid, String receiverName,
                        String skill) {
        this.requestId   = requestId;
        this.senderUid   = senderUid;
        this.senderName  = senderName;
        this.receiverUid = receiverUid;
        this.receiverName= receiverName;
        this.skill       = skill;
        this.status      = Status.PENDING.name();
        this.timestamp   = System.currentTimeMillis();
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public String getRequestId()                        { return requestId; }
    public void   setRequestId(String requestId)        { this.requestId = requestId; }

    public String getSenderUid()                        { return senderUid; }
    public void   setSenderUid(String senderUid)        { this.senderUid = senderUid; }

    public String getSenderName()                       { return senderName; }
    public void   setSenderName(String senderName)      { this.senderName = senderName; }

    public String getReceiverUid()                      { return receiverUid; }
    public void   setReceiverUid(String receiverUid)    { this.receiverUid = receiverUid; }

    public String getReceiverName()                     { return receiverName; }
    public void   setReceiverName(String receiverName)  { this.receiverName = receiverName; }

    public String getSkill()                            { return skill; }
    public void   setSkill(String skill)                { this.skill = skill; }

    public String getStatus()                           { return status; }
    public void   setStatus(String status)              { this.status = status; }

    public long   getTimestamp()                         { return timestamp; }
    public void   setTimestamp(long timestamp)           { this.timestamp = timestamp; }

    // ── Convenience ────────────────────────────────────────────────────

    public Status getStatusEnum() {
        try {
            return Status.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException e) {
            return Status.PENDING;
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("requestId",    requestId);
        map.put("senderUid",    senderUid);
        map.put("senderName",   senderName);
        map.put("receiverUid",  receiverUid);
        map.put("receiverName", receiverName);
        map.put("skill",        skill);
        map.put("status",       status);
        map.put("timestamp",    timestamp);
        return map;
    }

    @Override
    public String toString() {
        return "SkillRequest{" +
                "requestId='" + requestId + '\'' +
                ", sender='" + senderName + '\'' +
                ", receiver='" + receiverName + '\'' +
                ", skill='" + skill + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
