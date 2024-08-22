package com.luckyxmobile.correction.dao.impl;


import com.luckyxmobile.correction.bean.Tag;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.util.SDCardUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签表封装方法
 * @author qjj
 * @date 2019/7/24
 */
public class TagDaoImpl {
    
    public static Tag newTag(String tagName){

        Tag tag = new Tag();
        tag.setTag_name(tagName);
        tag.setTag_create_time(CorrectionLab.getNowDate());

        //排除已经存在的情况
        if (LitePal.where("tag_name=?",tagName).find(Tag.class).size() != 0){
            return null;
        }

        for (int i = 0 ; i < Integer.SIZE; i++){

            if (LitePal.where("tag_topic=?",String.valueOf(Math.pow(2,i))).find(Tag.class).size() == 0){
                tag.setTag_topic((int) Math.pow(2,i));
                tag.save();
                return tag;
            }
        }

        return null;
    }

    public static List<Tag> findAllTag(){

        List<Tag> tags = LitePal.findAll(Tag.class);

        return tags;
    }


    public static Topic topicAddTag(int topicId, int tagId){

        Topic topic = LitePal.find(Topic.class,topicId);

        //排除已经存在的情况
        if ((topic.getTopic_tag()&LitePal.find(Tag.class,tagId).getTag_topic())==0){
            topic.setTopic_tag(topic.getTopic_tag()+LitePal.find(Tag.class,tagId).getTag_topic());
        }

        topic.save();

        return topic;

    }

    public static Topic topicDeleteTag(int topicId, int tagId){

        Topic topic = LitePal.find(Topic.class,topicId);
        //排除不存在的情况
        if ((topic.getTopic_tag()&LitePal.find(Tag.class,tagId).getTag_topic())!=0){
            topic.setTopic_tag(topic.getTopic_tag()-LitePal.find(Tag.class,tagId).getTag_topic());
        }

        topic.save();

        return topic;
    }

    public static void deleteTag(int tagId){

        Tag tag = LitePal.find(Tag.class,tagId);
        List<Topic> topics =  findTopicByTag(tag.getTag_topic());
        for(Topic topic : topics){
            topic.setTopic_tag(topic.getTopic_tag()-tag.getTag_topic());
            topic.save();
        }
        LitePal.delete(Tag.class,tagId);

    }

    public static List<Tag> findTagByTopic(int topicTag){

        List<Tag> tags = LitePal.findAll(Tag.class);
        List<Tag> result = new ArrayList<>();

        for (Tag tag:tags){
            if ((tag.getTag_topic()&topicTag) != 0){
                result.add(tag);
            }
        }

        return result;
    }

    public static List<Topic> findTopicByTag(int tagTopic){

        List<Topic> topics = LitePal.findAll(Topic.class);
        List<Topic> result = new ArrayList<Topic>();
        for (Topic topic : topics){
            if ((topic.getTopic_tag()&tagTopic) != 0){
                result.add(topic);
            }
        }
        return result;
    }

    /**
     * 通过用户选择的标签的名称的集合的字符串来筛选是否含有相关错题
     * 没有 返回原list
     * */
    public static List<Topic> findTopicsByTags(List<Topic> topics, String tagSum){

        List<Topic> result = new ArrayList<Topic>();

        // 这里的方法是将传入的标签名称字符串按照逗号分隔
        List<String> tags = SDCardUtil.handlePath(tagSum);

        if (tags.size() == 0){
            return topics;
        }

        for (Topic topic : topics){
            List<Tag> tagList =  findTagByTopic(topic.getTopic_tag());
            for (Tag tag:tagList){
                if (tags.contains(tag.getTag_name()) && !result.contains(topic)){
                    result.add(topic);
                }
            }
        }
        return result;
    }

    public static List<Tag> findTagsByTopics(List<Topic> topics){

        List<Tag> result = new ArrayList<Tag>();

        for (Topic topic : topics){
            List<Tag> tagList =  findTagByTopic(topic.getTopic_tag());
            for (Tag tag:tagList){
                if (!isTagsContainTag(result,tag)){
                    result.add(tag);
                }
            }
        }
        return result;
    }

    public static boolean isTagsContainTag(List<Tag> tags, Tag tag){

        for (Tag t :tags){
            if (t.getId() == tag.getId()){
                return true;
            }
        }
        return false;
    }

}
