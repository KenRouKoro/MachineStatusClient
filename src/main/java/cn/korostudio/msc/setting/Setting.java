package cn.korostudio.msc.setting;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.korostudio.msc.data.Server;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Setting {
    @Getter
    protected static cn.hutool.setting.Setting setting;

    protected static Logger logger ;



    static {
        logger = LoggerFactory.getLogger(Setting.class);
        setting = new cn.hutool.setting.Setting(FileUtil.touch(System.getProperty("user.home")+"/.ms/client/setting.setting"), CharsetUtil.CHARSET_UTF_8,true);
    }

    static public void Init(){
        logger.info(System.getProperty("user.home")+"/.ms/client/setting.setting");
        setting.getStr("test","d");
    }
}
