package calculator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorTest {
	@Test
	void addsTwoNumbers() {
		assertEquals(7, Main.add(3, 4));
	}

	@Test
	void subtractsTwoNumbers() {
		assertEquals(3, Main.subtract(7, 4));
	}

	@Test
	void multipliesTwoNumbers() {
		assertEquals(28, Main.multiply(7, 4));
	}

	@Test
	void dividesTwoNumbers() {
		assertEquals(4, Main.divide(8, 2));
	}

	@Test
	void rejectsDivisionByZero() {
		assertThrows(IllegalArgumentException.class, () -> Main.divide(8, 0));
	}
}