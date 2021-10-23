package cn.korostudio.msc.net;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.system.oshi.OshiUtil;
import cn.korostudio.msc.data.Server;
import cn.korostudio.msc.setting.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.io.File;
import java.util.List;
import java.util.Map;

public class NetSystem {

    protected static Logger logger;

    protected static Server server;

    static {
        logger = LoggerFactory.getLogger(NetSystem.class);
        server = new Server();
    }


    public static void Init() {
        cn.hutool.setting.Setting setting = Setting.getSetting();
        server.setCustom(setting.getStr("Custom", ""));
        server.setLocation(setting.getStr("location", "中国"));
        server.setServerID(setting.getStr("serverID", "" + server.hashCode()));
        server.setHost(setting.getStr("serverhost", "127.0.0.1"));
        server.setName(setting.getStr("name", "NULL"));
        server.setOnline(true);
        server.setType(setting.getStr("type", "KVM"));
        server.setRegion(setting.getStr("region", "CN"));
        server.setMemory_total(OshiUtil.getMemory().getTotal() / 1024);
        server.setSwap_total(OshiUtil.getMemory().getVirtualMemory().getSwapTotal() / 1024);


        long time = System.currentTimeMillis();

        new Thread(() -> {
            long network_last_in = 0;
            long network_last_out = 0;
            while (true) {
                SystemInfo si = new SystemInfo();
                HardwareAbstractionLayer hal = si.getHardware();
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
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

        new Thread(() -> {
            while (true) {
                server.setLoad(OshiUtil.getProcessor().getSystemLoadAverage(1)[0]);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();


        while (true) {

            server.setCpu((int) (100 - OshiUtil.getCpuInfo().getFree()));


            server.setMemory_used((OshiUtil.getMemory().getTotal() - OshiUtil.getMemory().getAvailable()) / 1024);
            server.setSwap_used(OshiUtil.getMemory().getVirtualMemory().getSwapUsed() / 1024);


            File[] files = File.listRoots();
            long using = 0;
            long total = 0;
            for (File file : files) {
                total += file.getTotalSpace();
                using += file.getUsableSpace();
            }
            server.setHdd_total(total / 1024 / 1024);
            server.setHdd_used((total - using) / 1024 / 1024);

            server.setUpdated(System.currentTimeMillis() / 1000);
            server.setUptime(DateUtil.formatBetween(OshiUtil.getOs().getSystemUptime() * 1000, BetweenFormatter.Level.SECOND));


            logger.debug("post : " + server.toString());
            Map<String, Object> paramMap = BeanUtil.beanToMap(server);
            try {
                String result = HttpUtil.post(Setting.getSetting().getStr("host", "http://127.0.0.1:8090/update"), paramMap);
            } catch (Exception e) {
                logger.error("服务器连接失败,10秒后重试。");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }


            long sleepTime = time + 1000 - System.currentTimeMillis();

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            time = System.currentTimeMillis();
        }
    }

}
