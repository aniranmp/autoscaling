package ir.aniranmp;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;


public class Main {

    private static int numberOfInstances = 0;
    private static int[] state = new int[2];
    private static double[][] qTable;
    private static double learningRate = 0.1;
    private static double discountFactor = 0.9;
    private static int countRejected = 0;
    private static int countRequest = 0;
    private static ArrayList<Request> allRequests = new ArrayList<Request>();
    private static ArrayList<Instance> allInstances = new ArrayList<Instance>();

    private static ArrayList<Server> allServers = new ArrayList<Server>();

    private static Random rand = new Random();
    private static Scanner scan = new Scanner(System.in);

    private static int MAX_INSTANCES = 12312;
    private static int MAX_PENDING_REQUESTS = 12312;


    public static void main(String[] args) {

        qTable = new double[MAX_INSTANCES][MAX_PENDING_REQUESTS];
        for (int i = 0; i < MAX_INSTANCES; i++) {
            for (int j = 0; j < MAX_PENDING_REQUESTS; j++) {
                qTable[i][j] = 0.0;
            }
        }

        System.out.println("Please enter t: ");
        int timeSlots = scan.nextInt();

        // Create an instance of the RandomDataGenerator class
        RandomDataGenerator randomData = new RandomDataGenerator();


        // Generate a random number from a Poisson distribution - +1 is for not getting 0 and 1
        int minute = 60;


        for (int i = 0; i < timeSlots; i++) {

            // Set the mean (Lambda) of the Poisson distribution
            System.out.println("Please enter value of lambda: ");
            double lambda = scan.nextDouble();
            long k = randomData.nextPoisson(lambda) + 2;
            System.out.println("K : " + k);
            Request requests[] = requestsGenerator((int) k);
            System.out.println("Please enter algorithm for number of instances: ");
            System.out.println("1: Half of requests");
            System.out.println("2: Aniran's special");
            System.out.println("3: Reinforcement");
            System.out.println("999: Static value of 2");

            // Calling instance number generator
            int type = scan.nextInt();
            instanceGenerator(requests, type);

            if (type == 2) {
                int countQueued = 0;

                ArrayList<Instance> instances = instanceInitializer(numberOfInstances, 0,0);

                for (int j = 0; j < minute; j++) {
                    for (Request request : requests) {
                        System.out.println("Request ID : " + request.getId() + " : " + request.getStatus().name());
                        if (j == request.getCreationTime()) {
                            if (request.getStatus() == Request.Status.NONE) {
                                System.out.println("Request Creation Time : " + request.getCreationTime());
                                for (Instance instance : instances) {
                                    if (!instance.isWorking() && request.getStatus() == Request.Status.NONE) {
                                        instance.setWorking(true);
                                        instance.setRequestId(request.getId());
                                        request.setStatus(Request.Status.PENDING);
                                    }
                                }
                                if (request.getStatus() == Request.Status.NONE) {
                                    request.setStatus(Request.Status.QUEUE);
                                    countQueued++;
                                }
                            }

                        }
                        if (j <= request.getCreationTime() + 2) {
                            if (request.getStatus() == Request.Status.QUEUE) {
                                System.out.println("Request Queued Time : " + (j - request.getCreationTime()));
                                for (Instance instance : instances) {
                                    if (!instance.isWorking()) {
                                        instance.setWorking(true);
                                        instance.setRequestId(request.getId());
                                        request.setStatus(Request.Status.PENDING);
                                        countQueued--;
                                        break;
                                    }
                                }
                            }
                        }
                        if (j >= request.getCreationTime() + request.getExecutionTime()) {
                            if (request.getStatus() == Request.Status.PENDING) {
                                request.setStatus(Request.Status.DONE);
                            }
                        }
                        if (j >= request.getCreationTime() + 2) {
                            if (request.getStatus() == Request.Status.QUEUE) {
                                countRejected++;
                                request.setStatus(Request.Status.REJECTED);
                            }
                        }
                        if (request.getStatus() == Request.Status.DONE) {
                            for (Instance instance : instances) {
                                if (instance.isWorking() && instance.getRequestId() == request.getId()) {
                                    instance.setWorking(false);
                                    instance.setRequestId(0);
                                }
                            }
                        }
                    }

                    increaseOnlineTime();

                    if (instances.size() < countQueued) {
                        int lastInstanceId = allInstances.size();
                        instances.addAll(instanceInitializer(countQueued, lastInstanceId,j));
                    } else if (instances.size() > getNumQueuedAndPendingRequests()) {
                        for (int it = 0; it < instances.size() - getNumQueuedAndPendingRequests(); it++) {
                            for (int iterator = 0; iterator < instances.size(); iterator++) {
                                if (!instances.get(iterator).isWorking()) {
                                    instances.remove(iterator);
                                    break;
                                }
                            }
                        }
                    }

                }
            } else if (type == 3) {
                ArrayList<Instance> instances = instanceInitializer(numberOfInstances, 0,0);
                allInstances.addAll(instances);
                // Implement Q-learning
                for (int j = 0; j < minute; j++) {

                    increaseOnlineTime();

                    if (instances.size() < numberOfInstances) {
                        int lastInstanceId = instances.size();
                        instances.addAll(instanceInitializer(numberOfInstances - instances.size(), lastInstanceId,j));
                    } else if (instances.size() > numberOfInstances) {
                        for (int it = 0; it < instances.size() - numberOfInstances; it++) {
                            for (int iterator = 0; iterator < instances.size(); iterator++) {
                                if (!instances.get(iterator).isWorking()) {
                                    instances.remove(iterator);
                                    break;
                                }
                            }
                        }
                    }
                    for (Request request : requests) {
                        System.out.println("Request ID : " + request.getId() + " : " + request.getStatus().name());
                        if (j == request.getCreationTime()) {
                            if (request.getStatus() == Request.Status.NONE) {
                                System.out.println("Request Creation Time : " + request.getCreationTime());
                                for (Instance instance : instances) {
                                    if (!instance.isWorking() && request.getStatus() == Request.Status.NONE) {
                                        instance.setWorking(true);
                                        instance.setRequestId(request.getId());
                                        request.setStatus(Request.Status.PENDING);
                                    }
                                }
                                if (request.getStatus() == Request.Status.NONE) {
                                    request.setStatus(Request.Status.QUEUE);
                                }
                            }

                        }
                        if (j <= request.getCreationTime() + 2) {
                            if (request.getStatus() == Request.Status.QUEUE) {
                                System.out.println("Request Queued Time : " + (j - request.getCreationTime()));
                                for (Instance instance : instances) {
                                    if (!instance.isWorking()) {
                                        instance.setWorking(true);
                                        instance.setRequestId(request.getId());
                                        request.setStatus(Request.Status.PENDING);
                                        break;
                                    }
                                }
                            }
                        }
                        if (j >= request.getCreationTime() + request.getExecutionTime()) {
                            if (request.getStatus() == Request.Status.PENDING) {
                                request.setStatus(Request.Status.DONE);
                            }
                        }
                        if (j >= request.getCreationTime() + 2) {
                            if (request.getStatus() == Request.Status.QUEUE) {
                                countRejected++;
                                request.setStatus(Request.Status.REJECTED);
                            }
                        }
                        if (request.getStatus() == Request.Status.DONE) {
                            for (Instance instance : instances) {
                                if (instance.isWorking() && instance.getRequestId() == request.getId()) {
                                    instance.setWorking(false);
                                    instance.setRequestId(0);
                                }
                            }
                        }
                    }


                    // Update the state
                    state[0] = numberOfInstances;
                    state[1] = getNumQueuedAndPendingRequests();
                    System.out.println("state[0]: " + state[0] + ", state[1]: " + state[1]);

                    // Select an action using epsilon-greedy exploration
                    int action = selectAction(state);

                    // Simulate the effect of the action
                    if (action == -1) {
                        if (numberOfInstances > 1)
                            numberOfInstances--;
                    } else if (action == 1) {
                        numberOfInstances += 10;
                    }
                    System.out.println("Number of Instances : " + numberOfInstances);
                    // Update the Q-table
                    int nextState[] = {numberOfInstances, getNumQueuedAndPendingRequests()};
                    double reward = getReward(nextState);
                    double currentQ = qTable[state[0]][state[1]];
                    double maxNextQ = getMaxQ(nextState);
                    double newQ = currentQ + learningRate * (reward + discountFactor * maxNextQ - currentQ);
                    qTable[state[0]][state[1]] = newQ;
                }
            } else if (type == 1 || type == 999) {
                Instance instances[] = new Instance[numberOfInstances];
                for (int it = 0; it < numberOfInstances; it++) {
                    instances[it] = new Instance();
                    instances[it].setId(it);
                    instances[it].setOnlineTime(0);
                    instances[it].setWorking(false);
                    instances[it].setDateCreated(0);
                    instances[it].setSleepCount(100);
                    allInstances.add(instances[it]);
                }

                for (int j = 0; j < minute; j++) {
                    // Needs to check if instance is full for the one of the requests
                    System.out.println("Second : " + j);

                    increaseOnlineTime();

                    for (Request request : requests) {
                        if (request.getStatus() == Request.Status.NONE) {
                            System.out.println("Request ID : " + request.getId() + " : NONE");
                            if (j == request.getCreationTime()) {
                                System.out.println("Request Creation Time : " + request.getCreationTime());
                                for (Instance instance : instances) {
                                    if (!instance.isWorking() && !instance.isOffline() && request.getStatus() == Request.Status.NONE) {
                                        instance.setWorking(true);
                                        instance.setRequestId(request.getId());
                                        request.setStatus(Request.Status.PENDING);
                                        System.out.println("Request ID : " + request.getId() + " : PENDING");
                                    }
                                }
                                if (request.getStatus() == Request.Status.NONE) {
                                    countRejected++;
                                    request.setStatus(Request.Status.REJECTED);
                                    System.out.println("Request ID : " + request.getId() + " : REJECTED");

                                }
                            }
                        }
                        if (request.getStatus() == Request.Status.PENDING) {
                            System.out.println("Request ID : " + request.getId() + " : PENDING");
                            if (j >= request.getCreationTime() + request.getExecutionTime()) {
                                request.setStatus(Request.Status.DONE);
                            }
                        }
                        if (request.getStatus() == Request.Status.DONE) {
                            System.out.println("Request ID : " + request.getId() + " : DONE");
                            for (Instance instance : instances) {
                                if (instance.isWorking() && instance.getRequestId() == request.getId()) {
                                    instance.setWorking(false);
                                    instance.setRequestId(0);
                                    instance.setSleepCount(100);

                                }
                            }
                        }
                    }
                    for (int in = 0; in < instances.length; in++) {
                        if (!instances[in].isWorking()) {
                            instances[in].setSleepCount(instances[in].getSleepCount() - 1);
                        }
                        if (instances[in].getSleepCount() <= 0) {
                            instances[in].setOffline(true);
                        }
                    }
                }
            }

            Server server = new Server();
            server.setInstances(allInstances);
            server.setCountRejects(countRejected);
            server.setCountRequests(countRequest);
            server.setRequests(allRequests);
            server.setAlgorithmType(type);
            allServers.add(server);
            countRequest = 0;
            countRejected = 0;
            cleanAllInstancesAndRequests();
        }
        performanceCheck();
    }

