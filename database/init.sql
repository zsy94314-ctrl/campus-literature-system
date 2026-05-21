-- 校园学术文献智能检索与综述生成系统
-- MySQL 初始化建表脚本
-- 建议文件位置：database/init.sql

CREATE DATABASE IF NOT EXISTS campus_literature_system
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_literature_system;

-- 1. 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER普通用户，ADMIN管理员',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='用户表';

-- 2. 文献分类表
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='文献分类表';

-- 3. 文献表
CREATE TABLE literature (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文献ID',
    title VARCHAR(255) NOT NULL COMMENT '文献标题',
    authors VARCHAR(255) COMMENT '作者',
    abstract_text TEXT COMMENT '摘要',
    keywords VARCHAR(255) COMMENT '关键词，多个关键词用逗号分隔',
    journal VARCHAR(255) COMMENT '期刊或会议名称',
    publish_year INT COMMENT '发表年份',
    doi VARCHAR(100) COMMENT 'DOI',
    category_id BIGINT COMMENT '分类ID',
    citation_count INT DEFAULT 0 COMMENT '引用次数',
    file_url VARCHAR(500) COMMENT '文献文件地址，可选',
    source VARCHAR(255) COMMENT '数据来源',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_title (title),
    INDEX idx_year (publish_year),
    INDEX idx_category (category_id)
) COMMENT='文献表';

-- 4. 收藏表
CREATE TABLE favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    literature_id BIGINT NOT NULL COMMENT '文献ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_literature (user_id, literature_id)
) COMMENT='文献收藏表';

-- 5. 检索历史表
CREATE TABLE search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    keyword VARCHAR(255) NOT NULL COMMENT '检索关键词',
    search_type VARCHAR(30) DEFAULT 'NORMAL' COMMENT '检索类型：NORMAL普通检索，SEMANTIC语义检索',
    result_count INT DEFAULT 0 COMMENT '结果数量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检索时间',
    INDEX idx_user_time (user_id, create_time)
) COMMENT='检索历史表';

-- 6. 综述记录表
CREATE TABLE review_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '综述记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    topic VARCHAR(255) NOT NULL COMMENT '综述主题',
    literature_ids TEXT COMMENT '使用的文献ID列表，如：1,2,3',
    content LONGTEXT COMMENT '生成的综述内容',
    reference_text TEXT COMMENT '参考来源说明',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_time (user_id, create_time)
) COMMENT='综述记录表';

-- 7. 管理员操作日志表
CREATE TABLE admin_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(50) COMMENT '操作对象类型',
    target_id BIGINT COMMENT '操作对象ID',
    detail TEXT COMMENT '操作详情',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
) COMMENT='管理员操作日志表';

-- 8. 文献向量索引表，可选
CREATE TABLE literature_vector (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '向量记录ID',
    literature_id BIGINT NOT NULL UNIQUE COMMENT '文献ID',
    vector_index_id BIGINT COMMENT 'FAISS中的向量索引ID',
    embedding_model VARCHAR(100) COMMENT '向量模型名称',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='文献向量索引表';

-- 初始化管理员账号，密码后续由后端加密处理，这里仅作占位
INSERT INTO user (username, password_hash, real_name, email, role, status)
VALUES ('admin', 'admin123', '系统管理员', 'admin@example.com', 'ADMIN', 1);

-- 初始化分类
INSERT INTO category (name, parent_id, sort_order) VALUES
('人工智能', 0, 1),
('软件工程', 0, 2),
('数据挖掘', 0, 3),
('教育技术', 0, 4),
('信息管理', 0, 5);

-- 初始化示例文献
INSERT INTO literature
(title, authors, abstract_text, keywords, journal, publish_year, doi, category_id, citation_count, source)
VALUES
('人工智能在高校教学中的应用研究', '张三, 李四', '本文分析了人工智能技术在高校教学中的应用场景，包括智能推荐、个性化学习和教学评价等方面。', '人工智能,高校教学,个性化学习', '教育信息化研究', 2023, '10.0000/example001', 1, 12, '系统录入'),
('基于机器学习的学生学习行为分析', '王五', '本文利用机器学习方法对学生学习行为数据进行分析，探索学习行为与学习效果之间的关系。', '机器学习,学习行为,数据分析', '计算机教育', 2022, '10.0000/example002', 3, 8, '系统录入'),
('面向学术文献的智能检索系统设计', '赵六, 陈七', '本文设计了一种面向学术文献的智能检索系统，通过关键词匹配和语义相似度计算提升检索效果。', '文献检索,语义检索,智能系统', '软件工程与应用', 2024, '10.0000/example003', 2, 15, '系统录入');
