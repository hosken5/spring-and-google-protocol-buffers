package demo;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoApplication.class)
@WebAppConfiguration
@IntegrationTest
public class DemoApplicationTests {

    @Configuration
    public static class RestClientConfiguration {

        @Bean
        RestTemplate restTemplate(ProtobufHttpMessageConverter hmc) {
            return new RestTemplate(Arrays.asList(hmc));
        }

        @Bean
        ProtobufHttpMessageConverter protobufHttpMessageConverter() {
            return new ProtobufHttpMessageConverter();
        }
    }

    @Autowired
    private RestTemplate restTemplate;

    private int port = 8080;

    private String baseUri = "localhost";

    @Test
    public  void  test2() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost("http://localhost:8080/proto/write1");
        //UserProto.User user =   UserProto.User.newBuilder().setId(1).setName("zhangsan").addPhone(UserProto.User.PhoneNumber.newBuilder().setNumber("18611163408")).build();//构造
        CustomerProtos.Customer customer =
                CustomerProtos.Customer.newBuilder()
                        .setFirstName("f")
                        .setLastName("last")
                        .setId(3)
                        .build() ;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(customer.toByteArray());
        InputStreamEntity inputStreamEntity = new InputStreamEntity(inputStream);

        //这两行很重要的，是告诉springmvc客户端请求和响应的类型，指定application/x-protobuf类型,spring会用ProtobufHttpMessageConverter类来解析请求和响应的实体
        httpPost.addHeader("Content-Type","application/x-protobuf");
        httpPost.addHeader("Accept", "application/x-protobuf");
        httpPost.setEntity(inputStreamEntity);
        //httpPost.setURI();
        CloseableHttpResponse response2 = httpclient.execute(httpPost);

        try {
            System.out.println(response2.getStatusLine());
            org.apache.http.HttpEntity entity2 = response2.getEntity();

            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            entity2.writeTo(buf);
            System.out.println(new String(buf.toByteArray())+"#################");
            CustomerProtos.Customer user2 = CustomerProtos.Customer.parseFrom(buf.toByteArray());
            //user2.toString();
            System.out.println(user2);
        } finally {
            response2.close();
        }
    }

    @Test
    public void contextLoaded() throws IOException {
        ResponseEntity<CustomerProtos.Customer> customer1 = restTemplate.getForEntity(
                "http://127.0.0.1:" + port + "/customers/2", CustomerProtos.Customer.class);
        System.out.println("customer retrieved: " + customer1.toString());
    }
}