    public static void instanceGenerator(Request[] requests, int type) {
        if (type == 1) {
            numberOfInstances = requests.length / 2;
        } else if (type == 2) {
            numberOfInstances = requests.length / 60;
        } else if (type == 3) {
            numberOfInstances = requests.length / 60;
//            numberOfInstances = rand.nextInt(100);
        } else if (type == 999) {
            numberOfInstances = 2;
        }
    }

    private static int selectAction(int[] state) {
        // Select a random action with probability epsilon,
        // otherwise select the action with the highest Q-value
        if (rand.nextDouble() < 0.01) {
            return rand.nextInt(3) - 1;
        } else {
            double maxQ = Double.NEGATIVE_INFINITY;
            int bestAction = -1;
            for (int a = -1; a <= 1; a++) {
                int nextNumInstances = numberOfInstances + a;
                if (nextNumInstances >= 0) {
                    double qValue = qTable[nextNumInstances][state[1]];
                    if (qValue > maxQ) {
                        maxQ = qValue;
                        bestAction = a;
                    }
                }
            }
            return bestAction;
        }
    }

    private static int getNumQueuedAndPendingRequests() {
        int count = 0;
        for (Request request : allRequests) {
            if (request.getStatus() == Request.Status.QUEUE || request.getStatus() == Request.Status.PENDING) {
                count++;
            }
        }
        return count;
    }

