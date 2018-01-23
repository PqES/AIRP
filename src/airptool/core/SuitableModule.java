package airptool.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;

import airptool.core.coefficients.BaroniUrbaniCoefficientStrategy;
import airptool.core.coefficients.DotProductCoefficientStrategy;
import airptool.core.coefficients.HamannCoefficientStrategy;
import airptool.core.coefficients.ICoefficientStrategy;
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
import airptool.enums.DependencyType;
import airptool.util.AirpUtil;
import airptool.util.CsvData;
import airptool.util.CsvDataBM;
import airptool.util.CsvDataMC;
import airptool.util.CsvFileWriter;
import airptool.util.CsvFileWriterBM;
import airptool.util.CsvFileWriterMC;
import airptool.util.FormatUtil;

public class SuitableModule {
	/*private static final ICoefficientStrategy[] coefficientStrategies = { new JaccardCoefficientStrategy(), new SMCCoefficientStrategy(),
			new YuleCoefficientStrategy(), new HamannCoefficientStrategy(), new SorensonCoefficientStrategy(),
			new RogersTanimotoCoefficientStrategy(), new SokalSneathCoefficientStrategy(), new RussellRaoCoefficientStrategy(),
			new BaroniUrbaniCoefficientStrategy(), new SokalBinaryDistanceCoefficientStrategy(), new OchiaiCoefficientStrategy(),
			new PhiBinaryDistance(), new PSCCoefficientStrategy(), new DotProductCoefficientStrategy(),
			new KulczynskiCoefficientStrategy(), new SokalSneath2CoefficientStrategy(), new SokalSneath4CoefficientStrategy(),
			new RelativeMatchingCoefficientStrategy() };*/
	
	private static final ICoefficientStrategy[] coefficientStrategies = { new JaccardCoefficientStrategy()};



	public static StringBuilder calculateAll(final DataStructure ds, final String classUnderAnalysis, final String expectedModule,
			final Collection<? extends Object> dependenciesClassUnderAnalysis, final Map<String, HashMap<String, Collection<? extends Object>>> packagesDependencies,
			final Collection<? extends Object> universeOfDependencies) {
		StringBuilder resume = new StringBuilder();
		//resume.append("Class Under Analysis: "+classUnderAnalysis+ "\t");

		//TODO: antes (primeira linha):Map<Class<? extends ICoefficientStrategy>, Set<Object[]>> suitableModulesByCoefficient = calculate(ds, classUnderAnalysis,
		
		String suitableModulesByCoefficient = calculate(ds, classUnderAnalysis,
				coefficientStrategies, dependenciesClassUnderAnalysis, packagesDependencies, universeOfDependencies);
		
		//TODO: descomentar
		/*for (ICoefficientStrategy cs : coefficientStrategies) {
			Set<Object[]> suitableModules = suitableModulesByCoefficient.get(cs.getClass());

			if (suitableModules != null) {
				int i = 0;
				boolean flag = true;
				for (Object[] o : suitableModules) {
					i++;
					if (o[0].equals(expectedModule) && !cs.getClass().equals(SokalBinaryDistanceCoefficientStrategy.class)) {
						resume.append(i + "\t" + FormatUtil.formatDouble((Double) o[1]) + "\t");
						flag = false;
						break;
					} else if (o[0].equals(expectedModule) && cs.getClass().equals(SokalBinaryDistanceCoefficientStrategy.class)) {
						resume.append(suitableModules.size() - i + 1 + "\t" + FormatUtil.formatDouble((Double) o[1]) + "\t");
						flag = false;
						break;
					}
				}
				if (flag) {
					resume.append(++i + "\t" + "0" + "\t");
				}
			}
		}

		resume.append("\t");*/

		//TODO: retirar isso:
		resume.append(suitableModulesByCoefficient+System.getProperty("line.separator"));
		
		return resume;
	}
	
	public static StringBuilder calculateAllBM(final DataStructure ds, String methodUnderAnalysis, final String expectedClass,
			final Collection<? extends Object> dependenciesMethodUnderAnalysis, final Map<String, Collection<? extends Object>> classDependencies,
			final Collection<? extends Object> universeOfDependencies, String blockNum, String tipo, String className) {
		StringBuilder resume = new StringBuilder();
		
		String suitableModulesByCoefficient = calculateBM(ds, expectedClass, methodUnderAnalysis,
				coefficientStrategies, dependenciesMethodUnderAnalysis, classDependencies, universeOfDependencies, blockNum, tipo, className);
		
		resume.append(suitableModulesByCoefficient+System.getProperty("line.separator"));
		
		return resume;
	}
	
