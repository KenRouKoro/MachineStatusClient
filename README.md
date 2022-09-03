# MachineStatusClient
### MachineStatus服务器监控的客户端，一个开箱即用的服务器监控系统客户端。支持Windows • Linux • macOS(x86 And ARM M1) • Unix (AIX, FreeBSD, OpenBSD, Solaris)
## 如何部署
### 1.依赖检查
目前运行 MachineStatusClient 的最低依赖要求为 JRE 17，请务必确保在进行下面操作之前已经正确安装了 JRE。

目前介绍两种 Linux 发行版的安装方式，均为 OpenJRE。(推荐使用JDK17)

#### CentOS
```shell
sudo yum install java-latest-openjdk -y
```
检查版本：
```shell
java -version
```
如果输出以下类似内容即代表成功
```shell
openjdk version "17" 2021-09-14
```
#### Ubuntu
```shell
sudo apt-get install openjdk-latest-jre -y
```
#### 检查版本：
```shell
java -version
```
如果输出以下类似内容即代表成功
```shell
openjdk version "17" 2021-09-14
```
### 2.建立目录结构
创建存放 运行包 的目录，这里以 ~/app 为例
```shell
mkdir ~/app && cd ~/app
```
下载运行包
```shell
wget https://github.com/KenRouKoro/MachineStatusClient/releases/download/0.1-Alpha/MSC-0.1Alpha.jar -O MSC.jar
```
创建 工作目录
```shell
mkdir ~/.ms/client && cd ~/.ms/client
```
下载示例配置文件到工作目录
```shell
wget https://file.korostudio.cn/setting_1635086019475.setting -O ./setting.setting
```
编辑配置文件，配置自定义数据，请按照注释提示填写
```shell
vim setting.setting
```
```yaml
#地址，支持中文
location = 中国
  #显示名称
name = 客户端自定义名称
  #设备类型，支持中文
type = xx云.png
  #区域，这个控制旗帜显示
region = CN
  #服务器唯一ID，这个是从客户端向服务端注册的，用于标识客户端，由客户端设置
serverID = koro
  #服务端地址，需要在后面加上子目录/update，支持cdn，反向代理
host = http://127.0.0.1:3620/update
```
****
**请注意！在客户端第一次开启后，serverID不可更改，其他可以更改。**
****
### 3.测试运行 MachineStatusClient
```shell
cd ~/app && java -jar MSC.jar
```
无报错即可
## 4.作为服务运行
1.下载 MachineStatus 官方的 msc.service 模板
```shell
wget  https://file.korostudio.cn/msc_1635086196488.service -O /etc/systemd/system/msc.service
```
2.修改 mss.service
```shell
vim /etc/systemd/system/msc.service
```
3.修改配置
YOUR_JAR_PATH：MachineStatus 运行包的绝对路径，例如 /root/app/MSC.jar，注意：此路径不支持 ~ 符号。
```yaml
[Unit]
Description=MSS Service
Documentation=https://github.com/KenRouKoro/MachineStatus
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
ExecStart=/usr/bin/java -server -Xms256m -Xmx256m -jar YOUR_JAR_PATH
ExecStop=/bin/kill -s QUIT $MAINPID
Restart=always
StandOutput=syslog

StandError=inherit

[Install]
WantedBy=multi-user.target
```
4.重新加载 systemd
```shell
systemctl daemon-reload
```
5.运行服务
```shell
systemctl start msc
```
6.在系统启动时启动服务
```shell
systemctl enable msc
```
您可以查看服务日志检查启动状态
```shell
journalctl -n 20 -u msc
```