package ir.aniranmp;

import lombok.Data;

@Data
public class Request {

    public enum Status{
        NONE,
        QUEUE,
        PENDING,
        DONE,
        REJECTED;
    }

    private int id;
    private int executionTime;
    private int creationTime;
    private Status status;
}
