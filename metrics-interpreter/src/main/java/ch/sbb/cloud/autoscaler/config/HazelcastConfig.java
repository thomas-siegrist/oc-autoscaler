package ch.sbb.cloud.autoscaler.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by thomas on 01.09.16.
 */
@Configuration
public class HazelcastConfig {

    @Bean
    public Config config() {
        Config config = new Config();
        config.setGroupConfig(groupConfig());
        return config;
    }

    @Bean
    public HazelcastInstance hzInstance() {
        return Hazelcast.newHazelcastInstance();
    }

    private GroupConfig groupConfig() {
        GroupConfig groupConfig = new GroupConfig();
        groupConfig.setName("oc-autoscaler-hz");
        groupConfig.setPassword("oc$autoscaler$pw");
        return groupConfig;
    }

}
