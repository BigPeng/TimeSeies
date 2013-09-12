package timeseries;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dataprocess.TextFile;

public class Classify {
	public Map<String, List<double[]>> formatTestData(
			List<String[]> testStringData) {
		String deviceId = testStringData.get(0)[0];
		Map<String, List<double[]>> testData = new LinkedHashMap<String, List<double[]>>();
		List<double[]> oneDivice = new ArrayList<double[]>();
		for (String[] line : testStringData) {
			if (!line[0].equals(deviceId)) {
				deviceId = line[0];
				oneDivice = new ArrayList<double[]>();
			}
			double[] doubleLine = new double[line.length - 1];
			for (int i = 1; i < line.length - 1; i++) {
				doubleLine[i - 1] = Double.valueOf(line[i]);
			}
			if (!testData.containsKey(deviceId)) {
				testData.put(deviceId, oneDivice);
			}
			oneDivice.add(doubleLine);
		}
		return testData;
	}

	/**
	 * 将String数据转换为double类型
	 * 
	 * @param trianStringData
	 * @return
	 */
	public List<double[]> str2double(List<String[]> trianStringData) {
		List<double[]> doubleData = new ArrayList<double[]>();
		for (String[] line : trianStringData) {
			double[] doubleLine = new double[line.length];
			int i = 0;
			for (String item : line)
				doubleLine[i++] = Double.valueOf(item);
			doubleData.add(doubleLine);
		}
		return doubleData;
	}

	public void classify(String writeFileName) throws IOException {
		final int max_m = 301;
		final int max_n = 501;
		Similarity similar = new Similarity(max_m, max_n);
		List<String[]> testStringData = TextFile.readAsMatrix(
				".\\data\\test.csv", ",");
		Map<String, List<double[]>> testData = formatTestData(testStringData);
		testStringData.clear();
		PrintWriter out = new PrintWriter(
				new File(writeFileName).getAbsoluteFile());
		File dir = new File(".\\data\\train2");
		for (File trianFile : dir.listFiles()) {
			System.out.println(trianFile.getName());
			List<String[]> trianStringData = TextFile.readAsMatrix(
					trianFile.getAbsolutePath(), ",");
			List<double[]> trainData = str2double(trianStringData);
			for (Map.Entry<String, List<double[]>> dataPerDevice : testData
					.entrySet()) {
				int segmentId = 0;
				for (double[] segmentQ : dataPerDevice.getValue()) {
					out.print(dataPerDevice.getKey() + "$" + segmentId++);
					double min = Double.MAX_VALUE;
					int minLable = 0;
					int k = 0;
					for (double[] segmentC : trainData) {
						k++;
						if (segmentC.length > max_n) {
							double[] segmentTemp = new double[max_n];
							for (int i = 0; i < max_n; i++)
								segmentTemp[i] = segmentC[i];
							segmentC = segmentTemp;
						}
						double distance = similar.constraintDtw(segmentQ,
								segmentC, 0.1f);
						if (distance < min) {
							min = distance;
							minLable = k;
						}
					}

					String trianDeviceId = trianFile.getName().split("\\.")[0];
					out.print("," + trianDeviceId);
					out.print("," + min);
					out.print("," + minLable);
					out.print(",");
				}
				out.println();
			}
		}
		out.close();
	}

	public static void main(String[] args) throws IOException {

		// String s = "7.csv";
		// String[] t = s.split("\\.");
		Classify classifier = new Classify();
		classifier.classify("data\\distance6.csv");
	}
}
