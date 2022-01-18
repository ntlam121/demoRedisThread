package obj;

public class Ne {
    int neId;
    long last_update_time;
    String type;
    boolean alarm_sent;
    long alarm_init_time;
    long alarm_trigger_time;
    long alarm_clear_time;
    String alarm_id;
    String alarm_location;

    public int getNeId() {
        return neId;
    }

    public void setNeId(int neId) {
        this.neId = neId;
    }

    public long getLast_update_time() {
        return last_update_time;
    }

    public void setLast_update_time(long last_update_time) {
        this.last_update_time = last_update_time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isAlarm_sent() {
        return alarm_sent;
    }

    public void setAlarm_sent(boolean alarm_sent) {
        this.alarm_sent = alarm_sent;
    }

    public long getAlarm_init_time() {
        return alarm_init_time;
    }

    public void setAlarm_init_time(long alarm_init_time) {
        this.alarm_init_time = alarm_init_time;
    }

    public long getAlarm_trigger_time() {
        return alarm_trigger_time;
    }

    public void setAlarm_trigger_time(long alarm_trigger_time) {
        this.alarm_trigger_time = alarm_trigger_time;
    }

    public long getAlarm_clear_time() {
        return alarm_clear_time;
    }

    public void setAlarm_clear_time(long alarm_clear_time) {
        this.alarm_clear_time = alarm_clear_time;
    }

    public String getAlarm_id() {
        return alarm_id;
    }

    public void setAlarm_id(String alarm_id) {
        this.alarm_id = alarm_id;
    }

    public String getAlarm_location() {
        return alarm_location;
    }

    public void setAlarm_location(String alarm_location) {
        this.alarm_location = alarm_location;
    }

    public Ne(int neId, long last_update_time, String type, boolean alarm_sent, long alarm_init_time, long alarm_trigger_time, long alarm_clear_time, String alarm_id, String alarm_location) {
        this.neId = neId;
        this.last_update_time = last_update_time;
        this.type = type;
        this.alarm_sent = alarm_sent;
        this.alarm_init_time = alarm_init_time;
        this.alarm_trigger_time = alarm_trigger_time;
        this.alarm_clear_time = alarm_clear_time;
        this.alarm_id = alarm_id;
        this.alarm_location = alarm_location;
    }

    public Ne(long last_update_time, String type, boolean alarm_sent, long alarm_init_time, long alarm_trigger_time, long alarm_clear_time, String alarm_id, String alarm_location) {
        this.last_update_time = last_update_time;
        this.type = type;
        this.alarm_sent = alarm_sent;
        this.alarm_init_time = alarm_init_time;
        this.alarm_trigger_time = alarm_trigger_time;
        this.alarm_clear_time = alarm_clear_time;
        this.alarm_id = alarm_id;
        this.alarm_location = alarm_location;
    }

    public Ne() {
    }
}