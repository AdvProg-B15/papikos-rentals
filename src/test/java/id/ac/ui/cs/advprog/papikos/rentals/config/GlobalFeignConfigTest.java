package id.ac.ui.cs.advprog.papikos.rentals.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class GlobalFeignConfigTest {

    private GlobalFeignConfig globalFeignConfig;

    @BeforeEach
    void setUp() {
        globalFeignConfig = new GlobalFeignConfig();
    }

    @Test
    void testPropertyBasedRequestInterceptor_withToken() {
        ReflectionTestUtils.setField(globalFeignConfig, "internalTokenSecret", "test-secret-token");
        RequestInterceptor interceptor = globalFeignConfig.propertyBasedRequestInterceptor();
        assertNotNull(interceptor);

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertTrue(template.headers().containsKey("X-Internal-Token"));
        assertEquals(1, template.headers().get("X-Internal-Token").size());
        assertEquals("test-secret-token", template.headers().get("X-Internal-Token").iterator().next());
    }

    @Test
    void testPropertyBasedRequestInterceptor_withoutToken() {
        // internalTokenSecret is null by default or can be set to null/empty
        ReflectionTestUtils.setField(globalFeignConfig, "internalTokenSecret", null);
        RequestInterceptor interceptor = globalFeignConfig.propertyBasedRequestInterceptor();
        assertNotNull(interceptor);

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey("X-Internal-Token"));
    }

    @Test
    void testPropertyBasedRequestInterceptor_withEmptyToken() {
        ReflectionTestUtils.setField(globalFeignConfig, "internalTokenSecret", "");
        RequestInterceptor interceptor = globalFeignConfig.propertyBasedRequestInterceptor();
        assertNotNull(interceptor);

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey("X-Internal-Token"));
    }
}