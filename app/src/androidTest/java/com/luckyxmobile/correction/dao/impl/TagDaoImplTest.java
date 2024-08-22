package com.luckyxmobile.correction.dao.impl;


import com.luckyxmobile.correction.bean.Tag;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.impl.TagDaoImpl;

import org.junit.Assert;
import org.junit.Test;
import org.litepal.LitePal;
import java.util.List;


/**
 * 对封装方法进行测试（关于标签）
 * @author qjj
 * @date 2019/7/24
 */
public class TagDaoImplTest {


    /**
     * 测试添加标签
     * @author qjj
     * @date 2019/7/24
     * 期望值是 1 2 4 （即2的次方）
     * 实际值是 每次添加标签为标签分配的tag_topic值（标签与错题的关联值）
     */
    @Test
    public void newTag() {

        Tag tag1 = TagDaoImpl.newTag("FirstTag");
        Tag tag2 = TagDaoImpl.newTag("SecondTag");
        Tag tag3 = TagDaoImpl.newTag("ThirdTag");

        Assert.assertEquals(1,tag1.getTag_topic());
        Assert.assertEquals(2,tag2.getTag_topic());
        Assert.assertEquals(4,tag3.getTag_topic());

        LitePal.delete(Tag.class,tag1.getId());
        LitePal.delete(Tag.class,tag2.getId());
        LitePal.delete(Tag.class,tag3.getId());

    }

    /**
     * 测试错题中添加标签
     * @author qjj
     * @date 2019/7/24
     *期望值是 1+2+4
     *实际值是 错题的tag字段（错题和标签的关联字段）
     */
    @Test
    public void topicAddTag() {

        Topic topic = new Topic();
        topic.save();

        Tag tag1 = TagDaoImpl.newTag("FirstTag");
        Tag tag2 = TagDaoImpl.newTag("SecondTag");
        Tag tag3 = TagDaoImpl.newTag("ThirdTag");

        TagDaoImpl.topicAddTag(topic.getId(),tag1.getId());
        TagDaoImpl.topicAddTag(topic.getId(),tag2.getId());
        TagDaoImpl.topicAddTag(topic.getId(),tag3.getId());

        Assert.assertEquals(1+2+4,LitePal.find(Topic.class,topic.getId()).getTopic_tag());

        LitePal.delete(Topic.class,topic.getId());
        LitePal.delete(Tag.class,tag1.getId());
        LitePal.delete(Tag.class,tag2.getId());
        LitePal.delete(Tag.class,tag3.getId());

    }

    /**
     * 测试删除标签
     * @author qjj
     * @date 2019/7/24
     *期望值是 1+2+4
     *实际值是 错题的tag字段（错题和标签的关联字段）
     */
    @Test
    public void deleteTag() {

        Topic topic = new Topic();
        topic.save();

        Tag tag1 = TagDaoImpl.newTag("FirstTag");
        Tag tag2 = TagDaoImpl.newTag("SecondTag");
        Tag tag3 = TagDaoImpl.newTag("ThirdTag");

        TagDaoImpl.topicAddTag(topic.getId(),tag1.getId());
        TagDaoImpl.topicAddTag(topic.getId(),tag2.getId());
        TagDaoImpl.topicAddTag(topic.getId(),tag3.getId());

        TagDaoImpl.deleteTag(tag1.getId());
        TagDaoImpl.deleteTag(tag2.getId());
        TagDaoImpl.deleteTag(tag3.getId());

        Assert.assertEquals(0,LitePal.find(Topic.class,topic.getId()).getTopic_tag());

        LitePal.delete(Topic.class,topic.getId());
        LitePal.delete(Tag.class,tag1.getId());
        LitePal.delete(Tag.class,tag2.getId());
        LitePal.delete(Tag.class,tag3.getId());

    }

    /**
     * 通过错题查找标签
     * @author qjj
     * @date 2019/7/24
     *期望值是 2
     *实际值是 通过错题查找到的标签数量
     */
    @Test
    public void findTagByTopic() {

        Topic topic = new Topic();
        topic.save();

        Tag tag1 = TagDaoImpl.newTag("FirstTag");
        Tag tag2 = TagDaoImpl.newTag("SecondTag");
        Tag tag3 = TagDaoImpl.newTag("ThirdTag");

        assert tag2 != null;
        TagDaoImpl.topicAddTag(topic.getId(),tag2.getId());
        assert tag3 != null;
        TagDaoImpl.topicAddTag(topic.getId(),tag3.getId());

        List<Tag> tags = TagDaoImpl.findTagByTopic(LitePal.find(Topic.class,topic.getId()).getTopic_tag());

        Assert.assertEquals(2,tags.size());

        LitePal.delete(Topic.class,topic.getId());
        LitePal.delete(Tag.class,tag1.getId());
        LitePal.delete(Tag.class,tag2.getId());
        LitePal.delete(Tag.class,tag3.getId());

    }

    /**
     * 通过标签查找错题
     * @author qjj
     * @date 2019/7/24
     *期望值是 3 2 1
     *实际值是 通过标签查找到的错题数量
     */
    @Test
    public void findTopicByTag() {

        Tag tag1 = TagDaoImpl.newTag("FirstTag");
        Tag tag2 = TagDaoImpl.newTag("SecondTag");
        Tag tag3 = TagDaoImpl.newTag("ThirdTag");

       Topic topic1 = new Topic();
       topic1.save();
       Topic topic2 = new Topic();
       topic2.save();
       Topic topic3 = new Topic();
       topic3.save();

        TagDaoImpl.topicAddTag(topic1.getId(),tag1.getId());
        TagDaoImpl.topicAddTag(topic2.getId(),tag1.getId());
        TagDaoImpl.topicAddTag(topic2.getId(),tag2.getId());
        TagDaoImpl.topicAddTag(topic3.getId(),tag1.getId());
        TagDaoImpl.topicAddTag(topic3.getId(),tag2.getId());
        TagDaoImpl.topicAddTag(topic3.getId(),tag3.getId());

        List<Topic> topics1 = TagDaoImpl.findTopicByTag(LitePal.find(Tag.class,tag1.getId()).getTag_topic());
        List<Topic> topics2 = TagDaoImpl.findTopicByTag(LitePal.find(Tag.class,tag2.getId()).getTag_topic());
        List<Topic> topics3 = TagDaoImpl.findTopicByTag(LitePal.find(Tag.class,tag3.getId()).getTag_topic());

        Assert.assertEquals(3,topics1.size());
        Assert.assertEquals(2,topics2.size());
        Assert.assertEquals(1,topics3.size());

        LitePal.delete(Topic.class,topic1.getId());
        LitePal.delete(Topic.class,topic2.getId());
        LitePal.delete(Topic.class,topic3.getId());
        LitePal.delete(Tag.class,tag1.getId());
        LitePal.delete(Tag.class,tag2.getId());
        LitePal.delete(Tag.class,tag3.getId());

    }
}