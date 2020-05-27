package node;

import java.util.List;

public class Cluster {

    private List<String> others;

    private String myself;

    private String leader;

    public Cluster() {
    }

    public Cluster(String myself, List<String> others, String leader) {
        this.myself = myself;
        this.others = others;
        this.leader = leader;
    }

    public List<String> getOthers() {
        return others;
    }

    public void setOthers(List<String> others) {
        this.others = others;
    }

    public String getMyself() {
        return myself;
    }

    public void setMyself(String myself) {
        this.myself = myself;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }
}
