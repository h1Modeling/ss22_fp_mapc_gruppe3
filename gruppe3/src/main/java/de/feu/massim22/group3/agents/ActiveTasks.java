package massim.javaagents.agents;

import java.util.ArrayList;

public class ActiveTasks {
    public ArrayList<Task> tasks = new ArrayList<Task>();
}

class Task {
    public String name;
    public int deadline;
    public int reward;
    public ArrayList<Block> blocks = new ArrayList<Block>();
    
    Task(String name, int deadline, int reward) {
        this.name = name;
        this.deadline = deadline;
        this.reward = reward;
    }
}

class Block {
    public int x;
    public int y;
    public String type;
    
    Block(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}