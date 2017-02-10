package airptool.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CsvFileWriterMC {
	
	//Delimiter used in CSV file
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	//CSV file header
	private static final String FILE_HEADER = "id,id2,metodo,classe,pacote,metodoDaClasse,a,b,c,d,tipo";
	
	//private static List<CsvDataMC> csvdatasMC = new ArrayList<CsvDataMC>();

	//public static void addCsvDataMC(List<CsvDataMC> csvdataMC){
	//	csvdatasMC.addAll(csvdataMC);
	//}
	
	public static void writeCsvFileMC(List<CsvDataMC> csvdataMC) {
		
		//Create new students objects
		/*
		Student student1 = new Student(1, "Ahmed", "Mohamed", "M", 25);
		Student student2 = new Student(2, "Sara", "Said", "F", 23);
		Student student3 = new Student(3, "Ali", "Hassan", "M", 24);
		Student student4 = new Student(4, "Sama", "Karim", "F", 20);
		Student student5 = new Student(5, "Khaled", "Mohamed", "M", 22);
		Student student6 = new Student(6, "Ghada", "Sarhan", "F", 21);
		*/
		
		//Create a new list of student objects
		/*
		List students = new ArrayList();
		students.add(student1);
		students.add(student2);
		students.add(student3);
		students.add(student4);
		students.add(student5);
		students.add(student6);
		*/
		
		FileWriter fileWriter = null;
				
		try {
			fileWriter = new FileWriter(AirpUtil.TEMP_FOLDER + "/dataMC.csv", true);

			//Write the CSV file header
			//fileWriter.append(FILE_HEADER.toString());
			
			//Add a new line separator after the header
			//fileWriter.append(NEW_LINE_SEPARATOR);
			
			//Write a new student object list to the CSV file
			for (CsvDataMC csvdMC : csvdataMC) {
				fileWriter.append(String.valueOf(csvdMC.getId()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(csvdMC.getId2()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(csvdMC.getMetodo());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(csvdMC.getClasse());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(csvdMC.getDepUnder());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(csvdMC.getMetodoDaClasse());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(csvdMC.getClasseAlvo());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(csvdMC.getPacoteAlvo());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(csvdMC.getDepAlvo());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(csvdMC.getLinha()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(csvdMC.getA()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(csvdMC.getB()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(csvdMC.getC()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(csvdMC.getD()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(csvdMC.getTipo());
				fileWriter.append(NEW_LINE_SEPARATOR);
			}

			
			
			//System.out.println("CSV file was created successfully !!!");
			
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
}