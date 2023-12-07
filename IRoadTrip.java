import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

public class IRoadTrip {

    private Map<String, Map<String, Integer>> countryGraph = new HashMap<>();
    private Map<String, String> countryNames = new HashMap<>();
    private Map<String, Map<String, Integer>> countryDistances = new HashMap<>();
    private Map<String, String> fixedNames;

    public IRoadTrip(String[] args) {
        if (args.length == 3) {
            readCountryDistancesFromCSV();
            mapStateIDToCountryNames();
            fixedNames = createFixedCountries();
            loadBorderingCountriesFromTxt();
        }
    }

    private void readCountryDistancesFromCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("capdist.csv"))) {
            reader.readLine(); // Skip the first line
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String originCountry = parts[1].trim();
                String destinationCountry = parts[3].trim();
                Integer distance = Integer.parseInt(parts[4].trim());

                countryDistances
                        .computeIfAbsent(originCountry, k -> new HashMap<>())
                        .put(destinationCountry, distance);
                countryDistances
                        .computeIfAbsent(destinationCountry, k -> new HashMap<>())
                        .put(originCountry, distance);  // Include both directions
            }
        } catch (IOException e) {
            System.err.println("An error occurred while processing CSV file: " + e.getMessage());
        }
    }

    private void mapStateIDToCountryNames() {
        try (BufferedReader reader = new BufferedReader(new FileReader("state_name.tsv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Pattern pattern = Pattern.compile("\\b[\\S ]+\\b");
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    String countryName = findKey(matcher, 2);
                    String countryID = findKey(matcher, 2);
                    countryNames.put(countryID, countryName);

                    // Add the country name to the countryGraph using both fixed and original names
                    countryGraph.computeIfAbsent(countryName, k -> new HashMap<>());
                    countryGraph.computeIfAbsent(countryID, k -> new HashMap<>());
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred while processing state names file: " + e.getMessage());
        }
    }

    private void loadBorderingCountriesFromTxt() {
        try (BufferedReader reader = new BufferedReader(new FileReader("borders.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=|;");
                System.out.println(Arrays.toString(parts)); // Print the parts array

                String originalCountry = parts[0].trim();
                String fixedCountry = getFixedCountryName(originalCountry);

                Map<String, Integer> borderCountryList = countryGraph.getOrDefault(fixedCountry, new HashMap<>());

                for (int i = 1; i < parts.length; i++) {
                    String[] stripBorders = parts[i].trim().split("\\s+\\d[\\d,]*\\s+km");

                    String originalBorderingCountry = stripBorders[0].trim();
                    String fixedBorderingCountry = getFixedCountryName(originalBorderingCountry);

                    System.out.println("Original Bordering Country: " + originalBorderingCountry);
                    System.out.println("Fixed Bordering Country: " + fixedBorderingCountry);

                    if (!fixedBorderingCountry.isEmpty() && !fixedBorderingCountry.equals(fixedCountry)) {
                        int length = getDistance(originalCountry, originalBorderingCountry);
                        if (length == -1) {
                            continue;
                        }

                        borderCountryList.put(fixedBorderingCountry, length);
                    }
                }

                countryGraph.put(fixedCountry, borderCountryList);

                System.out.println("Original Country: " + originalCountry);
                System.out.println("Fixed Country: " + fixedCountry);
                System.out.println("Border Country List: " + borderCountryList);
            }
        } catch (IOException e) {
            System.err.println("An error occurred while processing borders file: " + e.getMessage());
        }
    }

    private Map<String, String> createFixedCountries() {
        Map<String, String> fixedCountries = new HashMap<String, String>();
        fixedCountries.put("German Federal Republic", "Germany");
        fixedCountries.put("Macedonia (Former Yugoslav Republic of)", "Macedonia");
        fixedCountries.put("Bosnia-Herzegovina", "Bosnia and Herzegovina");
        fixedCountries.put("Bahamas", "Bahamas, The");
        fixedCountries.put("Zambia.", "Zambia");
        fixedCountries.put("US", "United States of America");
        fixedCountries.put("United States", "United States of America");
        fixedCountries.put("Greenland).", "Greenland");
        fixedCountries.put("Congo, Democratic Republic of (Zaire)", "Democratic Republic of the Congo");
        fixedCountries.put("Congo, Democratic Republic of the", "Democratic Republic of the Congo");
        fixedCountries.put("Congo, Republic of the", "Republic of the Congo");
        fixedCountries.put("Gambia, The", "The Gambia");
        fixedCountries.put("Gambia", "The Gambia");
        fixedCountries.put("Macedonia", "North Macedonia");
        fixedCountries.put("Macedonia (Former Yugoslav Republic of)", "North Macedonia");
        fixedCountries.put("Italy.", "Italy");
        fixedCountries.put("East Timor", "Timor-Leste");
        fixedCountries.put("UK", "United Kingdom");
        fixedCountries.put("Korea, North", "North Korea");
        fixedCountries.put("Korea, People's Republic of", "North Korea");
        fixedCountries.put("Korea, South", "South Korea");
        fixedCountries.put("Korea, Republic of", "South Korea");
        fixedCountries.put("UAE", "United Arab Emirates");
        fixedCountries.put("Turkey (Turkiye)", "Turkey");
        fixedCountries.put("Botswana.", "Botswana");
        fixedCountries.put("Myanmar (Burma)", "Burma");
        fixedCountries.put("Vietnam, Democratic Republic of", "Vietnam");
        fixedCountries.put("Cambodia (Kampuchea)", "Cambodia");
        fixedCountries.put("Sri Lanka (Ceylon)", "Sri Lanka");
        fixedCountries.put("Kyrgyz Republic", "Kyrgyzstan");
        fixedCountries.put("Yemen (Arab Republic of Yemen)", "Yemen");
        fixedCountries.put("Turkey (Ottoman Empire)", "Turkey");
        fixedCountries.put("Iran (Persia)", "Iran");
        fixedCountries.put("Zimbabwe (Rhodesia)", "Zimbabwe");
        fixedCountries.put("Tanzania/Tanganyika", "Tanzania");
        fixedCountries.put("Congo", "Republic of the Congo");
        fixedCountries.put("Burkina Faso (Upper Volta)", "Burkina Faso");
        fixedCountries.put("Belarus (Byelorussia)", "Belarus");
        fixedCountries.put("Russia (Soviet Union)", "Russia");
        fixedCountries.put("Italy/Sardinia", "Italy");
        addBordersToFixedCountries(fixedCountries);
        return fixedCountries;
    }

    private void addBordersToFixedCountries(Map<String, String> fixedCountries) {
        if (fixedNames != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader("borders.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=|;");
                    String originalCountry1 = parts[0].trim();
                    String country1 = getFixedCountryName(originalCountry1);

                    // Check if the country is in the fixedNames map
                    if (fixedNames.containsKey(country1)) {
                        Map<String, Integer> borderCountryList = new HashMap<>();

                        for (int i = 1; i < parts.length; i++) {
                            String[] stripBorders = parts[i].trim().split("\\s+\\d[\\d,]*\\s+km");
                            String originalBorderingCountry = stripBorders[0].trim();
                            String borderingCountry = getFixedCountryName(originalBorderingCountry);

                            if (!borderingCountry.isEmpty() && !borderingCountry.equals(country1)) {
                                int length = getDistance(originalCountry1, originalBorderingCountry);
                                if (length == -1) {
                                    continue;
                                }
                                borderCountryList.put(borderingCountry, length);
                            }
                        }
                        // Add borders to fixedNames map
                        fixedNames.put(country1, fixedNames.get(country1) + " Borders: " + borderCountryList);
                    }
                }
            } catch (IOException e) {
                System.err.println("An error occurred while processing file: " + e.getMessage());
            }
        } else {
            System.out.println("Error: fixedNames is null.");
        }
    }

    private String getFixedCountryName(String userInput) {
        // Check if the user input is directly in the fixedCountries map
        if (fixedNames.containsKey(userInput)) {
            return fixedNames.get(userInput);
        }

        // Check if the user input is one of the original names in fixedCountries
        for (Map.Entry<String, String> entry : fixedNames.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(userInput)) {
                return entry.getValue();
            }
        }
        // If not found, return the original user input
        return userInput;
    }

    private static String findKey(Matcher matcher, int position) {
        for (int i = 1; i < position && matcher.find(); i++) {
            // Skip the loop
        }
        return matcher.group();
    }

    public int getDistance(String originCountry, String destinationCountry) {
        String originCountryID = countryNames.get(originCountry);
        String destinationCountryID = countryNames.get(destinationCountry);

        if (originCountryID != null && destinationCountryID != null) {
            Map<String, Integer> originDistances = countryDistances.get(originCountryID);

            if (originDistances != null) {
                Integer distance = originDistances.get(destinationCountryID);

                if (distance != null) {
                    return distance;
                }
            }
        }
        return -1;
    }

    public List<String> findPath(String startLocation, String endLocation) {
        Set<String> visitedNodes = new HashSet<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        Map<String, Integer> shortestDistances = new HashMap<>();
        Map<String, String> previousNode = new HashMap<>();

        for (String node : countryGraph.keySet()) {
            shortestDistances.put(node, Integer.MAX_VALUE);
        }

        shortestDistances.put(startLocation, 0);
        priorityQueue.add(new Node(startLocation, 0));
        List<String> path = new ArrayList<>();

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            String current = currentNode.country;

            if (shortestDistances.get(current) == 1) {
                continue;
            }

            if (!visitedNodes.contains(current)) {
                visitedNodes.add(current);
            } else {
                continue;
            }

            if (countryGraph.containsKey(current)) {
                for (Map.Entry<String, Integer> neighbor : countryGraph.get(current).entrySet()) {
                    String adjacentLocation = neighbor.getKey();
                    int weight = neighbor.getValue();
                    int newDistance = shortestDistances.get(current) + weight;

                    if (newDistance < shortestDistances.getOrDefault(adjacentLocation, Integer.MAX_VALUE)) {
                        shortestDistances.put(adjacentLocation, newDistance);
                        priorityQueue.add(new Node(adjacentLocation, newDistance));
                        previousNode.put(adjacentLocation, current);
                    }
                }
            }
            if (current.equals(endLocation)) {
                break;
            }
        }

        String currentLocation = endLocation;
        while (!currentLocation.equals(startLocation)) {
            String previousLocation = previousNode.get(currentLocation);
            if (shortestDistances.get(currentLocation) == null || shortestDistances.get(previousLocation) == null) {
                return null;
            }
            int distance = shortestDistances.get(currentLocation) - shortestDistances.get(previousLocation);
            path.add(previousLocation + " --> " + currentLocation + " (" + distance + " km.)");
            currentLocation = previousLocation;
        }
        Collections.reverse(path);

        return path;
    }

    public void acceptUserInput() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Enter the name of the first country (type EXIT to quit): ");
                String startCountry = scanner.nextLine().trim();
                startCountry = getFixedCountryName(startCountry);
                if (startCountry.equalsIgnoreCase("EXIT")) {
                    break;
                } else if (!countryGraph.containsKey(startCountry)) {
                    System.out.println("Invalid country name. Please enter a valid country name.");
                    continue;
                }

                System.out.print("Enter the name of the second country (type EXIT to quit): ");
                String endCountry = scanner.nextLine().trim();
                endCountry = getFixedCountryName(endCountry);
                if (!countryGraph.containsKey(endCountry)) {
                    System.out.println("Invalid country name. Please enter a valid country name.");
                    continue;
                }
                List<String> path = findPath(startCountry, endCountry);
                if (path == null) {
                    System.out.println("No path found.");
                    continue;
                }

                // Display the result
                System.out.println("Route from " + startCountry + " to " + endCountry + ":");
                for (String step : path) {
                    System.out.println("* " + step);
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while processing file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        IRoadTrip roadTrip = new IRoadTrip(args);
        roadTrip.acceptUserInput();
    }

    public Map<String, Map<String, Integer>> getCountryGraph() {
        return countryGraph;
    }

    public class Node implements Comparable<Node> {
        String country;
        int distance;

        Node(String node, int distance) {
            this.country = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(Node n1) {
            return this.distance - n1.distance;
        }
    }
}