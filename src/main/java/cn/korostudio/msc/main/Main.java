package cn.korostudio.msc.main;

import cn.korostudio.msc.net.NetSystem;
import cn.korostudio.msc.setting.Setting;

public class Main {
    public static void main(String []arg0){
        Setting.Init();
        NetSystem.Init();
    }
}
