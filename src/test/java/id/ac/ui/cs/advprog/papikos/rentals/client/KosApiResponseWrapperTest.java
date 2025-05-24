package id.ac.ui.cs.advprog.papikos.rentals.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KosApiResponseWrapperTest {

    @Test
    void testKosApiResponseWrapper() {
        KosApiResponseWrapper<String> wrapper = new KosApiResponseWrapper<>();
        wrapper.setStatus(200);
        wrapper.setMessage("Success");
        wrapper.setData("TestData");
        wrapper.setTimestamp(123456789L);

        assertEquals(200, wrapper.getStatus());
        assertEquals("Success", wrapper.getMessage());
        assertEquals("TestData", wrapper.getData());
        assertEquals(123456789L, wrapper.getTimestamp());

        KosApiResponseWrapper<Integer> wrapper2 = new KosApiResponseWrapper<>();
        assertNotNull(wrapper2); // Test NoArgsConstructor
    }
}