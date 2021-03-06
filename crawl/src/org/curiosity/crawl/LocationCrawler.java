package org.curiosity.crawl;

import com.google.common.collect.ImmutableList;
import org.curiosity.concept.RoverLocation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Crawls Curiosity's location.
 *
 * XXX: Currently BROKEN as the location file contains bad data. (1/8/2015)
 *
 * @author jherwitz
 */
public class LocationCrawler extends Crawler {

    // XXX: use a file since JSoup was having issues pulling the large file over http
    private String locationsUri = "bin/locations.xml"; // http://mars.jpl.nasa.gov/msl-raw-images/locations.xml

    /**
     * Crawls the location file, returning a list of {@link RoverLocation}s.
     */
    public List<RoverLocation> crawl() {
        Document document;
        try {
            document = Jsoup.parse(new File(locationsUri), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return parseLocations(document);
    }

    /**
     * Parse the list of {@link RoverLocation}s from the retrieved {@code document}.
     */
    private List<RoverLocation> parseLocations(Document document) {
        // only use the last location from each endSol
        Map<Integer, RoverLocation> roverLocations = new HashMap<>();

        Elements locations = document.select("location");
        for(Element location : locations) {
            try {
                int sol = Integer.parseInt(location.select("endSol").text());
                if(roverLocations.containsKey(sol)) {
                    continue;
                }

                double latitude = Double.parseDouble(location.select("lat").text());
                double longitude = Double.parseDouble(location.select("lon").text());
                String arrivalTime = location.select("arrivalTime").text();
                roverLocations.put(sol, new RoverLocation(sol, latitude, longitude, arrivalTime));
            } catch(Throwable t){
                t.printStackTrace();
            }
        }

        return ImmutableList.copyOf(roverLocations.values());
    }
}
