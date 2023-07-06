package ir.aniranmp;


import lombok.Data;

import java.util.ArrayList;

@Data
public class Server {
    private int id;
    private ArrayList<Instance> instances;
    private ArrayList<Request> requests;
    private int algorithmType;
    private int countRejects;
    private int countRequests;
}
