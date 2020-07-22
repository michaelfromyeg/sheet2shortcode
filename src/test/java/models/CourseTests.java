package models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test.
 */
class CourseTests {
  private Course c1;
  private Course c2;
  private Course c3;

  @BeforeEach
  void setup() {
    c1 = new Course("MATH", 101);
    c2 = new Course("ECON", 504, "Khan", "khan@ubc.ca");
    c3 = new Course("PHYS", 200, "Bates", "bates@ubc.ca", true, "The course is now async only!");
  }

  @Test
  void courseDefaultsProfName() {
    assertEquals(c1.getProfName(), "");
  }

  @Test
  void courseDefaultsProfEmail() {
    assertEquals(c1.getProfEmail(), "");
  }

  @Test
  void courseSetsIsGradFalse() {
    assertFalse(c3.isGrad());
  }

  @Test
  void courseSetsIsGradTrue() {
    assertTrue(c2.isGrad());
  }
}