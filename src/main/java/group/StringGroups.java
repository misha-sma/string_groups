package group;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringGroups {

	private static final Pattern LINE_PATTERN = Pattern.compile("(\"\\d*\";)*\"\\d*\"");

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		if (args.length < 2) {
			System.out.println(
					"Usage: java -jar group-strings-art-0.0.1-SNAPSHOT.jar path_to_input_file path_to_output_file");
			return;
		}

		String path2InputFile = args[0];
		String path2OutputFile = args[1];
		Set<String> set = new HashSet<String>();
		try {
			FileReader fr = new FileReader(path2InputFile);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				Matcher m = LINE_PATTERN.matcher(line);
				if (m.matches()) {
					set.add(line);
				}
				line = br.readLine();
			}
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<List<Long>> megaList = new ArrayList<List<Long>>(set.size());
		String[] parts = null;
		int maxCount = 0;
		for (String line : set) {
			parts = line.split(";");
			List<Long> values = new ArrayList<Long>(parts.length);
			for (String part : parts) {
				values.add(part.length() <= 2 ? null : Long.parseLong(part.substring(1, part.length() - 1)));
			}
			megaList.add(values);
			if (values.size() > maxCount) {
				maxCount = values.size();
			}
		}
		set = null;
		parts = null;

		List<Map<Long, List<Integer>>> mmList = new ArrayList<Map<Long, List<Integer>>>(maxCount);
		for (int i = 0; i < maxCount; ++i) {
			mmList.add(new HashMap<Long, List<Integer>>(80000));
		}
		for (int j = 0; j < megaList.size(); ++j) {
			List<Long> values = megaList.get(j);
			for (int i = 0; i < values.size(); ++i) {
				Long value = values.get(i);
				if (value == null) {
					continue;
				}
				Map<Long, List<Integer>> mapOne = mmList.get(i);
				List<Integer> setOne = mapOne.get(value);
				if (setOne == null) {
					setOne = new LinkedList<Integer>();
					setOne.add(j);
					mapOne.put(value, setOne);
				} else {
					setOne.add(j);
				}
			}
		}

		List<Set<Integer>> allGroups = new ArrayList<Set<Integer>>();
		List<Set<Integer>> curGroups = new ArrayList<Set<Integer>>();
		for (int i = 0; i < maxCount; ++i) {
			Map<Long, List<Integer>> mapOne = mmList.get(i);
			for (Map.Entry<Long, List<Integer>> entry : mapOne.entrySet()) {
				if (entry.getValue().size() <= 1) {
					continue;
				}
				List<Integer> matchedIndexes = new ArrayList<Integer>();
				for (int j = 0; j < allGroups.size(); ++j) {
					Set<Integer> setOld = allGroups.get(j);
					if (isIntersec(setOld, entry.getValue())) {
						matchedIndexes.add(j);
					}
				}
				if (matchedIndexes.isEmpty()) {
					Set<Integer> curSet = new HashSet<Integer>();
					curSet.addAll(entry.getValue());
					curGroups.add(curSet);
				} else {
					Set<Integer> mainSetOld = allGroups.get(matchedIndexes.get(0));
					mainSetOld.addAll(entry.getValue());
					for (int j = matchedIndexes.size() - 1; j > 0; --j) {
						mainSetOld.addAll(allGroups.get(matchedIndexes.get(j)));
						allGroups.remove(matchedIndexes.get(j).intValue());
					}
				}
			}
			allGroups.addAll(curGroups);
			curGroups = new ArrayList<Set<Integer>>();
		}
		mmList = null;
		curGroups = null;

		Collections.sort(allGroups, (a, b) -> b.size() - a.size());

		Set<Integer> usedSet = new HashSet<Integer>();
		try {
			FileWriter fw = new FileWriter(path2OutputFile);
			fw.write(String.valueOf(allGroups.size()));
			for (int i = 0; i < allGroups.size(); ++i) {
				Set<Integer> setOne = allGroups.get(i);
				fw.write("\n\nГруппа " + (i + 1));
				for (Integer index : setOne) {
					usedSet.add(index);
					List<Long> oneList = megaList.get(index);
					fw.write(buildOneGroupString(oneList));
				}
			}
			int index = allGroups.size() + 1;
			allGroups = null;
			for (int i = 0; i < megaList.size(); ++i) {
				if (usedSet.contains(i)) {
					continue;
				}
				fw.write("\n\nГруппа " + index);
				List<Long> oneList = megaList.get(i);
				fw.write(buildOneGroupString(oneList));
				++index;
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("All Time=" + (System.currentTimeMillis() - startTime) + " ms");
	}

	private static String buildOneGroupString(List<Long> oneList) {
		StringBuilder builder = new StringBuilder();
		builder.append('\n');
		for (int j = 0; j < oneList.size(); ++j) {
			if (j > 0) {
				builder.append(';');
			}
			Long value = oneList.get(j);
			if (value == null) {
				builder.append("\"\"");
			} else {
				builder.append('\"').append(value).append('\"');
			}
		}
		return builder.toString();
	}

	private static boolean isIntersec(Set<Integer> set, List<Integer> list) {
		for (Integer v : list) {
			if (set.contains(v)) {
				return true;
			}
		}
		return false;
	}
}