	public static StringBuilder calculateAllMC(final DataStructure ds, String methodUnderAnalysis, final String expectedClass,
			final Collection<? extends Object> dependenciesMethodUnderAnalysis, final Map<String, HashMap<String, Collection<? extends Object>>> classDependencies,
			final Collection<? extends Object> universeOfDependencies, int lineUnderAnalysis) {
		StringBuilder resume = new StringBuilder();
		
		String suitableModulesByCoefficient = calculateMC(ds, expectedClass, methodUnderAnalysis,
				coefficientStrategies, dependenciesMethodUnderAnalysis, classDependencies, universeOfDependencies, lineUnderAnalysis);
		
		resume.append(suitableModulesByCoefficient+System.getProperty("line.separator"));
		
		return resume;
	}

	//TODO: antes (primeira linha): private static Map<Class<? extends ICoefficientStrategy>, Set<Object[]>> calculate(final DataStructure ds,
	
	private static String calculate(final DataStructure ds,
			final String classUnderAnalysis, final ICoefficientStrategy[] coefficientStrategies,
			final Collection<? extends Object> dependenciesClassUnderAnalysis, final Map<String, HashMap<String, Collection<? extends Object>>> packagesDependencies,
			final Collection<? extends Object> universeOfDependencies) {

		String tipo = "";
		List<CsvData> csvdatas = new ArrayList<CsvData>();
		
		Map<Class<? extends ICoefficientStrategy>, Set<Object[]>> result = new LinkedHashMap<Class<? extends ICoefficientStrategy>, Set<Object[]>>();
		for (ICoefficientStrategy cs : coefficientStrategies) {
			result.put(cs.getClass(), new TreeSet<Object[]>(new Comparator<Object[]>() {
				@SuppressWarnings("unchecked")
				@Override
				public int compare(Object[] o1, Object[] o2) {
					return ((Comparable<Double>) o2[1]).compareTo((Double) o1[1]);
				}
			}));
		}

		/*
		 * Also, if dependenciesClassA was empty, we do not calculate the
		 * suitable module.
		 */
		if (dependenciesClassUnderAnalysis.isEmpty()) {
			return null;
		}
		
		int i=1;

		for (Map.Entry<String, HashMap<String, Collection<? extends Object>>> entry : packagesDependencies.entrySet()) {
			String respectiveModuleName = entry.getKey();
			int j=0;
			//double mean=0;
			for (String respectiveClassName : entry.getValue().keySet()) {
				/*
				 * If dependencyType is null, the function above will consider all
				 * dependencies
				 */
				final Collection<? extends Object> dependenciesPackageUnderAnalysis = packagesDependencies.get(respectiveModuleName).get(respectiveClassName);
	
				int	a = CollectionUtils.intersection(dependenciesClassUnderAnalysis, dependenciesPackageUnderAnalysis).size(); // numberAB
				int	b = CollectionUtils.subtract(dependenciesClassUnderAnalysis, dependenciesPackageUnderAnalysis).size(); // numberAsubB
				int	c = CollectionUtils.subtract(dependenciesPackageUnderAnalysis, dependenciesClassUnderAnalysis).size(); // numberBsubA
				int	d = universeOfDependencies.size() - a - b - c; // numberNotAB
				
				//TODO: mudar essa parte (tirar comments
				
				/*for (ICoefficientStrategy cs : coefficientStrategies) {
					double similarity = cs.calculate(a, b, c, d);
	
					/* In order to avoid NaN values */
					/*if (!Double.isNaN(similarity) && !Double.isInfinite(similarity)) {
						result.get(cs.getClass()).add(new Object[] { respectiveModuleName, similarity });
					}
	
				}
				*/
				//mean=mean+(double)(a)/(a+b+c);
				
	
				if(AirpUtil.getPackageFromClassName(classUnderAnalysis).equals(respectiveModuleName)){
					tipo="Max";
				}
				else{
					tipo="Min";
				}
				
				j++;
				csvdatas.add(new CsvData(i, j, classUnderAnalysis, "\""+dependenciesClassUnderAnalysis.toString()+"\"", respectiveClassName, respectiveModuleName, "\""+dependenciesPackageUnderAnalysis.toString()+"\"", a, b, c, d, tipo,AirpUtil.getPackageFromClassName(classUnderAnalysis)));
				
			}
			//mean=mean/j;
			csvdatas.add(new CsvData());
			i++;
			
		}
		CsvFileWriter.writeCsvFile(csvdatas, ds.getProjectName());
		return "";
	}
	
