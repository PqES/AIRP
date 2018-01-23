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
public class CoefficientTests01 extends TestCase{
	int a, b, c, d;

	@Override
	protected void setUp() throws Exception {
		a = 2;
		b = 1;
		c = 2;
		d = 3;
	}

	@Test
	public void testJaccard() {
		Assert.assertEquals(0.4, new JaccardCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSMC() {
		Assert.assertEquals(0.625, new SMCCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testYule() {
		Assert.assertEquals(0.5, new YuleCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testHamann() {
		Assert.assertEquals(0.25, new HamannCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSorenson() {
		Assert.assertEquals(0.5714, new SorensonCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testRogersTanimoto() {
		Assert.assertEquals(0.4545, new RogersTanimotoCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSokalSneath() {
		Assert.assertEquals(0.7692, new SokalSneathCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testRussellRao() {
		Assert.assertEquals(0.25, new RussellRaoCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
		
	@Test
	public void testBaroniUrbani() {
		Assert.assertEquals(0.5972, new BaroniUrbaniCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSokalBinaryDistance() {
		Assert.assertEquals(0.6123, new SokalBinaryDistanceCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testOchiai() {
		Assert.assertEquals(0.5773, new OchiaiCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testPhiBinary() {
		Assert.assertEquals(0.2581, new PhiBinaryDistance().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testPSC() {
		Assert.assertEquals(0.3333, new PSCCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testDotProduct() {
		Assert.assertEquals(0.2857, new DotProductCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testKulczynski() {
		Assert.assertEquals(0.5833, new KulczynskiCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSokalSneath2() {
		Assert.assertEquals(0.25, new SokalSneath2CoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSokalSneath4() {
		Assert.assertEquals(0.6291
				, new SokalSneath4CoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testRelativeMatching() {
		Assert.assertEquals(0.4258, new RelativeMatchingCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
}
