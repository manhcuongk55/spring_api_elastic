package vn.com.vtcc.browser.api.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Created by c on 21/04/2017.
 */
public class KafkaFactory {
    @Autowired
    private KafkaTemplate<Integer, String> template;

    public void sendMessage() {

    }
}
