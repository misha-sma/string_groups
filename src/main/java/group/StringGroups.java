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

	private static final Pattern LINE_PATTERN = Pattern.compile("((\"(\\d+(\\.\\d+)?)?\")?;)*(\"(\\d+(\\.\\d+)?)?\")?");
	private static final String EMPTY_STRING = "";
	private static final String QUOTES_EMPTY_STRING = "\"\"";

	private static List<List<String>> megaList;
	private static int maxCount;
	private static List<Map<String, List<Integer>>> mmList;
	private static List<Set<String>> usedKeysList;
	private static Set<Integer> currentSet;

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

		megaList = new ArrayList<List<String>>(set.size());
		String[] parts = null;
		maxCount = 0;
		for (String line : set) {
			int count = 0;
			int i = 0;
			for (i = line.length() - 1; i >= 0; --i) {
				char c = line.charAt(i);
				if (c == ';') {
					++count;
				} else {
					break;
				}
			}
			List<String> values = null;
			if (i < 0) {
				++count;
				values = new ArrayList<String>(count);
				for (i = 0; i < count; ++i) {
					values.add(EMPTY_STRING);
				}
			} else {
				parts = line.split(";");
				values = new ArrayList<String>(parts.length + count);
				for (String part : parts) {
					values.add(part.isEmpty() ? EMPTY_STRING : (part.length() <= 2 ? QUOTES_EMPTY_STRING : part));
				}
				for (i = 0; i < count; ++i) {
					values.add(EMPTY_STRING);
				}
			}
			megaList.add(values);
			if (values.size() > maxCount) {
				maxCount = values.size();
			}
		}
		set = null;
		parts = null;

		mmList = new ArrayList<Map<String, List<Integer>>>(maxCount);
		for (int i = 0; i < maxCount; ++i) {
			mmList.add(new HashMap<String, List<Integer>>(80000));
		}
		for (int j = 0; j < megaList.size(); ++j) {
			List<String> values = megaList.get(j);
			for (int i = 0; i < values.size(); ++i) {
				String value = values.get(i);
				if (value == EMPTY_STRING || value == QUOTES_EMPTY_STRING) {
					continue;
				}
				Map<String, List<Integer>> mapOne = mmList.get(i);
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

		usedKeysList = new ArrayList<Set<String>>(maxCount);
		for (int i = 0; i < maxCount; ++i) {
			usedKeysList.add(new HashSet<String>());
		}
		currentSet = new HashSet<Integer>();
		List<Set<Integer>> allGroups = new ArrayList<Set<Integer>>();
		for (int i = 0; i < maxCount; ++i) {
			Map<String, List<Integer>> mapOne = mmList.get(i);
			Set<String> usedKeysSet = usedKeysList.get(i);
			for (Map.Entry<String, List<Integer>> entry : mapOne.entrySet()) {
				if (usedKeysSet.contains(entry.getKey())) {
					continue;
				}
				List<Integer> currentList = entry.getValue();
				if (currentList.size() <= 1) {
					continue;
				}
				currentSet.addAll(currentList);
				usedKeysSet.add(entry.getKey());
				recursiveSearch(currentList, i);
				allGroups.add(currentSet);
				currentSet = new HashSet<Integer>();
			}
		}
		mmList = null;
		usedKeysList = null;
		currentSet = null;

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
					List<String> oneList = megaList.get(index);
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
				List<String> oneList = megaList.get(i);
				fw.write(buildOneGroupString(oneList));
				++index;
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("All Time=" + (System.currentTimeMillis() - startTime) + " ms");
	}

	private static void recursiveSearch(List<Integer> currentList, int wrongIndex) {
		for (int i = 0; i < maxCount; ++i) {
			if (i == wrongIndex) {
				continue;
			}
			List<String> keys = new LinkedList<String>();
			for (Integer index : currentList) {
				List<String> values = megaList.get(index);
				String value = i < values.size() ? values.get(i) : EMPTY_STRING;
				if (value == EMPTY_STRING || value == QUOTES_EMPTY_STRING) {
					continue;
				}
				keys.add(value);
			}
			if (keys.isEmpty()) {
				continue;
			}
			Map<String, List<Integer>> mapOne = mmList.get(i);
			Set<String> usedKeysSet = usedKeysList.get(i);
			for (String key : keys) {
				if (usedKeysSet.contains(key)) {
					continue;
				}
				List<Integer> values = mapOne.get(key);
				if (values == null || values.size() <= 1) {
					continue;
				}
				usedKeysSet.add(key);
				currentSet.addAll(values);
				recursiveSearch(values, i);
			}
		}
	}

	private static String buildOneGroupString(List<String> oneList) {
		StringBuilder builder = new StringBuilder();
		builder.append('\n');
		for (int j = 0; j < oneList.size(); ++j) {
			if (j > 0) {
				builder.append(';');
			}
			builder.append(oneList.get(j));
		}
		return builder.toString();
	}
}
