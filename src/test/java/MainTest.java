import org.example.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest {
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    void testSimpleAddition() {
        String input = "2+3\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[0]);
        String output = outContent.toString().trim();
        assertTrue(output.contains("Result: 5.0"), "Expected Result: 5.0 but was: " + output);
    }

    @Test
    void testWithVariable() {
        // Expression: x * 2, x = 4
        String input = "x*2\n4\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[0]);
        String output = outContent.toString().trim();
        assertTrue(output.contains("Result: 8.0"), "Expected Result: 8.0 but was: " + output);
    }

    @Test
    void testFunctionAndPower() {
        // Expression: pow(2,3) + max(1,5)
        String input = "pow(2,3)+max(1,5)\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[0]);
        String output = outContent.toString().trim();
        assertTrue(output.contains("Result: 13.0"), "Expected Result: 13.0 but was: " + output);
    }

    @Test
    void testComplexExpression() {
        // Expression: (1+2)*3 - sqrt(16)
        String input = "(1+2)*3 - sqrt(16)\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[0]);
        String output = outContent.toString().trim();
        assertTrue(output.contains("Result: 5.0"), "Expected Result: 5.0 but was: " + output);
    }

    @Test
    void testInvalidExpression() {
        String input = "2+*3\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[0]);
        String output = outContent.toString().trim();
        assertTrue(output.contains("Error: Invalid expression"), "Expected error message but was: " + output);
    }
}
