package cn.korostudio.msc.setting;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.korostudio.msc.data.Server;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class Setting {
    @Getter
    protected static cn.hutool.setting.Setting setting;


    static {
        setting = new cn.hutool.setting.Setting(FileUtil.touch(System.getProperty("user.dir")+"/msc/setting.setting"), CharsetUtil.CHARSET_UTF_8,true);
    }

    static public void Init(){
        log.info(System.getProperty("user.dir")+"/msc/setting.setting");
    }
}
