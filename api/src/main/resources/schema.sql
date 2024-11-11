SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 文件表
-- ----------------------------
DROP TABLE IF EXISTS lin_file;
CREATE TABLE lin_file
(
    id          int(10) unsigned NOT NULL AUTO_INCREMENT,
    path        varchar(500)     NOT NULL,
    type        varchar(10)      NOT NULL DEFAULT 'LOCAL' COMMENT 'LOCAL 本地，REMOTE 远程',
    name        varchar(100)     NOT NULL,
    extension   varchar(50)               DEFAULT NULL,
    size        int(11)                   DEFAULT NULL,
    md5         varchar(40)               DEFAULT NULL COMMENT 'md5值，防止上传重复文件',
    create_time datetime(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time datetime(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    delete_time datetime(3)               DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY md5_del (md5, delete_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- 日志表
-- ----------------------------
DROP TABLE IF EXISTS lin_log;
CREATE TABLE lin_log
(
    id          int(10) unsigned NOT NULL AUTO_INCREMENT,
    message     varchar(450)              DEFAULT NULL,
    user_id     int(10) unsigned NOT NULL,
    username    varchar(24)               DEFAULT NULL,
    status_code int(11)                   DEFAULT NULL,
    method      varchar(20)               DEFAULT NULL,
    path        varchar(50)               DEFAULT NULL,
    permission  varchar(100)              DEFAULT NULL,
    create_time datetime(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time datetime(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    delete_time datetime(3)               DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- 权限表
-- ----------------------------
DROP TABLE IF EXISTS lin_permission;
CREATE TABLE lin_permission
(
    id          int(10) unsigned NOT NULL AUTO_INCREMENT,
    name        varchar(60)      NOT NULL COMMENT '权限名称，例如：访问首页',
    module      varchar(50)      NOT NULL COMMENT '权限所属模块，例如：人员管理',
    mount       tinyint(1)       NOT NULL DEFAULT 1 COMMENT '0：关闭 1：开启',
    create_time datetime(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time datetime(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    delete_time datetime(3)               DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- 分组表
-- ----------------------------
DROP TABLE IF EXISTS lin_group;
CREATE TABLE lin_group
(
    id          int(10) unsigned NOT NULL AUTO_INCREMENT,
    name        varchar(60)      NOT NULL COMMENT '分组名称，例如：搬砖者',
    info        varchar(255)     DEFAULT NULL COMMENT '分组信息：例如：搬砖的人',
    level       tinyint(2)       NOT NULL DEFAULT 3 COMMENT '分组级别 1：root 2：guest 3：user  root（root、guest分组只能存在一个)',
    create_time datetime(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time datetime(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    delete_time datetime(3)      DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY name_del (name, delete_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- 分组-权限表
-- ----------------------------
DROP TABLE IF EXISTS lin_group_permission;
CREATE TABLE lin_group_permission
(
    id            int(10) unsigned NOT NULL AUTO_INCREMENT,
    group_id      int(10) unsigned NOT NULL COMMENT '分组id',
    permission_id int(10) unsigned NOT NULL COMMENT '权限id',
    PRIMARY KEY (id),
    KEY group_id_permission_id (group_id, permission_id) USING BTREE COMMENT '联合索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- 用户基本信息表
-- ----------------------------
DROP TABLE IF EXISTS lin_user;
CREATE TABLE lin_user
(
    id          int(10) unsigned NOT NULL AUTO_INCREMENT,
    username    varchar(24)      NOT NULL COMMENT '用户名，唯一',
    nickname    varchar(24)               DEFAULT NULL COMMENT '用户昵称',
    avatar      varchar(500)              DEFAULT NULL COMMENT '头像url',
    email       varchar(100)              DEFAULT NULL COMMENT '邮箱',
    create_time datetime(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time datetime(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    delete_time datetime(3)               DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY username_del (username, delete_time),
    UNIQUE KEY email_del (email, delete_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- 用户授权信息表
# id
# user_id
# identity_type 登录类型（手机号 邮箱 用户名）或第三方应用名称（微信 微博等）
# identifier 标识（手机号 邮箱 用户名或第三方应用的唯一标识）
# credential 密码凭证（站内的保存密码，站外的不保存或保存token）
-- ----------------------------
DROP TABLE IF EXISTS lin_user_identity;
CREATE TABLE lin_user_identity
(
    id            int(10) unsigned NOT NULL AUTO_INCREMENT,
    user_id       int(10) unsigned NOT NULL COMMENT '用户id',
    identity_type varchar(100)     NOT NULL,
    identifier    varchar(100),
    credential    varchar(100),
    create_time   datetime(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time   datetime(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    delete_time   datetime(3)               DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;


-- ----------------------------
-- 用户-分组表
-- ----------------------------
DROP TABLE IF EXISTS lin_user_group;
CREATE TABLE lin_user_group
(
    id       int(10) unsigned NOT NULL AUTO_INCREMENT,
    user_id  int(10) unsigned NOT NULL COMMENT '用户id',
    group_id int(10) unsigned NOT NULL COMMENT '分组id',
    PRIMARY KEY (id),
    KEY user_id_group_id (user_id, group_id) USING BTREE COMMENT '联合索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- 图书表
-- ----------------------------
DROP TABLE IF EXISTS book;
CREATE TABLE book
(
    id          int(11)     NOT NULL AUTO_INCREMENT,
    title       varchar(50) NOT NULL,
    author      varchar(30)          DEFAULT NULL,
    summary     varchar(1000)        DEFAULT NULL,
    image       varchar(100)         DEFAULT NULL,
    create_time datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    delete_time datetime(3)          DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

INSERT INTO book(`title`, `author`, `summary`, `image`) VALUES ('深入理解计算机系统', 'Randal E.Bryant', '从程序员的视角，看计算机系统！\n本书适用于那些想要写出更快、更可靠程序的程序员。通过掌握程序是如何映射到系统上，以及程序是如何执行的，读者能够更好的理解程序的行为为什么是这样的，以及效率低下是如何造成的。', 'https://img3.doubanio.com/lpic/s1470003.jpg');
INSERT INTO book(`title`, `author`, `summary`, `image`) VALUES ('C程序设计语言', '（美）Brian W. Kernighan', '在计算机发展的历史上，没有哪一种程序设计语言像C语言这样应用广泛。本书原著即为C语言的设计者之一Dennis M.Ritchie和著名计算机科学家Brian W.Kernighan合著的一本介绍C语言的权威经典著作。', 'https://img3.doubanio.com/lpic/s1106934.jpg');

-- ----------------------------
-- 插入超级管理员
-- 插入root分组
-- ----------------------------
BEGIN;

INSERT INTO lin_user(id, username, nickname)
VALUES (1, 'root', 'root');

INSERT INTO lin_user_identity (id, user_id, identity_type, identifier, credential)
VALUES (1, 1, 'USERNAME_PASSWORD', 'root',
        'pbkdf2sha256:64000:18:24:n:yUnDokcNRbwILZllmUOItIyo9MnI00QW:6ZcPf+sfzyoygOU8h/GSoirF');

INSERT INTO lin_group(id, name, info, level)
VALUES (1, 'root', '超级用户组', 1);

INSERT INTO lin_group(id, name, info, level)
VALUES (2, 'guest', '游客组', 2);

INSERT INTO lin_user_group(id, user_id, group_id)
VALUES (1, 1, 1);

COMMIT;

-- ----------------------------
-- 工程表
-- ----------------------------
CREATE TABLE `project` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `name` varchar(50) NOT NULL COMMENT '项目名称',
   `creator` int(11) DEFAULT NULL COMMENT '创建人员',
   `description` varchar(1000) DEFAULT NULL COMMENT '项目描述',
   `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
   `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
   `delete_time` datetime(3) DEFAULT NULL COMMENT '删除时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 压力机表
-- ----------------------------
CREATE TABLE `machine` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `name` varchar(50) NOT NULL COMMENT '机器名称',
   `address` varchar(30) NOT NULL COMMENT '工作节点地址',
   `path` varchar(2000) DEFAULT NULL COMMENT 'jmeter路径',
   `version` varchar(30) DEFAULT NULL COMMENT '版本',
   `is_online` tinyint(2) NOT NULL DEFAULT '0' COMMENT '在线状态 1：在线 0：离线',
   `jmeter_status` tinyint(2) NOT NULL DEFAULT '0' COMMENT 'jmeter状态',
   `client_id` varchar(36) DEFAULT NULL COMMENT 'socketIO sessionId 连接标识',
   `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
   `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
   `delete_time` datetime(3) DEFAULT NULL COMMENT '删除时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

INSERT INTO `easy-jmeter`.machine (id, name, address, path, version, is_online, jmeter_status, client_id, create_time, update_time, delete_time) VALUES (1, '172.25.0.91', '172.25.0.91', '/opt/apache-jmeter', '5.6.2', 1, 0, 'd0947a00-f375-4129-82b3-8e907f4cbbdc', '2024-04-04 21:41:49.477', '2024-11-11 21:14:10.000', null);
INSERT INTO `easy-jmeter`.machine (id, name, address, path, version, is_online, jmeter_status, client_id, create_time, update_time, delete_time) VALUES (2, '172.25.0.92', '172.25.0.92', '/opt/apache-jmeter', '5.6.2', 1, 0, '5c598572-56bd-4183-b951-44d5bfb40e4f', '2024-04-04 21:41:56.725', '2024-11-11 21:14:10.000', null);

-- ----------------------------
-- 文件表
-- ----------------------------
CREATE TABLE `file` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL COMMENT '原始文件名称',
    `type` varchar(50) DEFAULT NULL COMMENT '文件类型',
    `path` varchar(500) NOT NULL COMMENT '文件路径',
    `url` varchar(500) NOT NULL COMMENT '访问全路径',
    `size` bigint(20) DEFAULT NULL COMMENT '文件大小',
    `cut` tinyint(1) DEFAULT 0 NOT NULL comment '是否切分',
    `origin_id` int(11) DEFAULT NULL COMMENT '原始文件id',
    `task_id` varchar(50) DEFAULT NULL COMMENT '切分文件所属任务id',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `delete_time` datetime(3) DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 用例表
-- ----------------------------
CREATE TABLE `case` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL COMMENT '名称',
    `creator` int(11) NOT NULL COMMENT '创建人',
    `project` int(11) NOT NULL COMMENT '所属工程',
    `jmx` varchar(50) DEFAULT NULL COMMENT 'jmx文件id,逗号分割',
    `csv` varchar(50) DEFAULT NULL COMMENT 'csv文件id,逗号分割',
    `jar` varchar(50) DEFAULT NULL COMMENT 'jar文件id,逗号分割',
    `status` tinyint(2) NOT NULL DEFAULT '0' COMMENT 'jmeter状态',
    `description` varchar(500) DEFAULT NULL COMMENT '描述',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `delete_time` datetime(3) DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 任务表
-- ----------------------------
CREATE TABLE `task` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `task_id` varchar(50) NOT NULL COMMENT '任务标识',
    `creator` int(11) NOT NULL COMMENT '创建人',
    `jmeter_case` int(11) NOT NULL COMMENT '所属用例',
    `jmx` varchar(50) DEFAULT NULL COMMENT 'jmx文件id,逗号分割',
    `csv` varchar(50) DEFAULT NULL COMMENT 'csv文件id,逗号分割',
    `jar` varchar(50) DEFAULT NULL COMMENT 'jar文件id,逗号分割',
    `num_threads` int(11) NOT NULL COMMENT '并发线程数',
    `duration` int(11) NOT NULL COMMENT '压测时长',
    `ramp_time` int(11) DEFAULT 0 COMMENT '过渡时间',
    `qps_limit` int(11) DEFAULT 0 COMMENT 'QPS限制',
    `machine` varchar(50) DEFAULT NULL COMMENT '压力机文件id,逗号分割',
    `machine_num` int(11) NOT NULL DEFAULT 1 COMMENT '压力机数量',
    `granularity` int(11) NOT NULL DEFAULT '0' COMMENT '生成报告的时间采样颗粒度',
    `realtime` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否显示实时数据',
    `log_level` tinyint(2) NOT NULL DEFAULT '4' COMMENT 'jmeter运行日志等级 1-OFF|2-ERROR|3-WARN|4-INFO|5-DEBUG',
    `remark` varchar(500) DEFAULT NULL COMMENT '描述',
    `result` tinyint(2) NOT NULL DEFAULT '0' COMMENT '测试执行结果 0-进行中|1-成功|2-异常终止|3-手动终止',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `delete_time` datetime(3) DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_task_task_id ON task (task_id);

-- ----------------------------
-- 任务日志表
-- ----------------------------
CREATE TABLE `task_log` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `task_id` varchar(50) NOT NULL COMMENT '任务标识',
    `j_case` int(11) NOT NULL COMMENT '所属用例',
    `status` tinyint(2) NOT NULL DEFAULT '0' COMMENT 'jmeter状态',
    `result` tinyint(2) COMMENT '结果',
    `address` varchar(30) NOT NULL COMMENT '工作节点地址',
    `machine` int(11) NOT NULL COMMENT '执行任务的机器',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `delete_time` datetime(3) DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;