	private static String calculateMC(final DataStructure ds, String expectedClass,
			final String methodUnderAnalysis, final ICoefficientStrategy[] coefficientStrategies,
			final Collection<? extends Object> dependenciesMethodUnderAnalysis, final Map<String, HashMap<String, Collection<? extends Object>>> classDependencies,
			final Collection<? extends Object> universeOfDependencies, int lineUnderAnalysis) {

		String tipo = "";
		List<CsvDataMC> csvdatasMC = new ArrayList<CsvDataMC>();
		
		Map<Class<? extends ICoefficientStrategy>, Set<Object[]>> result = new LinkedHashMap<Class<? extends ICoefficientStrategy>, Set<Object[]>>();
		for (ICoefficientStrategy cs : coefficientStrategies) {
			result.put(cs.getClass(), new TreeSet<Object[]>(new Comparator<Object[]>() {
				@SuppressWarnings("unchecked")
				@Override
				public int compare(Object[] o1, Object[] o2) {
					return ((Comparable<Double>) o2[1]).compareTo((Double) o1[1]);
				}
			}));
		}

		/*
		 * Also, if dependenciesClassA was empty, we do not calculate the
		 * suitable module.
		 */
		if (dependenciesMethodUnderAnalysis.isEmpty()) {
			return null;
		}
		
		int i=1;

		for (Map.Entry<String, HashMap<String, Collection<? extends Object>>> entry : classDependencies.entrySet()) {
			String respectiveClassName = entry.getKey();
			String respectiveModuleName = AirpUtil.getPackageFromClassName(respectiveClassName);
			//double mean=0;
			int j=0;
			for (String respectiveMethodName : entry.getValue().keySet()) {
				/*
				 * If dependencyType is null, the function above will consider all
				 * dependencies
				 */
				final Collection<? extends Object> dependenciesClassUnderAnalysis = classDependencies.get(respectiveClassName).get(respectiveMethodName);
	
				int	a = CollectionUtils.intersection(dependenciesMethodUnderAnalysis, dependenciesClassUnderAnalysis).size(); // numberAB
				int	b = CollectionUtils.subtract(dependenciesMethodUnderAnalysis, dependenciesClassUnderAnalysis).size(); // numberAsubB
				int	c = CollectionUtils.subtract(dependenciesClassUnderAnalysis, dependenciesMethodUnderAnalysis).size(); // numberBsubA
				int	d = universeOfDependencies.size() - a - b - c; // numberNotAB
				
				//TODO: mudar essa parte (tirar comments
				
				/*for (ICoefficientStrategy cs : coefficientStrategies) {
					double similarity = cs.calculate(a, b, c, d);
	
					/* In order to avoid NaN values */
					/*if (!Double.isNaN(similarity) && !Double.isInfinite(similarity)) {
						result.get(cs.getClass()).add(new Object[] { respectiveModuleName, similarity });
					}
	
				}
				*/
				//result2 = result2+"Classe - "+methodUnderAnalysis+" // Pacote - "+respectiveClassName+" ---- A: "+a+" B: "+b+" C: "+c+" D: "+d+System.getProperty("line.separator");
	
				//mean = mean+(double)(a)/(a+b+c);
				
				if(expectedClass.equals(respectiveClassName)){
					tipo="Max";
				}
				else{
					tipo="Min";
				}
				j++;
				csvdatasMC.add(new CsvDataMC(i,j, methodUnderAnalysis, expectedClass, "\""+dependenciesMethodUnderAnalysis.toString()+"\"", respectiveMethodName, respectiveClassName, respectiveModuleName, "\""+dependenciesClassUnderAnalysis.toString()+"\"", lineUnderAnalysis, a, b, c, d, tipo,AirpUtil.getPackageFromClassName(expectedClass)));
				
			}
			//mean=mean/j;
			csvdatasMC.add(new CsvDataMC());
			i++;
		}
		CsvFileWriterMC.writeCsvFileMC(csvdatasMC, ds.getProjectName());
		return "";
	}
	
