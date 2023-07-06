package ir.aniranmp;

import lombok.Data;

@Data
public class Instance {
    private boolean working;
    private boolean offline;
    private int id;
    private int dateCreated;
    private int requestId;
    private int sleepCount;
    private int onlineTime;
}
