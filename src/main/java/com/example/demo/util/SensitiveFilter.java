package com.example.demo.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.tomcat.util.buf.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        //getResourceAsStream从classes下读取文件
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(is));) {
            String keyword;
            while ((keyword = br.readLine()) != null) {
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }

    }

    /**
     * 过滤敏感词
     *
     * @param text（待过滤的文本）
     * @return（过滤后的文本）
     */
    public String filter(String text) {
        if (text == null) {
            return null;
        }

        //指针1（指向前缀树）
        TrieNode tempNode = rootNode;

        //指针2（和指针3一起指向待过滤的文本字符串）
        int begin = 0;

        //指针3
        int position = 0;

        StringBuilder sb = new StringBuilder();

        //字符串长度
        int length = text.length();

        while (position < length) {
            char c = text.charAt(position);

            //跳过符号
            if (isSymbol(c)) {
                //若指针1指向根节点，将此符号计入结果，让指针二向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }

            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                //以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置
                position = ++begin;
                //重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd) {
                //发现敏感词，将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一个位置
                begin = ++position;
                //重新指向根节点
                tempNode = rootNode;
            } else {
                //检查下一个字符
                position++;
            }
        }

        //如果begin越界，那么position必定越界
        //当begin没有越界而退出循环，那么position就越界了，需要添加bengin之后的文本字符串
        if (begin < length) {
            sb.append(text.substring(begin));
        }

        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol(char c) {
        //CharUtils.isAsciiAlphanumeric(c)，普通字符返回true
        //0x2E80~0x9FFF，为东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            //指向子节点，进入下一轮循环
            tempNode = subNode;
        }
        tempNode.setKeywordEnd(true);
    }

    //前缀树
    private class TrieNode {

        //描述关键词结束的标识
        boolean isKeywordEnd = false;

        //子节点（key是下级字符，value是下级节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
