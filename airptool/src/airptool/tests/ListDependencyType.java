package airptool.tests;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import airptool.core.coefficients.BaroniUrbaniCoefficientStrategy;
import airptool.core.coefficients.DotProductCoefficientStrategy;
import airptool.core.coefficients.HamannCoefficientStrategy;
import airptool.core.coefficients.JaccardCoefficientStrategy;
import airptool.core.coefficients.KulczynskiCoefficientStrategy;
import airptool.core.coefficients.OchiaiCoefficientStrategy;
import airptool.core.coefficients.PSCCoefficientStrategy;
import airptool.core.coefficients.PhiBinaryDistance;
import airptool.core.coefficients.RelativeMatchingCoefficientStrategy;
import airptool.core.coefficients.RogersTanimotoCoefficientStrategy;
import airptool.core.coefficients.RussellRaoCoefficientStrategy;
import airptool.core.coefficients.SMCCoefficientStrategy;
import airptool.core.coefficients.SokalBinaryDistanceCoefficientStrategy;
import airptool.core.coefficients.SokalSneath2CoefficientStrategy;
import airptool.core.coefficients.SokalSneath4CoefficientStrategy;
import airptool.core.coefficients.SokalSneathCoefficientStrategy;
import airptool.core.coefficients.SorensonCoefficientStrategy;
import airptool.core.coefficients.YuleCoefficientStrategy;

/**
 * @author Luis Miranda
 */
public class ListDependencyType extends TestCase {
	int a, b, c, d;

	@Override
	protected void setUp() throws Exception {
		a = 10;
		b = 8;
		c = 16;
		d = 60;
	}

	@Test
	public void testJaccard() {
		Assert.assertEquals(0.2941, new JaccardCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSMC() {
		Assert.assertEquals(0.7446, new SMCCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testYule() {
		Assert.assertEquals(0.6483, new YuleCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testHamann() {
		Assert.assertEquals(0.4893, new HamannCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSorenson() {
		Assert.assertEquals(0.4545, new SorensonCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testRogersTanimoto() {
		Assert.assertEquals(0.5932, new RogersTanimotoCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSokalSneath() {
		Assert.assertEquals(0.8536, new SokalSneathCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testRussellRao() {
		Assert.assertEquals(0.1063, new RussellRaoCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
		
	@Test
	public void testBaroniUrbani() {
		Assert.assertEquals(0.5897, new BaroniUrbaniCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSokalBinaryDistance() {
		Assert.assertEquals(0.5052, new SokalBinaryDistanceCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testOchiai() {
		Assert.assertEquals(0.4622, new OchiaiCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testPhiBinary() {
		Assert.assertEquals(0.3034, new PhiBinaryDistance().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testPSC() {
		Assert.assertEquals(0.2136, new PSCCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testDotProduct() {
		Assert.assertEquals(0.2272, new DotProductCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testKulczynski() {
		Assert.assertEquals(0.4700, new KulczynskiCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSokalSneath2() {
		Assert.assertEquals(0.1724, new SokalSneath2CoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSokalSneath4() {
		Assert.assertEquals(0.6529, new SokalSneath4CoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testRelativeMatching() {
		Assert.assertEquals(0.2911, new RelativeMatchingCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
}
