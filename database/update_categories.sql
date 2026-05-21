-- 更新分类体系脚本
-- 用途：将现有 category 表改造为两级分类结构
-- 注意：本脚本会清空旧分类数据，但不会删除 literature 表中的文献数据。
--       执行后，已有文献的 category_id 可能指向不存在的旧分类，后续生成新文献时会使用新分类。

SET NAMES utf8mb4;
USE campus_literature_system;

-- 清空旧分类数据并重置自增 ID
TRUNCATE TABLE category;

-- 一级分类
INSERT INTO category (name, parent_id, sort_order) VALUES
('自然科学', 0, 1),
('工程与技术', 0, 2),
('医学与健康科学', 0, 3),
('农业与生命科学', 0, 4),
('社会科学', 0, 5),
('人文与艺术', 0, 6);

-- 二级分类：自然科学 (parent_id = 1)
INSERT INTO category (name, parent_id, sort_order) VALUES
('数学', 1, 1),
('物理学', 1, 2),
('化学', 1, 3),
('地球科学', 1, 4),
('生物科学', 1, 5),
('环境科学', 1, 6);

-- 二级分类：工程与技术 (parent_id = 2)
INSERT INTO category (name, parent_id, sort_order) VALUES
('计算机科学与人工智能', 2, 1),
('软件工程', 2, 2),
('数据科学与数据挖掘', 2, 3),
('信息与通信工程', 2, 4),
('电子工程', 2, 5),
('智能制造与自动化', 2, 6),
('能源与环境工程', 2, 7);

-- 二级分类：医学与健康科学 (parent_id = 3)
INSERT INTO category (name, parent_id, sort_order) VALUES
('基础医学', 3, 1),
('临床医学', 3, 2),
('公共健康', 3, 3),
('心理健康', 3, 4),
('健康管理', 3, 5),
('医学信息学', 3, 6);

-- 二级分类：农业与生命科学 (parent_id = 4)
INSERT INTO category (name, parent_id, sort_order) VALUES
('农学', 4, 1),
('食品科学', 4, 2),
('生态学', 4, 3),
('生物技术', 4, 4);

-- 二级分类：社会科学 (parent_id = 5)
INSERT INTO category (name, parent_id, sort_order) VALUES
('教育学', 5, 1),
('管理学', 5, 2),
('经济学', 5, 3),
('法学', 5, 4),
('社会学', 5, 5),
('心理学', 5, 6),
('新闻传播学', 5, 7),
('图书情报与档案管理', 5, 8);

-- 二级分类：人文与艺术 (parent_id = 6)
INSERT INTO category (name, parent_id, sort_order) VALUES
('文学', 6, 1),
('历史学', 6, 2),
('哲学', 6, 3),
('语言学', 6, 4),
('艺术学', 6, 5),
('文化研究', 6, 6);