	private static String calculateBM(final DataStructure ds, String expectedClass,
			final String methodUnderAnalysis, final ICoefficientStrategy[] coefficientStrategies,
			final Collection<? extends Object> dependenciesMethodUnderAnalysis, final Map<String, Collection<? extends Object>> classDependencies,
			final Collection<? extends Object> universeOfDependencies, String blockNum, String tipo, String className) {

		List<CsvDataBM> csvdatasBM = new ArrayList<CsvDataBM>();
		
		Map<Class<? extends ICoefficientStrategy>, Set<Object[]>> result = new LinkedHashMap<Class<? extends ICoefficientStrategy>, Set<Object[]>>();
		for (ICoefficientStrategy cs : coefficientStrategies) {
			result.put(cs.getClass(), new TreeSet<Object[]>(new Comparator<Object[]>() {
				@SuppressWarnings("unchecked")
				@Override
				public int compare(Object[] o1, Object[] o2) {
					return ((Comparable<Double>) o2[1]).compareTo((Double) o1[1]);
				}
			}));
		}

		/*
		 * Also, if dependenciesClassA was empty, we do not calculate the
		 * suitable module.
		 */
		if (dependenciesMethodUnderAnalysis.isEmpty()) {
			return null;
		}
		
		int i=1;

		//for (Map.Entry<String, HashMap<String, Collection<? extends Object>>> entry : classDependencies.entrySet()) {
			String respectiveClassName = className;
			String respectiveModuleName = AirpUtil.getPackageFromClassName(respectiveClassName);
			//double mean=0;
			for (Map.Entry<String, Collection<? extends Object>> entry : classDependencies.entrySet()) {
				String respectiveMethodName = entry.getKey();
				/*
				 * If dependencyType is null, the function above will consider all
				 * dependencies
				 */
				final Collection<? extends Object> dependenciesClassUnderAnalysis = classDependencies.get(respectiveMethodName);
	
				int	a = CollectionUtils.intersection(dependenciesMethodUnderAnalysis, dependenciesClassUnderAnalysis).size(); // numberAB
				int	b = CollectionUtils.subtract(dependenciesMethodUnderAnalysis, dependenciesClassUnderAnalysis).size(); // numberAsubB
				int	c = CollectionUtils.subtract(dependenciesClassUnderAnalysis, dependenciesMethodUnderAnalysis).size(); // numberBsubA
				int	d = universeOfDependencies.size() - a - b - c; // numberNotAB
				
				//TODO: mudar essa parte (tirar comments
				
				/*for (ICoefficientStrategy cs : coefficientStrategies) {
					double similarity = cs.calculate(a, b, c, d);
	
					/* In order to avoid NaN values */
					/*if (!Double.isNaN(similarity) && !Double.isInfinite(similarity)) {
						result.get(cs.getClass()).add(new Object[] { respectiveModuleName, similarity });
					}
	
				}
				*/
				//result2 = result2+"Classe - "+methodUnderAnalysis+" // Pacote - "+respectiveClassName+" ---- A: "+a+" B: "+b+" C: "+c+" D: "+d+System.getProperty("line.separator");
	
				//mean = mean+(double)(a)/(a+b+c);
		
				csvdatasBM.add(new CsvDataBM(i, methodUnderAnalysis, expectedClass, "\""+dependenciesMethodUnderAnalysis.toString()+"\"", respectiveMethodName, respectiveClassName, respectiveModuleName, "\""+dependenciesClassUnderAnalysis.toString()+"\"", blockNum, a, b, c, d, tipo,AirpUtil.getPackageFromClassName(expectedClass)));
				i++;
				
				
			}
			//TODO: método melhorzim de fazer (menos gambiarra)
			//Similaridade da Classe (Metodo EM)
			//Iterator it = classDependencies.entrySet().iterator();
			
			/*ArrayList<String> alreadyVisited = new ArrayList<String>(); 
			
			for (Map.Entry<String, Collection<? extends Object>> entry : classDependencies.entrySet()) {
				String respectiveMethodName = entry.getKey();
				alreadyVisited.add(respectiveMethodName);
				
				for (Map.Entry<String, Collection<? extends Object>> entry2 : classDependencies.entrySet()) {
					String respectiveMethodName2 = entry2.getKey();
					
					if(!alreadyVisited.contains(respectiveMethodName2)){
						final Collection<? extends Object> dependenciesClassUnderAnalysis = classDependencies.get(respectiveMethodName);
						final Collection<? extends Object> dependenciesClassUnderAnalysis2 = classDependencies.get(respectiveMethodName2);
						
						int	a = CollectionUtils.intersection(dependenciesClassUnderAnalysis, dependenciesClassUnderAnalysis2).size(); // numberAB
						int	b = CollectionUtils.subtract(dependenciesClassUnderAnalysis, dependenciesClassUnderAnalysis2).size(); // numberAsubB
						int	c = CollectionUtils.subtract(dependenciesClassUnderAnalysis2, dependenciesClassUnderAnalysis).size(); // numberBsubA
						int	d = universeOfDependencies.size() - a - b - c; // numberNotAB
						
						csvdatasBM.add(new CsvDataBM(i, methodUnderAnalysis, expectedClass, "\""+dependenciesMethodUnderAnalysis.toString()+"\"", respectiveMethodName, respectiveClassName, respectiveModuleName, "\""+dependenciesClassUnderAnalysis.toString()+"\"", blockNum, a, b, c, d, tipo,AirpUtil.getPackageFromClassName(expectedClass)));
						i++;
					}
				}
			}
			*/	
			
			//mean=mean/j;
			csvdatasBM.add(new CsvDataBM());

		CsvFileWriterBM.writeCsvFileBM(csvdatasBM, ds.getProjectName());
		return "";
	}

}