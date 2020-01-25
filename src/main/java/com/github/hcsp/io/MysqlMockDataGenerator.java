package com.github.hcsp.io;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MysqlMockDataGenerator {

    public static void main(String[] args) throws IOException {
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        insert(sqlSessionFactory, 80_0000);
    }

    public static void insert(SqlSessionFactory sqlSessionFactory, int total){
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {//开启批处理模式
            try{
                List<News> newsList = session.selectList("com.github.hcsp.MockDataMapper.selectTopNews");
                int count = total - newsList.size();
                Random random = new Random();
                while (count-- > 0){
                    int index = random.nextInt(newsList.size()-1);
                    News news = newsList.get(index);
                    news.setModified_at(Instant.now());
                    news.setCreated_at(Instant.now());
                    session.insert("com.github.hcsp.MockDataMapper.insertNew", news);

                    System.out.println("left:"+count);
                    if ( count%20000==0 ){
                        session.flushStatements(); //自动提交
                    }
                }
                session.commit();

            }catch(Exception exp){
                session.rollback();
                throw new RuntimeException(exp);
            }

        }
    }
}
