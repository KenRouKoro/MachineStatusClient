package cn.korostudio.msc.net;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.oshi.OshiUtil;
import cn.korostudio.msc.data.Server;
import cn.korostudio.msc.setting.Setting;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class NetSystem {

    protected static Server server;
    protected static cn.hutool.setting.Setting setting = Setting.getSetting();

    static {
        server = new Server();
    }


    public static void Init() {


        setting.autoLoad(true);

        server.setServerID(setting.getStr("serverID", StrUtil.uuid()));

        log.info("当前服务器ID："+server.getServerID());

        //long time = System.currentTimeMillis();

        CronUtil.setMatchSecond(true);


        CronUtil.schedule("*/1 * * * * * *", new Task() {
            long network_last_in = 0;
            long network_last_out = 0;
            @Override
            public void execute() {

                server.setCustom(setting.getStr("Custom", ""));
                server.setLocation(setting.getStr("location", "中国"));
                server.setServerID(setting.getStr("serverID", StrUtil.uuid()));
                server.setHost(setting.getStr("serverhost", "127.0.0.1"));
                server.setName(setting.getStr("name", "NULL"));
                server.setOnline(true);
                server.setType(setting.getStr("type", "KVM"));
                server.setRegion(setting.getStr("region", "CN"));
                server.setMemory_total(OshiUtil.getMemory().getTotal() / 1024);
                server.setSwap_total(OshiUtil.getMemory().getVirtualMemory().getSwapTotal() / 1024);

                SystemInfo si = new SystemInfo();
                HardwareAbstractionLayer hal = si.getHardware();

                ThreadUtil.execute(()->{
                    List<NetworkIF> network = hal.getNetworkIFs();

                    long network_rx;
                    long network_tx;
                    long network_in = 0;
                    long network_out = 0;

                    for (NetworkIF networkIF : network) {
                        network_out += networkIF.getBytesSent();
                        network_in += networkIF.getBytesRecv();
                    }

                    network_rx = network_in - network_last_in;
                    network_tx = network_out - network_last_out;

                    network_last_in = network_in;
                    network_last_out = network_out;

                    server.setNetwork_in(network_in);
                    server.setNetwork_out(network_out);
                    server.setNetwork_rx(network_rx);
                    server.setNetwork_tx(network_tx);
                });

                server.setLoad(OshiUtil.getProcessor().getSystemLoadAverage(1)[0]);

                server.setCpu((int) (100 - OshiUtil.getCpuInfo().getFree()));
                server.setMemory_used((OshiUtil.getMemory().getTotal() - OshiUtil.getMemory().getAvailable()) / 1024);
                server.setSwap_used(OshiUtil.getMemory().getVirtualMemory().getSwapUsed() / 1024);

                server.setUpdated(System.currentTimeMillis() / 1000);
                server.setUptime(DateUtil.formatBetween(OshiUtil.getOs().getSystemUptime() * 1000, BetweenFormatter.Level.SECOND));

                log.debug("post : " + server.toString());
                Map<String, Object> paramMap = BeanUtil.beanToMap(server);
                paramMap.put("password",setting.getStr("password","korostudio"));
                try {
                    String result = HttpUtil.post(Setting.getSetting().getStr("host", "http://127.0.0.1:8090/update"), paramMap);
                    JSONObject resultObj = JSONUtil.parseObj(result);
                    String status = resultObj.getStr("status","false");

                    if(Objects.equals(status, "false")){
                        log.error("未知故障，程序终止！");
                        System.exit(0);
                    }else if(Objects.equals(status, "error_password")){
                        log.error("密码错误，请修改连接密码后重启！");
                        System.exit(0);
                    }

                } catch (Exception e) {
                    log.error("服务器连接失败",e);
                }
            }
        });

        CronUtil.schedule("* */2 * * * * *", new Task() {

            @Override
            public void execute() {
                File[] files = File.listRoots();
                long using = 0;
                long total = 0;
                for (File file : files) {
                    total += file.getTotalSpace();
                    using += file.getUsableSpace();
                }
                server.setHdd_total(total / 1024 / 1024);
                server.setHdd_used((total - using) / 1024 / 1024);
            }
        });
    }

}
