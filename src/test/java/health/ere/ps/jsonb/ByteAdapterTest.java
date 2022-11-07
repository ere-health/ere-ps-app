package health.ere.ps.jsonb;



import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class ByteAdapterTest {

    ByteAdapter byteAdapter = new ByteAdapter();


    @Test
    void adaptToJson() {
        String str = "with byte array , i can only try my best and hope for the best";
        byte array[] = str.getBytes();
        //assertTrue(byteAdapter.adaptToJson(array)
        //assertThatJson(byteAdapter.adaptToJson(array));
        //assertThatJson(byteAdapter.adaptToJson(array));
    }

    @Test
    void adaptFromJson() {
    }
}