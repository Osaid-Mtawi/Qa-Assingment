package org.osaid;


import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Test.ProductStock – Complete Inventory Test Suite")
class ProductStockTest {

    private ProductStock stock;


    // Lifecycle


    @BeforeAll
    static void initSuite() {
        System.out.println("=== Test.ProductStock Test Suite Started ===");
    }

    @AfterAll
    static void finishSuite() {
        System.out.println("=== Test.ProductStock Test Suite Completed ===");
    }

    @BeforeEach
    void initTest() {
        stock = new ProductStock("ITEM-99", "LOC-A1", 50, 10, 100);
    }

    @AfterEach
    void testSummary() {
        System.out.println("[STATE] " + stock.toString());
    }


    // Constructor Tests


    @Test
    @Tag("sanity")
    @DisplayName("Constructor initializes attributes correctly when inputs are valid")
    void constructor_validInputs_initializesFields() {
        ProductStock p = new ProductStock("X-1", "L-1", 15, 5, 40);

        assertAll("constructor fields",
                () -> assertEquals("X-1", p.getProductId()),
                () -> assertEquals("L-1", p.getLocation()),
                () -> assertEquals(15, p.getOnHand()),
                () -> assertEquals(0, p.getReserved()),
                () -> assertEquals(5, p.getReorderThreshold()),
                () -> assertEquals(40, p.getMaxCapacity()),
                () -> assertEquals(15, p.getAvailable())
        );
    }

