package cn.korostudio.msc.data;

import lombok.Data;

@Data
public class Server {
    protected String name;
    protected String serverID;
    protected String type;
    protected String host;
    protected String location;
    protected boolean online;
    protected String uptime;
    protected double load;
    protected long network_rx;
    protected long network_tx;
    protected long network_in;
    protected long network_out;
    protected int cpu;
    protected long memory_total;
    protected long memory_used;
    protected long swap_total;
    protected long swap_used;
    protected long hdd_total;
    protected long hdd_used;
    protected String custom;
    protected String region;
    protected long updated;




}