    private static double getReward(int[] nextState) {
        int numRejected = 0;
        for (Request request : allRequests) {
            if (request.getStatus() == Request.Status.REJECTED) {
                numRejected++;
            }
        }
        int numPending = nextState[1];
        int reward = -numRejected;
        if (numPending == 0) {
            reward += 5;
        }
        return reward;
    }

    private static double getMaxQ(int[] nextState) {
        double maxQ = Double.NEGATIVE_INFINITY;
        for (int a = -1; a <= 1; a++) {
            int nextNumInstances = numberOfInstances + a;
            if (nextNumInstances >= 0) {
                double qValue = qTable[nextNumInstances][nextState[1]];
                if (qValue > maxQ) {
                    maxQ = qValue;
                }
            }
        }
        return maxQ;
    }

    private static void increaseOnlineTime() {
        for (Instance instance : allInstances) {
            if (instance.isWorking()) {
                instance.setOnlineTime(instance.getOnlineTime() + 1);
            }
        }
    }

    private static void cleanAllInstancesAndRequests() {
        allInstances = new ArrayList<>();
        allRequests = new ArrayList<>();
    }

    private static ArrayList<Instance> instanceInitializer(int number, int increment, int dateCreated) {
        ArrayList<Instance> instances = new ArrayList<>();
        for (int ins = 0; ins < number; ins++) {
            Instance instance = new Instance();
            instance.setId(ins + increment);
            instance.setWorking(false);
            instance.setDateCreated(dateCreated);
            instance.setSleepCount(100);
            instance.setOnlineTime(0);
            instances.add(instance);
        }
        allInstances.addAll(instances);
        return instances;
    }