    @Test
    @Tag("regression")
    @DisplayName("Constructor rejects invalid productId or location values")
    void constructor_invalidTextFields_throws() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock(null, "A1", 10, 2, 30)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("   ", "A1", 10, 2, 30)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("P1", "", 10, 2, 30))
        );
    }

    @Test
    @Tag("regression")
    @DisplayName("Constructor rejects negative or inconsistent numeric values")
    void constructor_invalidNumbers_throws() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("P1", "A1", -5, 0, 20)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("P1", "A1", 0, -3, 20)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("P1", "A1", 0, 0, 0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("P1", "A1", 200, 10, 100))
        );
    }


    // changeLocation

    @Test
    @Tag("sanity")
    @DisplayName("changeLocation updates the internal location when valid")
    void changeLocation_updatesCorrectly() {
        stock.changeLocation("LOC-B2");
        assertEquals("LOC-B2", stock.getLocation());
    }

    @Test
    @Tag("regression")
    @DisplayName("changeLocation rejects null or blank values")
    void changeLocation_invalidValues_throws() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> stock.changeLocation(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> stock.changeLocation("   "))
        );
    }


    // addStock

    @Test
    @Tag("sanity")
    @DisplayName("addStock increases onHand without exceeding capacity")
    void addStock_validIncrease() {
        stock.addStock(25);
        assertEquals(75, stock.getOnHand());
        assertEquals(75, stock.getAvailable());
    }

    @Test
    @Tag("regression")
    @DisplayName("addStock rejects zero or negative inputs")
    void addStock_zeroOrNegative_throws() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> stock.addStock(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> stock.addStock(-10))
        );
    }

    @Test
    @Tag("regression")
    @DisplayName("addStock does not allow exceeding maxCapacity")
    void addStock_exceedsCapacity_throws() {
        assertThrows(IllegalStateException.class, () -> stock.addStock(60));
    }

    @Test
    @Timeout(1)
    @Tag("sanity")
    @DisplayName("addStock completes quickly under normal conditions")
    void addStock_executesWithinTimeLimit() {
        assertDoesNotThrow(() -> stock.addStock(10));
    }


    // removeDamaged

    @Test
    @Tag("sanity")
    @DisplayName("removeDamaged reduces onHand and maintains invariants")
    void removeDamaged_validAmount() {
        stock.removeDamaged(15);
        assertEquals(35, stock.getOnHand());
        assertEquals(35, stock.getAvailable());
    }

    @Test
    @Tag("regression")
    @DisplayName("removeDamaged rejects zero or negative values")
    void removeDamaged_zeroOrNegative_throws() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> stock.removeDamaged(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> stock.removeDamaged(-3))
        );
    }

    @Test
    @Tag("regression")
    @DisplayName("removeDamaged rejects values greater than onHand")
    void removeDamaged_exceedsOnHand_throws() {
        assertThrows(IllegalStateException.class, () -> stock.removeDamaged(500));
    }



    // reserve + releaseReservation

    @Test
    @Tag("sanity")
    @DisplayName("reserve transfers quantity from available to reserved")
    void reserve_validAmount() {
        stock.reserve(20);

        assertAll(
                () -> assertEquals(50, stock.getOnHand()),
                () -> assertEquals(20, stock.getReserved()),
                () -> assertEquals(30, stock.getAvailable())
        );
    }

    @Test
    @Tag("regression")
    @DisplayName("reserve rejects zero, negative, or above available")
    void reserve_invalid_throws() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> stock.reserve(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> stock.reserve(-2)),
                () -> assertThrows(IllegalStateException.class, () -> stock.reserve(500))
        );
    }

    @Test
    @Tag("sanity")
    @DisplayName("releaseReservation decreases reserved and restores availability")
    void releaseReservation_validCase() {
        stock.reserve(25);
        stock.releaseReservation(10);

        assertAll(
                () -> assertEquals(15, stock.getReserved()),
                () -> assertEquals(35, stock.getAvailable())
        );
    }

    @Test
    @Tag("regression")
    @DisplayName("releaseReservation rejects invalid values")
    void releaseReservation_invalid_throws() {
        stock.reserve(10);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> stock.releaseReservation(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> stock.releaseReservation(-5)),
                () -> assertThrows(IllegalStateException.class, () -> stock.releaseReservation(100))
        );
    }


    // shipReserved

    @Test
    @Tag("sanity")
    @DisplayName("shipReserved correctly reduces both reserved and onHand")
    void shipReserved_validOperation() {
        stock.reserve(15);
        stock.shipReserved(10);

        assertAll(
                () -> assertEquals(40, stock.getOnHand()),
                () -> assertEquals(5, stock.getReserved()),
                () -> assertEquals(35, stock.getAvailable())
        );
    }

    @Test
    @Tag("regression")
    @DisplayName("shipReserved rejects zero, negative or greater-than-reserved values")
    void shipReserved_invalid_throws() {
        stock.reserve(12);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> stock.shipReserved(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> stock.shipReserved(-2)),
                () -> assertThrows(IllegalStateException.class, () -> stock.shipReserved(400))
        );
    }


    // Nested: Reorder Logic

    @Nested
    @DisplayName("Reorder behavior tests")
    class ReorderTests {

        @Test
        @Tag("sanity")
        @DisplayName("Reorder NOT needed when available >= threshold")
        void reorder_notNeeded() {
            assertFalse(stock.isReorderNeeded());
        }

        @Test
        @Tag("regression")
        @DisplayName("Reorder needed when available < threshold")
        void reorder_needed() {
            stock.removeDamaged(45);
            assertTrue(stock.isReorderNeeded());
        }
    }


    // updateReorderThreshold

    @Test
    @Tag("sanity")
    @DisplayName("updateReorderThreshold accepts values from 0 to maxCapacity")
    void updateThreshold_validRange() {
        stock.updateReorderThreshold(0);
        assertEquals(0, stock.getReorderThreshold());

        stock.updateReorderThreshold(100);
        assertEquals(100, stock.getReorderThreshold());
    }

    @Test
    @Tag("regression")
    @DisplayName("updateReorderThreshold rejects negative or > maxCapacity")
    void updateThreshold_invalid_throws() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> stock.updateReorderThreshold(-1)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> stock.updateReorderThreshold(101))
        );
    }


    // updateMaxCapacity

    @Test
    @Tag("sanity")
    @DisplayName("updateMaxCapacity supports increasing capacity and adjusts threshold")
    void updateMaxCapacity_validIncrease() {
        stock.updateMaxCapacity(150);
        assertEquals(150, stock.getMaxCapacity());

        stock.updateReorderThreshold(120);
        assertEquals(120, stock.getReorderThreshold());
    }

    @Test
    @Tag("regression")
    @DisplayName("updateMaxCapacity rejects invalid capacity values")
    void updateMaxCapacity_invalid_throws() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> stock.updateMaxCapacity(0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> stock.updateMaxCapacity(-5)),
                () -> assertThrows(IllegalStateException.class,
                        () -> stock.updateMaxCapacity(30))
        );
    }


    // To String

    @Test
    @DisplayName("toString should include key fields")
    void testToString() {
        String s = stock.toString();

        assertAll(
                () -> assertTrue(s.contains("productId")),
                () -> assertTrue(s.contains("location")),
                () -> assertTrue(s.contains("onHand")),
                () -> assertTrue(s.contains("reserved")),
                () -> assertTrue(s.contains("available")),
                () -> assertTrue(s.contains("maxCapacity"))
        );

    }




    // Future test

    @Test
    @Disabled("Pending business analysis for backorder support")
    @DisplayName("Backorder computation (feature under evaluation)")
    void backorder_futureFeature() {
        fail("Not implemented yet");
    }
}
