package stocker;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stocker.data.MainDataFetcher;
import stocker.data.fetchers.YahooFinanceFetcher;
import stocker.database.CandlestickDao;
import stocker.representation.Candlestick;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests the integration between the API and the database, ensuring that the data fetched from the API
 * is correctly saved to the database.
 *
 * @author Joakim Colloz
 * @version 1.0
 * @see stocker.data.MainDataFetcher
 * @see YahooFinanceFetcher
 * @see stocker.data.parsers.YahooFinanceParser
 * @see stocker.database.CandlestickDao
 */
public class ApiToDatabaseIntegrationIT {
    private MainDataFetcher mainDataFetcher;
    private static CandlestickDao candlestickDao;

    @BeforeEach
    void setUp() {
        mainDataFetcher = new MainDataFetcher();

        candlestickDao = new CandlestickDao();
        candlestickDao.resetTable();
    }

    @AfterAll
    static void tearDown() {
        candlestickDao.resetTable();
    }

    @Test
    void testAddCurrentDayDataForAAkAndABBToDb() {
        // Fetch and insert data to DB
        mainDataFetcher.addPriceDataToDb(List.of("AAK", "ABB"), MainDataFetcher.Range.ONE_DAY,
                MainDataFetcher.Interval.ONE_DAY);

        // Get data from DB
        List<Candlestick> aak = candlestickDao.getAllRowsByName("AAK.ST");
        List<Candlestick> abb = candlestickDao.getAllRowsByName("ABB.ST");

        assertFalse(aak.isEmpty(), "AAK current day data should exist in DB");
        assertEquals(1, abb.size(), "There should be 1 day of AAB price data in the database");
        assertFalse(abb.isEmpty(), "ABB current day data should exist in DB");
        assertEquals(1, abb.size(), "There should be 1 day of AAK price data in the database");
    }

    @Test
    void testAddHistoricalDataForAAkAndABBToDb() {
        // Fetch and insert data to DB
        mainDataFetcher.addPriceDataToDb(List.of("AAK", "ABB"), MainDataFetcher.Range.THREE_MONTHS,
                MainDataFetcher.Interval.ONE_DAY);

        // Get data from DB
        List<Candlestick> aak = candlestickDao.getAllRowsByName("AAK.ST");
        List<Candlestick> abb = candlestickDao.getAllRowsByName("ABB.ST");

        assertFalse(aak.isEmpty(), "AAK historical data should exist in DB");
        assertTrue(is3MonthsOfPriceDataInDB(aak), "There should be 3 months of AAK price data in the database");
        assertFalse(abb.isEmpty(), "ABB historical data should exist in DB");
        assertTrue(is3MonthsOfPriceDataInDB(abb), "There should be 3 months of ABB price data in the database");
    }

    private static boolean is3MonthsOfPriceDataInDB(List<Candlestick> candlesticks) {
        return candlesticks.size() >= 57 && candlesticks.size() <= 70;
    }
}