    private static Request[] requestsGenerator(int k) {
        Request requests[] = new Request[k];
        for (int l = 0; l < k; l++) {
            requests[l] = new Request();
            requests[l].setId(countRequest++);
            requests[l].setCreationTime(rand.nextInt(56));
            requests[l].setExecutionTime(rand.nextInt(4));
            requests[l].setStatus(Request.Status.NONE);
            allRequests.add(requests[l]);
        }
        return requests;
    }

    private static void printDetails() {
        for (Server server : allServers) {
            ArrayList<Instance> instances = server.getInstances();
            for (Instance instance : instances) {
                System.out.println("Instance Id : " + instance.getId() + " Active time " + instance.getOnlineTime());
            }

            String algorithmTypeString = (server.getAlgorithmType() == 1) ? " Half of requests" :
                    (server.getAlgorithmType() == 2) ? " Aniran's special" :
                            (server.getAlgorithmType() == 3) ? " Reinforcement" :
                                    (server.getAlgorithmType() == 999) ? " Static value of 2" :
                                            "Algorithm not provided";
            System.out.println("Algorithm Type : " + algorithmTypeString);
            System.out.println("Rejected at all    : " + server.getCountRejects());
            System.out.println("Number of requests : " + server.getCountRequests());
        }
    }

    private static void performanceCheck() {

        for (Server server : allServers) {
            int completedRequests = server.getCountRequests() - server.getCountRejects();
            int numberOfInstances = server.getInstances().size();
            double performance = 0;
            if (numberOfInstances != 0)
                performance = completedRequests / numberOfInstances;
            else
                System.out.println("Divide by zero");
            String algorithmTypeString = (server.getAlgorithmType() == 1) ? " Half of requests" :
                    (server.getAlgorithmType() == 2) ? " Aniran's special" :
                            (server.getAlgorithmType() == 3) ? " Reinforcement" :
                                    (server.getAlgorithmType() == 999) ? " Static value of 2" :
                                            "Algorithm not provided";
            System.out.println("Performance for algorithm : " + algorithmTypeString + " : ");
            System.out.println(performance);
        }


        System.out.println("Please choose: ");
        System.out.println("1- Show Details of Instances, Rejects, Requests, ...");
        System.out.println("2- End");
        int showDetails = scan.nextInt();
        if (showDetails == 1) {
            printDetails();
        }
    }
}
