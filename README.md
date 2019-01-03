# house
elasticsearch

## mysql安装相关脚本
```
[mysql]
default-character-set=utf-8
[mysqld]
port = 3306
basedir = E:\mysql-5.7.24-winx64
datadir = E:\mysql-5.7.24-winx64\data
max_connections=200
character-set-server=utf-8
default-storage-engine=INNODB

skip-gran-tables
```

## docker 安装redis

- docker pull redis:3.2
- 开启6379端口
- docker run -p 6379:6379 -v $PWD/redis/data:/data -d redis:3.2 redis-server --appendonly yes

```aidl
-p 6379:6379 : 将容器的6379端口映射到主机的6379端口

-v $PWD/data:/data : 将主机中当前目录下的data挂载到容器的/data

redis-server --appendonly yes : 在容器执行redis-server启动命令，并打开redis持久化配置
```

