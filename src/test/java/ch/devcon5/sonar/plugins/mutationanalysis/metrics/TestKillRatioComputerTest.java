package ch.devcon5.sonar.plugins.mutationanalysis.metrics;

import static ch.devcon5.sonar.plugins.mutationanalysis.metrics.MutationMetrics.TEST_KILLS_KEY;
import static ch.devcon5.sonar.plugins.mutationanalysis.metrics.MutationMetrics.TEST_KILL_RATIO_KEY;
import static ch.devcon5.sonar.plugins.mutationanalysis.metrics.MutationMetrics.UTILITY_GLOBAL_MUTATIONS_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.ce.measure.test.TestMeasureComputerContext;
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinitionContext;

/**
 *
 */
public class TestKillRatioComputerTest {

  private MeasureComputerTestHarness<TestKillRatioComputer> harness;

  private TestKillRatioComputer computer;

  @Before
  public void setUp() throws Exception {
    this.harness = MeasureComputerTestHarness.createFor(TestKillRatioComputer.class);
    this.computer = harness.getComputer();
  }



  @Test
  public void define() {
    final TestMeasureComputerDefinitionContext context = new TestMeasureComputerDefinitionContext();

    final MeasureComputer.MeasureComputerDefinition def = computer.define(context);

    assertTrue(def.getInputMetrics()
                  .containsAll(Arrays.asList(UTILITY_GLOBAL_MUTATIONS_KEY, TEST_KILLS_KEY)));
    assertTrue(def.getOutputMetrics().containsAll(Arrays.asList(TEST_KILL_RATIO_KEY)));
  }

  @Test
  public void compute_noInputMeasures_noOutputMeasure() {

    final TestMeasureComputerContext measureContext = harness.createMeasureContextForUnitTest("compKey");

    computer.compute(measureContext);

    Measure total = measureContext.getMeasure(TEST_KILL_RATIO_KEY);

    //no measure is created if component contains no/0 input values
    assertNull(total);

  }

  @Test
  public void compute_experimentalFeaturesDisable_noOutputMeasure() {

    harness.enableExperimentalFeatures(false);
    final TestMeasureComputerContext measureContext = harness.createMeasureContextForUnitTest("compKey");

    measureContext.addInputMeasure(UTILITY_GLOBAL_MUTATIONS_KEY, 10);
    measureContext.addInputMeasure(TEST_KILLS_KEY, 1);

    computer.compute(measureContext);

    Measure total = measureContext.getMeasure(TEST_KILL_RATIO_KEY);

    //no measure is created if component contains no/0 input values
    assertNull(total);

  }

  @Test
  public void compute_noUnitTest_noOutputMeasure() {

    final TestMeasureComputerContext measureContext = harness.createMeasureContext("compKey", Component.Type.FILE);

    computer.compute(measureContext);

    Measure total = measureContext.getMeasure(TEST_KILL_RATIO_KEY);

    //no measure is created if component contains no/0 input values
    assertNull(total);

  }

  @Test
  public void compute_oneInputMeasure_computesOutputMeasure() {

    final TestMeasureComputerContext measureContext = harness.createMeasureContextForUnitTest("compKey");

    measureContext.addInputMeasure(UTILITY_GLOBAL_MUTATIONS_KEY, 10);
    measureContext.addInputMeasure(TEST_KILLS_KEY, 1);

    computer.compute(measureContext);

    Measure ratio = measureContext.getMeasure(TEST_KILL_RATIO_KEY);

    assertEquals(10.0, ratio.getDoubleValue(), 0.05);

  }

  @Test
  public void compute_globalMutations0_noOutputMeasure() {

    final TestMeasureComputerContext measureContext = harness.createMeasureContextForUnitTest("compKey");

    measureContext.addInputMeasure(UTILITY_GLOBAL_MUTATIONS_KEY, 0);
    measureContext.addInputMeasure(TEST_KILLS_KEY, 0);

    computer.compute(measureContext);

    Measure ratio = measureContext.getMeasure(TEST_KILL_RATIO_KEY);

    assertNull(ratio);

  }

  @Test
  public void compute_noGlobalMetrics_noOutputMeasure() {

    final TestMeasureComputerContext measureContext = harness.createMeasureContextForUnitTest("compKey");

    measureContext.addChildrenMeasures(TEST_KILLS_KEY, 3,2, 1);

    computer.compute(measureContext);

    Measure ratio = measureContext.getMeasure(TEST_KILL_RATIO_KEY);

    assertNull(ratio);

  }

  @Test
  public void compute_childInputMeasure_computesOutputMeasure() {

    final TestMeasureComputerContext measureContext = harness.createMeasureContextForUnitTest("compKey");

    measureContext.addChildrenMeasures(UTILITY_GLOBAL_MUTATIONS_KEY, 10, 10, 10, 10);
    measureContext.addChildrenMeasures(TEST_KILLS_KEY, 3,2, 1);

    computer.compute(measureContext);

    Measure ratio = measureContext.getMeasure(TEST_KILL_RATIO_KEY);

    assertEquals(60.0, ratio.getDoubleValue(), 0.05);

  }
}